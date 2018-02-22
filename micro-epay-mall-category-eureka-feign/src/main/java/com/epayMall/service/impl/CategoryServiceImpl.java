package com.epayMall.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epayMall.common.ServerResponse;
import com.epayMall.dao.CategoryMapper;
import com.epayMall.pojo.Category;
import com.epayMall.service.ICategoryService;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
	
	@Autowired
	private CategoryMapper categoryMapper;
	
	@Override
    public ServerResponse addCategory(String categoryName,Integer parentId){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorResReturnMsg("添加品类参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0){
            return ServerResponse.createBySucessResReturnMsg("添加品类成功");
        }
        return ServerResponse.createByErrorResReturnMsg("添加品类失败");
    }

	@Override
    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorResReturnMsg("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return ServerResponse.createBySucessResReturnMsg("更新品类名字成功");
        }
        return ServerResponse.createByErrorResReturnMsg("更新品类名字失败");
    }

	@Override
	public ServerResponse getParallelCategory(Integer categoryId) {
		ServerResponse response;
		if (categoryId == null) {
			response = ServerResponse.createByErrorResReturnMsg("未找到该品类");
			return response;
		}
		
		List<Category> categories = categoryMapper.selectByParentId(categoryId);
		if (CollectionUtils.isEmpty(categories)) {
			response = ServerResponse.createByErrorResReturnMsg("未找到该品类");
			return response;
		}
		
		response = ServerResponse.createBySucessResReturnData(categories);
		return response;
	}

	@Override
	public ServerResponse<List<Integer>> getRecursionCategoryId(Integer categoryId) {
		ServerResponse response;
		if (categoryId == null) {
			response = ServerResponse.createByErrorResReturnMsg("未找到该品类");
			return response;
		}
		
		Category category = categoryMapper.selectByPrimaryKey(categoryId);
		if (category == null) {
			response = ServerResponse.createByErrorResReturnMsg("未找到该品类");
			return response;
		}
		
		Set<Category> set = new HashSet<>();
		getRecursiveCategory(set, categoryId);
		
		List<Integer> categoryIds = new ArrayList<>();
		for (Category childernCategory : set) {
			categoryIds.add(childernCategory.getId());
		}
		categoryIds.add(categoryId);
		response = ServerResponse.createBySucessResReturnData(categoryIds);
		return response;
	}
	
	/**
	 * 递归查找所有的子节点
	 * @param set
	 * @param categoryId
	 */
	private void getRecursiveCategory(Set<Category> set, Integer categoryId){
		List<Category> categories = categoryMapper.selectByParentId(categoryId);
		
		for (Category category : categories) {
			set.add(category);
			getRecursiveCategory(set, category.getId());
		}
		
	}
	
	@Override
	public ServerResponse<Category> getCategoryByCategoryId(Integer categoryId) {
		Category category = categoryMapper.selectByPrimaryKey(categoryId);
		return ServerResponse.createBySucessResReturnData(category);
	}
	
}
