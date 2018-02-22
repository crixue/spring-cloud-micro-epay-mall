package com.epayMall.controller.backend;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.Category;
import com.epayMall.pojo.User;
import com.epayMall.service.ICategoryService;
import com.epayMall.service.IDataClientZuulForUser;

@Controller
@RequestMapping("/epay-mall/manage/category/")
public class MicroCategoryController {
	private static final Logger logger = LoggerFactory.getLogger(MicroCategoryController.class);
	private static final String CONTROLLER = "CategoryController";

	@Autowired
	private ICategoryService iCategoryService;
	@Autowired
	private IDataClientZuulForUser iDataClientZuulForUser;
	
	/**
	 * 如果本地存在session缓存就直接使用，否则查看redis缓存中是否存在
	 * @return
	 */
	private Session getSession() {
		Subject currentSub = SecurityUtils.getSubject();
		Session session = currentSub.getSession();
		if(session.getAttribute(Const.CURRENT_USER) == null) {
			ServerResponse<Session> response = iDataClientZuulForUser.getUserSession(session);
			if(!response.isSuccess()) {
				return session;
			}
			session = response.getData();
		}
			
		return session;
	}

	@RequestMapping("add_category.do")
	@ResponseBody
	public ServerResponse addCategory(String categoryName,
			@RequestParam(value = "parentId", defaultValue = "0") int parentId) {
		ServerResponse response;
		logger.info("[{}]-[addCategory method]-[接受客户端请求]", CONTROLLER);
		Session session = getSession();
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[addCategory method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					ResponseCodeEnum.NEED_LOGIN.getDesc());
		}
		// 校验一下是否是管理员
		// 是管理员
		// 增加我们处理分类的逻辑
		logger.info("[{}]-[addCategory method]-[管理员登录成功,可以正常增加品类节点]", CONTROLLER);
		response = iCategoryService.addCategory(categoryName, parentId);
		return response;

	}

	@RequestMapping("set_category_name.do")
	@ResponseBody
	public ServerResponse setCategoryName(Integer categoryId, String categoryName) {
		ServerResponse response;
		logger.info("[{}]-[setCategoryName method]-[接受客户端请求]", CONTROLLER);
		Session session = getSession();
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[setCategoryName method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					ResponseCodeEnum.NEED_LOGIN.getDesc());
		}

		// 更新categoryName
		response = iCategoryService.updateCategoryName(categoryId, categoryName);
		logger.info("[{}]-[setCategoryName method]-[管理员登录成功,可以正常更新品类节点]", CONTROLLER);
		return response;
	}

	@RequestMapping("get_category.do")
	@ResponseBody
	public <T> ServerResponse<T> getParallelCategory(
			@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
		ServerResponse response;
		logger.info("[{}]-[getParallelCategory method]-[接受客户端请求]", CONTROLLER);
		Session session = getSession();
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[getParallelCategory method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					ResponseCodeEnum.NEED_LOGIN.getDesc());
		}

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
	@ResponseBody
	public ServerResponse<List<Integer>> getRecursionCategoryId(
			@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
		ServerResponse<List<Integer>> response;
		logger.info("[{}]-[addCategory method]-[接受客户端请求]", CONTROLLER);
		Session session = getSession();
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[addCategory method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					ResponseCodeEnum.NEED_LOGIN.getDesc());
		}
		// 校验一下是否是管理员
		response = iCategoryService.getRecursionCategoryId(categoryId);
		return response;

	}
	
	@PostMapping("getCategoryByCategoryId")
	public ServerResponse<Category> getCategoryByCategoryId(Integer categoryId) {
		return iCategoryService.getCategoryByCategoryId(categoryId);
	}

}
