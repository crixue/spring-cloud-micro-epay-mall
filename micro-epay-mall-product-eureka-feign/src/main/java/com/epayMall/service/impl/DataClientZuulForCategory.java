package com.epayMall.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.Category;
import com.epayMall.service.IDataClientZuulForCategory;

@Component("dataClientZuulForCategory")
public class DataClientZuulForCategory implements IDataClientZuulForCategory{

	private static final Logger logger = LoggerFactory.getLogger(DataClientZuulForCategory.class);
	
	@Override
	public ServerResponse<List<Integer>> getRecursionCategoryId(Integer categoryId) {
		logger.warn("[getRecursionCategoryId]-Category远程服务不可用，已熔断降级");
		List<Integer> tempList = new ArrayList<>();
		return ServerResponse.createBySucessResReturnData(tempList);
	}

	@Override
	public ServerResponse<Category> getCategoryByCategoryId(Integer categoryId) {
		logger.warn("[getCategoryByCategoryId]-Category远程服务不可用，已熔断降级");
		Category tempCategory = new Category();
		tempCategory.setId(10000);
		tempCategory.setParentId(0);
		tempCategory.setName("不可用");
		return ServerResponse.createBySucessResReturnData(tempCategory);
	}

}
