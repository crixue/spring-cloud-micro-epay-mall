package com.epayMall.controller.backend;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.Category;
import com.epayMall.pojo.User;
import com.epayMall.service.ICategoryService;
import com.epayMall.service.IDataClientZuulForUser;

@RestController
@RequestMapping("/epay-mall/category/manage/")
public class MicroCategoryController {
	private static final Logger logger = LoggerFactory.getLogger(MicroCategoryController.class);
	private static final String CONTROLLER = "CategoryController";

	@Autowired
	private ICategoryService iCategoryService;
	@Autowired
	private IDataClientZuulForUser iDataClientZuulForUser;
	

	@RequestMapping("add_category.do")
	
	public ServerResponse addCategory(String categoryName,
			@RequestParam(value = "parentId", defaultValue = "0") int parentId) {
		ServerResponse response;
		logger.info("[{}]-[addCategory method]-[接受客户端请求]", CONTROLLER);
		// 增加我们处理分类的逻辑
		logger.info("[{}]-[addCategory method]-[管理员登录成功,可以正常增加品类节点]", CONTROLLER);
		response = iCategoryService.addCategory(categoryName, parentId);
		return response;

	}

	@RequestMapping("set_category_name.do")
	
	public ServerResponse setCategoryName(Integer categoryId, String categoryName) {
		ServerResponse response;
		logger.info("[{}]-[setCategoryName method]-[接受客户端请求]", CONTROLLER);

		// 更新categoryName
		response = iCategoryService.updateCategoryName(categoryId, categoryName);
		logger.info("[{}]-[setCategoryName method]-[管理员登录成功,可以正常更新品类节点]", CONTROLLER);
		return response;
	}

	@RequestMapping("get_category.do")
	
	public <T> ServerResponse<T> getParallelCategory(
			@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
		ServerResponse response;
		logger.info("[{}]-[getParallelCategory method]-[接受客户端请求]", CONTROLLER);

		response = iCategoryService.getParallelCategory(categoryId);
		if (!response.isSuccess()) {
			logger.info("[{}]-[getParallelCategory method]-[未找到该商品的类目，响应描述：]-{}", CONTROLLER, response.getMsg());
			return response;
		}

		logger.info("[{}]-[getParallelCategory method]-[找到该商品的类目]", CONTROLLER);
		return response;

	}

	/**
	 * 获取当前分类id及递归子节点categoryId
	 * 
	 * @param session
	 * @param categoryId
	 * @return
	 */
	@RequestMapping("get_deep_category.do")
	
	public ServerResponse<List<Integer>> getRecursionCategoryId(
			@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
		ServerResponse<List<Integer>> response;
		logger.info("[{}]-[addCategory method]-[接受客户端请求]", CONTROLLER);
		// 校验一下是否是管理员
		response = iCategoryService.getRecursionCategoryId(categoryId);
		return response;

	}
	
	@PostMapping("getCategoryByCategoryId")
	public ServerResponse<Category> getCategoryByCategoryId(Integer categoryId) {
		return iCategoryService.getCategoryByCategoryId(categoryId);
	}

}
