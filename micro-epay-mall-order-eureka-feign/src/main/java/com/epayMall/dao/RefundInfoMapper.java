package com.epayMall.dao;

import org.apache.ibatis.annotations.Param;

import com.epayMall.pojo.RefundInfo;

public interface RefundInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RefundInfo record);

    int insertSelective(RefundInfo record);

    RefundInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RefundInfo record);

    int updateByPrimaryKey(RefundInfo record);
    
    RefundInfo selectByOrderNoAndUserId(@Param("orderNo")Long orderNo, @Param("userId")Integer userId);
}