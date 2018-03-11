package com.epayMall.dao;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;

import com.epayMall.pojo.Order;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
    
    Order selectByUserIdAndOrderNo(@Param("userId")Integer userId, @Param("orderNo")Long orderNo);
    
    Order selectByOutOrderNoAndMoney(@Param("outTradeNo")Long outTradeNo, @Param("totalAmount")BigDecimal totalAmount);
    
    Order selectByOrderNo(Long orderNo);
}