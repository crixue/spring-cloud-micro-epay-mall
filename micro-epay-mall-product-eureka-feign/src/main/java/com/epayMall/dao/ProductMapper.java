package com.epayMall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.epayMall.pojo.Product;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);
    
    Product selectByCategoryIdAndName(@Param("categoryId")Integer categoryId, @Param("productName")String productName);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);
    
    List<Product> selectList();
    
    List<Product> selectProducByNameAndId(@Param("productName")String productName, @Param("productId")Integer productId);
    
    List<Product> selectByNameAndCategoryIds(@Param("productName")String productName, @Param("categoryIds")List<Integer> categoryIds);
}