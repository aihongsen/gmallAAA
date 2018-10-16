package com.atguigu.gmall.manager.Service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.Manager.BaseAttrInfoService;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.BaseAttrValue;
import com.atguigu.gmall.manager.BaseCatalog3;
import com.atguigu.gmall.manager.Mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manager.Mapper.BaseAttrValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.runtime.directive.Foreach;
import org.apache.zookeeper.data.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aihs
 * @user Shinelon
 * @date 2018/10/12-23:21
 */
@Slf4j
@Service    //通过dubble暴露Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {

    @Autowired  //自动装配baseAttrInfoMapper
    BaseAttrInfoMapper  baseAttrInfoMapper;

    @Autowired  //自动装配baseAttrValueMapper
    BaseAttrValueMapper baseAttrValueMapper;
    /**
     * 返回一个BaseAttrInfo数据集合
     * @param catalog3Id 三级分类ID
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoByCatalog3Id(Integer catalog3Id) {

       return baseAttrInfoMapper.selectList(
               new QueryWrapper<BaseAttrInfo>()
                       .eq("catalog3_id",catalog3Id));
    }


    /**
     * 获取所有属性信息
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAllAttrInfo() {

        return baseAttrInfoMapper.selectList(null);
    }

    /**
     * 根据AttrInfoId获得对应的属性值
     * @param baseAttrInfoId
     * @return
     */
    @Override
    public List<BaseAttrValue> getBaseAttrValueByAttrId(Integer baseAttrInfoId) {

        return baseAttrValueMapper.selectList(
                new QueryWrapper<BaseAttrValue>()
                        .eq("attr_id",baseAttrInfoId));
    }


    /**
     * 开启事务 需要2步骤
     * 第一在需要事务处开启注解@Transactional
     * 第二需要在Service的主程序GmallManagerServiceApplication中开启事务
     * @EnableTransactionManagement
     */
    @Transactional      //事务处理方法(需要都成功才操作否则回滚 只需要加这个@Transactional)
    @Override
    public void saveOrUpdateBaseInfo(BaseAttrInfo baseAttrInfo) {
        log.info("准备修改的数据:{}",baseAttrInfo);
        //先判断是修改、保存、删除
        if(baseAttrInfo.getId() !=null){
            //1.修改基本属性
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //2.修改属性的属性值
            List<BaseAttrValue> attrValues = baseAttrInfo.getAttrValues();
            List<Integer> ids = new ArrayList<>();
            //2.1) 删除没有提交过来的数据
            for(BaseAttrValue attrValue : attrValues){
                Integer id = attrValue.getId();
                //判断如果ID不为空则有ID
                if(id!=null){
                    //收集页面所有属性值的ID 放到ids集合总
                    ids.add(id);
                }
            }
            baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>().notIn("id",ids).eq("attr_id",baseAttrInfo.getId()));
            for (BaseAttrValue attrValue : attrValues) {
                if(attrValue.getId() != null){
                    //2.2） 提交过来的数据如果有ID就是修改
                    baseAttrValueMapper.updateById(attrValue);

                }else{
                //2.3） 如果没有ID就是新增
                    baseAttrValueMapper.insert(attrValue);
                }
            }

        }
    }
}
