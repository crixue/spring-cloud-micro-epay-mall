package com.epayMall.dao;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.epayMall.pojo.OrderItem;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);
    
    List<OrderItem> selectByOrderNoAndUserId(@Param("userId")Integer userId, @Param("orderNo")Long orderNo);
    
    int branchInsert(List<OrderItem> orderItems);
}