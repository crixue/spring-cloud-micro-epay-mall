package com.epayMall.controller.portal;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;
import com.epayMall.service.IProductSercive;

@Controller
@RequestMapping("/product/")
public class ProductController {

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	private static final String CONTROLLER = "ProductController";
	
	@Autowired
	private IProductSercive iProductSercive;
	
	@RequestMapping("detail.do")
	@ResponseBody
	public <T> ServerResponse<T> getProductDetail(HttpSession session, Integer productId){
		logger.info("[{}]-[getProductDetail method]-[接受客户端请求]", CONTROLLER);
    		ServerResponse response;
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
        	logger.info("[{}]-[getProductDetail method]-[当前用户未登录]", CONTROLLER);
        	response = ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),"用户未登录,请登录管理员");
            return response;
        }
        
        response = iProductSercive.getProductDetailForMcht(productId);
    	if (!response.isSuccess()) {
    		logger.info("[{}]-[getProductDetail method]-[获取产品detail失败，响应描述为：{}]", CONTROLLER, response.getMsg());
    		return response;
		}
    	
    	logger.info("[{}]-[getProductDetail method]-[获取产品detail成功，响应描述为：{}]", CONTROLLER);
        return response;
		
	}
	
	
	@RequestMapping("list.do")
	@ResponseBody
	public <T> ServerResponse<T> getProuductList(@RequestParam(value="categoryId", required=false)Integer categoryId, @RequestParam(value="keyword",required=false)String keyword, HttpSession session,
			@RequestParam(value="pageNum",defaultValue="1")Integer pageNum, @RequestParam(value="pageSize",defaultValue="10")Integer pageSize, @RequestParam(value="orderBy",defaultValue="")String orderBy){
		logger.info("[{}]-[getProuductList method]-[接受客户端请求]", CONTROLLER);
    	ServerResponse response;
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
        	logger.info("[{}]-[getProuductList method]-[当前用户未登录]", CONTROLLER);
        	response = ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),"用户未登录,请登录管理员");
            return response;
        }
        
        response = iProductSercive.searchProductForMcht(categoryId, keyword, pageNum, pageSize, orderBy);
    	if (!response.isSuccess()) {
    		logger.info("[{}]-[getProuductList method]-[搜索产品失败，响应描述为：{}]", CONTROLLER, response.getMsg());
    		return response;
		}
    	
    	logger.info("[{}]-[getProuductList method]-[搜索产品成功，响应描述为：{}]", CONTROLLER);
        return response;
		
	}
	
}
