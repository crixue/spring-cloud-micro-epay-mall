package com.epayMall.service;

import java.util.Map;

import com.epayMall.common.ServerResponse;
import com.epayMall.pojo.User;

public interface IOrderSevice {
	/**
	 * 采用支付宝为当前当前用户的订单创建付款二维码，
	 * 并将该二维码存储到相应路径当中，
	 * 最后返回预下单的二维码图片路径
	 * @param user
	 * @param orderNo
	 * @param path
	 * @return
	 */
	<T> ServerResponse<T> tradePrecreateForAliPay(User user, Long orderNo, String path);
	
	/**
	 * 验证是否为我们的订单以及金额是否正确
	 * 验证是否为重复回调
	 * 对订单做更新操作
	 * @param paramMap
	 * @return
	 */
	ServerResponse aliPayCallback(Map<String, String> paramMap);
	
	/**
	 * 查询订单状态
	 * @param OrderNo
	 * @return
	 */
	ServerResponse queryOrderStatus(String OrderNo);
	
	/**
	 * ali支付退款
	 * @return
	 */
	<T> ServerResponse<T> aliPayTradeRefund(User user, String orderNo, String refundReason);
	
	/**
	 * ali网页支付
	 * @param orderNo
	 * @return
	 */
	<T> ServerResponse<T> wapPayForAliPay(User user, String orderNo);
	
 
}
