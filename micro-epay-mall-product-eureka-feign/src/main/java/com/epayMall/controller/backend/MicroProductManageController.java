package com.epayMall.controller.backend;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.Product;
import com.epayMall.pojo.User;
import com.epayMall.service.IDataClientZuulForUser;
import com.epayMall.service.IFileService;
import com.epayMall.service.IProductSercive;

@Controller
@RequestMapping("/epay-mall/manage/product/")
public class MicroProductManageController {
	private static final Logger logger = LoggerFactory.getLogger(MicroProductManageController.class);
	private static final String CONTROLLER = "ProductManageController";

	@Autowired
	private IProductSercive iProductService;
	@Autowired
	private IFileService iFileService;
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

	/**
	 * 新增OR更新产品
	 * 
	 * @param session
	 * @param product
	 * @return
	 */
	@RequestMapping("save.do")
	@ResponseBody
	public ServerResponse productSave(Product product) {
		logger.info("[{}]-[productSave method]-[接受客户端请求]", CONTROLLER);
		Session session = getSession();
		ServerResponse response;
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[productSave method]-[当前用户未登录]", CONTROLLER);
			response = ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					"用户未登录,请登录管理员");
			return response;
		}
		// 填充我们增加产品的业务逻辑
		response = iProductService.saveOrUpdateProduct(product);
		if (!response.isSuccess()) {
			logger.info("[{}]-[productSave method]-[新增OR更新产品失败，响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}

		logger.info("[{}]-[productSave method]-[新增OR更新产品成功，响应描述为：{}]", CONTROLLER, response.getMsg());
		return response;
	}

	/**
	 * 产品上下架更新状态
	 * 
	 * @param session
	 * @param productId
	 * @param status
	 *            1-在售 2-下架 3-删除
	 * @return
	 */
	@RequestMapping("set_sale_status.do")
	@ResponseBody
	public ServerResponse setSaleStatus(Integer productId, Integer status) {
		ServerResponse response;
		logger.info("[{}]-[setSaleStatus method]-[接受客户端请求]", CONTROLLER);
		Session session = getSession();
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[setSaleStatus method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					"用户未登录,请登录管理员");

		}
		response = iProductService.setSaleStatus(productId, status);
		if (!response.isSuccess()) {
			logger.info("[{}]-[setSaleStatus method]-[产品上下架更新状态失败，响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}

		logger.info("[{}]-[setSaleStatus method]-[产品上下架更新状态成功]", CONTROLLER);
		return response;
	}

	/**
	 * 产品详情
	 * 
	 * @param session
	 * @param productId
	 * @return
	 */
	@RequestMapping("detail.do")
	@ResponseBody
	public ServerResponse getDetail(HttpSession session, Integer productId) {
		ServerResponse response;
		logger.info("[{}]-[getDetail method]-[接受客户端请求]", CONTROLLER);
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[getDetail method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					"用户未登录,请登录管理员");

		}

		response = iProductService.getProductDetail(productId);
		if (!response.isSuccess()) {
			logger.info("[{}]-[getDetail method]-[获取商品信息失败，响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		} else {
			logger.info("[{}]-[getDetail method]-[获取商品信息成功]", CONTROLLER);
			return response;
		}

	}

	/**
	 * 获得分页的产品
	 * 
	 * @param session
	 * @param productId
	 * @return
	 */
	@RequestMapping("list.do")
	@ResponseBody
	public ServerResponse getProductList(HttpSession session,
			@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		ServerResponse response;
		logger.info("[{}]-[getDetail method]-[接受客户端请求]", CONTROLLER);
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[getDetail method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					"用户未登录,请登录管理员");

		}

		response = iProductService.getProductList(pageNum, pageSize);
		if (!response.isSuccess()) {
			logger.info("[{}]-[getDetail method]-[获取商品信息失败，响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		} else {
			logger.info("[{}]-[getDetail method]-[获取商品信息成功]", CONTROLLER);
			return response;
		}

	}

	/**
	 * 产品搜索
	 * 
	 * @param session
	 * @param productName
	 * @param productId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("search.do")
	@ResponseBody
	public ServerResponse searchProducts(HttpSession session, String productName, Integer productId,
			@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		ServerResponse response;
		logger.info("[{}]-[searchProducts method]-[接受客户端请求]", CONTROLLER);
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[searchProducts method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					"用户未登录,请登录管理员");

		}

		response = iProductService.searchProducts(productName, productId, pageNum, pageSize);
		if (!response.isSuccess()) {
			logger.info("[{}]-[searchProducts method]-[搜索产品失败，响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		} else {
			logger.info("[{}]-[searchProducts method]-[搜索产品成功]", CONTROLLER);
			return response;
		}

	}

	/**
	 * 上传产品的图片
	 * 
	 * @param session
	 * @param file
	 * @param request
	 * @return
	 */
	@RequestMapping("upload.do")
	@ResponseBody
	public ServerResponse uploadFile(HttpSession session, @RequestParam("upload_file") MultipartFile file,
			HttpServletRequest request) {
		ServerResponse response;
		logger.info("[{}]-[uploadFile method]-[接受客户端请求]", CONTROLLER);
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[uploadFile method]-[当前用户未登录]", CONTROLLER);
			return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
					"用户未登录,请登录管理员");

		}
		String path = request.getSession().getServletContext().getRealPath("upload");

		response = iFileService.uploadFile(file, path);
		if (!response.isSuccess()) {
			logger.info("[{}]-[uploadFile method]-[上传产品的图片失败，响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}

		logger.info("[{}]-[uploadFile method]-[上传产品的图片成功]", CONTROLLER);
		return response;
	}

	/**
	 * 富文本图片上传 simditor要求的上传的格式 { "success": true/false, "msg": "error message", #
	 * optional "file_path": "[real file path]" }
	 * 
	 * @param session
	 * @param file
	 * @param request
	 * @return
	 */
	@RequestMapping("richtext_img_upload.do")
	@ResponseBody
	public Map richtextImgUpload(HttpSession session, @RequestParam("upload_file") MultipartFile file,
			HttpServletRequest request) {
		ServerResponse response;
		logger.info("[{}]-[uploadFile method]-[接受客户端请求]", CONTROLLER);

		Map<String, Object> resMap = new HashMap<>();
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			logger.info("[{}]-[uploadFile method]-[当前用户未登录]", CONTROLLER);
			resMap.put("success", false);
			resMap.put("msg", "用户未登录,请登录管理员");
			return resMap;
		}

		String path = request.getSession().getServletContext().getRealPath("upload");

		response = iFileService.uploadFile(file, path);
		if (!response.isSuccess()) {
			logger.info("[{}]-[uploadFile method]-[上传产品的图片失败，响应描述为：{}]", CONTROLLER, response.getMsg());
			resMap.put("success", false);
			resMap.put("msg", response.getMsg());
			return resMap;
		}

		logger.info("[{}]-[uploadFile method]-[上传产品的图片成功]", CONTROLLER);
		Map<String, Object> uploadedMap = (Map<String, Object>) response.getData();
		resMap.put("success", true);
		resMap.put("msg", "上传成功");
		resMap.put("file_path", uploadedMap.get("url"));
		return resMap;

	}

}
