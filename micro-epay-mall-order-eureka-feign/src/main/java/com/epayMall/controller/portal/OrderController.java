package com.epayMall.controller.portal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.epayMall.common.Const;
import com.epayMall.common.ResponseCodeEnum;
import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;
import com.epayMall.service.IOrderSevice;

@Controller
@RequestMapping("/order/")
public class OrderController {
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	private static final String CONTROLLER = "OrderController";
	
	@Autowired
	private IOrderSevice iOrderSevice;

	/**
	 * 为用户预下单，并创建预下单二维码返回给前段
	 * @param session
	 * @param orderNo
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "trade_precreate_for_aliPay.do", method = RequestMethod.POST)
	@ResponseBody
	public <T> ServerResponse<T> tradePrecreateForAliPay(HttpSession session, String orderNo, HttpServletRequest request){
		logger.info("[{}]-[pay method]-[接受客户端请求]", CONTROLLER);
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
        	logger.info("[{}]-[pay method]-[当前用户未登录]", CONTROLLER);
            return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
            		ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
        
        //这样写有一定的问题，如果打成war包部署到web logic容器上的时候就不能正常获得路径，注意！
        String path = request.getSession().getServletContext().getRealPath("upload");
        ServerResponse<T> response = iOrderSevice.tradePrecreateForAliPay(user, Long.valueOf(orderNo), path);
        logger.info("[{}]-[pay method]-[为 {} 用户返回响应内容,响应描述为：{}]", CONTROLLER,
				user.getUsername(), response.getMsg());
		return response;
		
	}
	
	/**
	 * 支付宝异步回调，用于验证商户是否支付成功
	 * 问题：对于支付宝和自身业务验证不正确的我们应该返回给支付宝什么？
	 * @param request
	 * @return  支付成功返回success，失败或者其他情况failed
	 */
	@RequestMapping("alipay_callback.do")
	@ResponseBody
	public String aliPayCallback(HttpServletRequest request){
		logger.info("[{}]-[aliPayCallback method]-[接受客户端请求]", CONTROLLER);
		//获取支付宝的回调参数,加签
		Map<String, String> rsaCheckV2ParamMap = new HashMap<>();
		Map<String, String[]> paramsMap = request.getParameterMap();
		Iterator<String> iterator = paramsMap.keySet().iterator();
		while(iterator.hasNext()){
			String key = (String)iterator.next();
			String[] values = (String[]) paramsMap.get(key);
			StringBuffer valueStr = new StringBuffer();
			for (int i = 0; i < values.length; i++) {
				if (values.length-1 == 0) {
					valueStr.append(values[i]);
				} else {
					//注意，有多个值的时候采用,隔开
					valueStr.append(values[i]).append(",");
				}
			}
			rsaCheckV2ParamMap.put(key, valueStr.toString());
 			
		}
		
		//对支付宝传过来的信息做验签处理
		/*
		 * 有一个非常值得注意：
		 * AlipaySignature的加签返回参数仅仅除去了sign字段，未除去sign_type字段，因此我们必须自己除去该字段，才能验签通过！！！
		 * 坑！
		 */
		rsaCheckV2ParamMap.remove("sign_type");
		try {
			boolean checkAlipPaySignuature = AlipaySignature.rsaCheckV2(rsaCheckV2ParamMap, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
			if (!checkAlipPaySignuature) {
				//这里应该返回failed字符串嘛？
				logger.warn("[{}]-[alipay_callback method]-[支付宝回调响应描述为：{}]", CONTROLLER,
						"支付宝回调验签失败，存在恶意调用接口嫌疑！");
				return Const.AlipayCallback.RESPONSE_FAILED;
			}
		} catch (AlipayApiException e) {
			logger.error("验签处理异常，异常原因：", e);
			e.printStackTrace();
			
		}
		
		//对我们自己的订单内容做校验,是否为重复回调，以及具体业务逻辑处理
		ServerResponse response = iOrderSevice.aliPayCallback(rsaCheckV2ParamMap);
		if (response.isSuccess()) {
			logger.info("[{}]-[alipay_callback method]-[支付宝回调成功]", CONTROLLER);
			return Const.AlipayCallback.RESPONSE_SUCCESS;
		}
		
        logger.info("[{}]-[alipay_callback method]-[支付宝回调响应描述为：{}]", CONTROLLER, response.getMsg());
		return Const.AlipayCallback.RESPONSE_FAILED;
		
	}
	
	/**
	 * 查询订单状态
	 * @param session
	 * @param orderNo
	 * @return
	 */
	@RequestMapping("query_order_pay_status.do")
	@ResponseBody
	public <T> ServerResponse<T> queryOrderStatus(HttpSession session, String orderNo){
		logger.info("[{}]-[queryOrderStatus method]-[接受客户端请求]", CONTROLLER);
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
        	logger.info("[{}]-[queryOrderStatus method]-[当前用户未登录]", CONTROLLER);
            return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
            		ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
		
        ServerResponse response = iOrderSevice.queryOrderStatus(orderNo);
        logger.info("[{}]-[queryOrderStatus method]-[查询订单是否已支付的响应描述为：{}]", CONTROLLER, response.getData().toString());
		return response;
		
	}
	
	/**
	 * ali交易退货
	 * @param session
	 * @param orderNo
	 * @return
	 */
	@RequestMapping("alipay_trade_refund.do")
	@ResponseBody
	public <T> ServerResponse<T> alipayTradeRefund(HttpSession session, String orderNo, String refundReason){
		logger.info("[{}]-[alipayTradeRefund method]-[接受客户端请求]", CONTROLLER);
		ServerResponse response;
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
        	logger.info("[{}]-[alipayTradeRefund method]-[当前用户未登录]", CONTROLLER);
            return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
            		ResponseCodeEnum.NEED_LOGIN.getDesc());
        }
        
        response = iOrderSevice.aliPayTradeRefund(user, orderNo, refundReason);
        if (!response.isSuccess()) {
			logger.info("[{}]-[alipayTradeRefund method]-[支付宝退款失败的响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
        logger.info("[{}]-[alipayTradeRefund method]-[支付宝退款成功的响应描述为：{}]", CONTROLLER);
		return response;
		
	}
	
	/**
	 * ali手机网站支付
	 * @param request
	 * @param session
	 * @param orderNo
	 * @return
	 */
	@RequestMapping("wap_pay_for_aliPay.do")
	@ResponseBody
	public <T> ServerResponse<T> wapPayForAliPay(HttpServletRequest request, HttpSession session, String orderNo){
		logger.info("[{}]-[wapPayForAliPay method]-[接受客户端请求]", CONTROLLER);
		ServerResponse response ;
		
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
//        if(user ==null){
//        	logger.info("[{}]-[wapPayForAliPay method]-[当前用户未登录]", CONTROLLER);
//            return ServerResponse.createByOtherErrorResReturnMsg(ResponseCodeEnum.NEED_LOGIN.getStatus(),
//            		ResponseCodeEnum.NEED_LOGIN.getDesc());
//        }
		
		//仅仅为了方便测试
		User user = new User();
		user.setId(1);
        
		response = iOrderSevice.wapPayForAliPay(user, orderNo);
		if (!response.isSuccess()) {
			logger.info("[{}]-[wapPayForAliPay method]-[手机网站支付失败的响应描述为：{}]", CONTROLLER, response.getMsg());
			return response;
		}
		logger.info("[{}]-[wapPayForAliPay method]-[手机网站支付成功]", CONTROLLER);
		return response;
		
	}
	
}
