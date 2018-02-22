package com.epayMall.service;

import java.util.List;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.Category;

public interface ICategoryService {

	ServerResponse addCategory(String categoryName, Integer parentId);

	ServerResponse updateCategoryName(Integer categoryId, String categoryName);
	
	ServerResponse getParallelCategory(Integer categoryId);
	
	ServerResponse<List<Integer>> getRecursionCategoryId(Integer categoryId);

	ServerResponse<Category> getCategoryByCategoryId(Integer categoryId);

}
