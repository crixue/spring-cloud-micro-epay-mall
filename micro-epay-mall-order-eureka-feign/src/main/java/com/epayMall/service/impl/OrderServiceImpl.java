package com.epayMall.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeRefundRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.model.result.AlipayF2FRefundResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.epayMall.common.Const;
import com.epayMall.common.ServerResponse;
import com.epayMall.dao.CartMapper;
import com.epayMall.dao.OrderItemMapper;
import com.epayMall.dao.OrderMapper;
import com.epayMall.dao.PayInfoMapper;
import com.epayMall.dao.ProductMapper;
import com.epayMall.dao.RefundInfoMapper;
import com.epayMall.dao.ShippingMapper;
import com.epayMall.pojo.Cart;
import com.epayMall.pojo.Order;
import com.epayMall.pojo.OrderItem;
import com.epayMall.pojo.PayInfo;
import com.epayMall.pojo.Product;
import com.epayMall.pojo.RefundInfo;
import com.epayMall.pojo.Shipping;
import com.epayMall.pojo.User;
import com.epayMall.service.IOrderSevice;
import com.epayMall.util.BigDecimalUtil;
import com.epayMall.util.Constants;
import com.epayMall.util.DateTimeUtil;
import com.epayMall.util.FTPUtil;
import com.epayMall.vo.OrderItemVo;
import com.epayMall.vo.OrderVo;
import com.epayMall.vo.ShippingVo;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@Service("iOrderSevice")
public class OrderServiceImpl implements IOrderSevice {

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private OrderItemMapper orderItemMapper;
	@Autowired
	private PayInfoMapper payInfoMapper;
	@Autowired
	private RefundInfoMapper refundInfoMapper;
	@Autowired
	private CartMapper cartMapper;
	@Autowired
	private ProductMapper productMapper;
	@Autowired
	private ShippingMapper shippingMapper;

	// 支付宝当面付2.0服务
	private static AlipayTradeService tradeService;

	static {
		/**
		 * 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
		 * Configs会读取classpath下的zfbinfo.properties文件配置信息，
		 * 如果找不到该文件则确认该文件是否在classpath目录
		 */
		Configs.init("zfbinfo.properties");

		/**
		 * 使用Configs提供的默认参数 AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
		 */
		tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

	}
	
	public ServerResponse createOrder(Integer userId, Integer shippingId){
		//从购物车中获取被勾选的数据
		List<Cart> carts = cartMapper.selectCheckedCartByUserId(userId);
		//计算这个订单的总价
		Multimap<Integer, Product> checkedProMutliMap = getCheckedProductAndQuanality(carts);
		BigDecimal payment = getCartTotalPrice(checkedProMutliMap);
		//生成订单
		ServerResponse<Order> orderRes = assembleAndCreateOrder(userId, shippingId, payment);
		if (!orderRes.isSuccess()) {
			return orderRes;
		}
		Order order = orderRes.getData();
		//生成订单详情
		ServerResponse<List<OrderItem>> orderItemsRes = assembleAndCreateOrderItem(order, checkedProMutliMap);
		if (!orderItemsRes.isSuccess()) {
			return orderItemsRes;
		}
		List<OrderItem> orderItems = orderItemsRes.getData();
		
		//成功生成订单和订单详情后清空被选择的购物车
		ServerResponse cleanRes = cleanCart(carts, userId);
		if (!cleanRes.isSuccess()) {
			return cleanRes;
		}
		//减少我们的产品库存
		ServerResponse reduceRes = reduceOurStock(checkedProMutliMap);
		if (!reduceRes.isSuccess()) {
			return reduceRes;
		}
		//返回前段定义好的vo类
		List<OrderItemVo> orderItemVos = assembleOrderItemVo(orderItems);
		ShippingVo shippingVo = assembleShippingVo(shippingId);
		
		OrderVo orderVo = assembleOrderVo(order, orderItemVos, shippingVo, shippingId);
		return ServerResponse.createBySucessResReturnData(orderVo);
		
	}
	
	private OrderVo assembleOrderVo(Order order, List<OrderItemVo> orderItemVos, ShippingVo shippingVo, Integer shippingId){
		OrderVo orderVo = new OrderVo();
		
		BeanUtils.copyProperties(order, orderVo);
		orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
		orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
		orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
		orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
		orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
		orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
		orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
		orderVo.setReceiverName(shippingVo.getReceiverName());
		orderVo.setShippingVo(shippingVo);
		orderVo.setOrderItemVoList(orderItemVos);
		orderVo.setImageHost(Constants.getProperty("ftp.server.http.prefix"));
		
		return orderVo;
	}
	
	private List<OrderItemVo> assembleOrderItemVo(List<OrderItem> orderItems){
		List<OrderItemVo> orderItemVos = new ArrayList<>();
		for (OrderItem orderItem : orderItems) {
			OrderItemVo orderItemVo = new OrderItemVo();
			BeanUtils.copyProperties(orderItem, orderItemVo);
			orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
			orderItemVos.add(orderItemVo);
		}
		return orderItemVos;
	}
	
	private ShippingVo assembleShippingVo(Integer shippingId){
		Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
		ShippingVo shippingVo = new ShippingVo();
		BeanUtils.copyProperties(shipping, shippingVo);
		return shippingVo;
	}
	
	private ServerResponse reduceOurStock(Multimap<Integer, Product> checkedProMutliMap){
		Collection<Entry<Integer, Product>> entries = checkedProMutliMap.entries();
		for (Entry<Integer, Product> e : entries) {
			Product product = e.getValue();
			product.setStock(product.getStock()-e.getKey());
			int row = productMapper.updateByPrimaryKeySelective(product);
			if (row == 0) {
				return ServerResponse.createByErrorResReturnMsg("减少我们的库存失败");
			}
		}
		return ServerResponse.createBySucessRes();
	}
	
	private ServerResponse cleanCart(List<Cart> carts, Integer userId){
		List<Integer> productIds = new ArrayList<>();
		for (Cart cart : carts) {
			productIds.add(cart.getProductId());
		}
		int row = cartMapper.deleteProductByUserIdAndProductId(userId, productIds);
		if (row == 0) {
			return ServerResponse.createByErrorResReturnMsg("清空购物车失败");
		}
		return ServerResponse.createBySucessRes();
	}
	
	private ServerResponse<List<OrderItem>> assembleAndCreateOrderItem(Order order, Multimap<Integer, Product> checkedProMutliMap){
		List<OrderItem> orderItems = new ArrayList<>();
		Collection<Entry<Integer, Product>> entries = checkedProMutliMap.entries();
		for (Entry<Integer, Product> e : entries) {
			Product product = e.getValue();
			OrderItem orderItem = new OrderItem();
			orderItem.setUserId(order.getUserId());
			orderItem.setOrderNo(order.getOrderNo());
			orderItem.setProductId(product.getId());
			orderItem.setProductImage(product.getMainImage());
			orderItem.setProductName(product.getName());
			orderItem.setCurrentUnitPrice(product.getPrice());
			orderItem.setQuantity(e.getKey());
			orderItem.setTotalPrice(BigDecimalUtil.mul(e.getKey().doubleValue(), product.getPrice().doubleValue()));
			orderItems.add(orderItem);
		}
		
		int row = orderItemMapper.branchInsert(orderItems);
		if (row == 0) {
			return ServerResponse.createByErrorResReturnMsg("创建订单详情失败");
		}
		return ServerResponse.createBySucessResReturnData(orderItems);
		
	}
	
	private ServerResponse<Order> assembleAndCreateOrder(Integer userId, Integer shippingId, BigDecimal payment){
		Order order = new Order();
		order.setOrderNo(getOrderNo());
		order.setPayment(payment);
		order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
		order.setPostage(0);
		order.setShippingId(shippingId);
		order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
		order.setUserId(userId);
		
		int row = orderMapper.insert(order);
		if (row == 0) {
			return ServerResponse.createByErrorResReturnMsg("创建订单失败");
		}
		
		return ServerResponse.createBySucessResReturnData(order);
	}
	
	private Long getOrderNo(){
		long curTime = DateTime.now().getMillis();
		return curTime+new Random().nextInt(10);
	}
	
	private BigDecimal getCartTotalPrice(Multimap<Integer, Product> multimap){
		BigDecimal payment = new BigDecimal("0");
		Collection<Entry<Integer, Product>> entries = multimap.entries();
		for (Entry<Integer, Product> entry : entries) {
			payment = payment.add(BigDecimalUtil.mul(entry.getKey().doubleValue(), entry.getValue().getPrice().doubleValue()));
		}
		return payment;
	}
	
	/**
	 * key为被勾选的产品的数量，value为对应的产品的list集合
	 * @param carts
	 * @return
	 */
	private Multimap<Integer, Product> getCheckedProductAndQuanality(List<Cart> carts){
		Multimap<Integer, Product> multimap = ArrayListMultimap.create();
		for (Cart cart : carts) {
			Product product = productMapper.selectByPrimaryKey(cart.getProductId());
			multimap.put(cart.getQuantity(), product);
		}
		return multimap;
	}
	

	@Override
	public <T> ServerResponse<T> tradePrecreateForAliPay(User user, Long orderNo, String path) {
		ServerResponse response;

		// 查询该商户的该笔订单是否存在
		Order order = orderMapper.selectByUserIdAndOrderNo(user.getId(), orderNo);
		if (order == null) {
			response = ServerResponse.createByErrorResReturnMsg("不存在该笔订单号，请重新下单");
			return response;
		}

		// 获取该笔订单的详细信息，采用支付宝生成相对应的二维码，并返回我们该图片的url
		// (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
		// 需保证商户系统端不能重复，建议通过数据库sequence生成，
		String outTradeNo = orderNo.toString();

		// (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
		String subject = "当面付扫码消费--" + orderNo;

		// (必填) 订单总金额，单位为元，不能超过1亿元
		// 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
		String totalAmount = order.getPayment().toString();

		// (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
		// 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
		String undiscountableAmount = "0";

		// 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
		// 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
		String sellerId = "";

		// 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
		String body = new StringBuilder().append("订单号：").append(outTradeNo).append("共消费：").append(totalAmount)
				.append("元").toString();

		// 商户操作员编号，添加此参数可以为商户操作员做销售统计
		String operatorId = "test_operator_id";

		// (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
		String storeId = "test_store_id";

		// 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
		ExtendParams extendParams = new ExtendParams();
		extendParams.setSysServiceProviderId("2088100200300400500");

		// 支付超时，定义为120分钟
		String timeoutExpress = "120m";

		List<OrderItem> orderItems = orderItemMapper.selectByOrderNoAndUserId(user.getId(), orderNo);
		// 商品明细列表，需填写购买商品详细信息，
		List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
		for (OrderItem orderItem : orderItems) {
			String productId = orderItem.getProductId().toString();
			String productName = orderItem.getProductName();
			Long unitPrice = BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100))
					.longValue();
			Integer quanlity = orderItem.getQuantity();

			GoodsDetail good = GoodsDetail.newInstance(productId, productName, unitPrice, quanlity);
			goodsDetailList.add(good);

		}

		// 创建扫码支付请求builder，设置请求参数
		AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder().setSubject(subject)
				.setTotalAmount(totalAmount).setOutTradeNo(outTradeNo).setUndiscountableAmount(undiscountableAmount)
				.setSellerId(sellerId).setBody(body).setOperatorId(operatorId).setStoreId(storeId)
				.setExtendParams(extendParams).setTimeoutExpress(timeoutExpress)
				.setNotifyUrl(Constants.getProperty("alipay.callback.url"))// 支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
				.setGoodsDetailList(goodsDetailList);

		AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
		switch (result.getTradeStatus()) {
		case SUCCESS:
			logger.info("支付宝预下单成功: )");

			AlipayTradePrecreateResponse response1 = result.getResponse();
			dumpResponse(response1);

			// 先创建一个本地保存的文件夹
			File floder = new File(path);
			if (!floder.exists()) {
				floder.setWritable(true);
				floder.mkdirs();
			}

			// 在制定路径下生成一个二维码
			String qrcodeFileName = String.format("qr-%s.png", response1.getOutTradeNo());
			String qrPath = path + File.separator + qrcodeFileName;
			logger.info("qrcode is saved in local file that path is :" + qrPath);
			ZxingUtils.getQRCodeImge(response1.getQrCode(), 256, qrPath);

			// 将二维码图片上传到ftp服务器上
			File qrcodeImg = new File(path, qrcodeFileName);
			List<File> uploadFiles = new ArrayList<>();
			uploadFiles.add(qrcodeImg);
			try {
				FTPUtil.uploadFile(uploadFiles);
			} catch (IOException e) {
				logger.error("上传文件失败，失败原因：", e);
				e.printStackTrace();
			}

			// 返回给前端我们的二维码url
			String qrcodeUrl = Constants.getProperty("ftp.server.http.prefix") + qrcodeFileName;
			Map<String, String> data = new HashMap<>();
			data.put("qrPath", qrcodeUrl);
			data.put("orderNo", orderNo.toString());
			response = ServerResponse.createBySucessResReturnData(data);

			return response;

		case FAILED:
			logger.error("支付宝预下单失败!!!");
			response = ServerResponse.createByErrorResReturnMsg("支付宝预下单失败!!!");
			return response;

		case UNKNOWN:
			logger.error("系统异常，预下单状态未知!!!");
			response = ServerResponse.createByErrorResReturnMsg("系统异常，预下单状态未知!!!");
			return response;

		default:
			logger.error("不支持的交易状态，交易返回异常!!!");
			response = ServerResponse.createByErrorResReturnMsg("不支持的交易状态，交易返回异常!!!");
			return response;
		}

	}

	// 简单打印应答
	private void dumpResponse(AlipayResponse response) {
		if (response != null) {
			logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
			if (StringUtils.isNotEmpty(response.getSubCode())) {
				logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(), response.getSubMsg()));
			}
			logger.info("body:" + response.getBody());
		}
	}

	@Override
	public ServerResponse aliPayCallback(Map<String, String> paramMap) {
		ServerResponse response;
		// 订单合法性校验
		String outTradeNo = paramMap.get("out_trade_no");
		String totalAmount = paramMap.get("total_amount");
		Order order = orderMapper.selectByOutOrderNoAndMoney(Long.valueOf(outTradeNo),
				BigDecimalUtil.setDefalutScale(totalAmount));
		if (order == null) {
			response = ServerResponse.createByErrorResReturnMsg("非本商城订单号或者支付金额不正确");
			return response;
		}

		// 支付宝重复回调校验
		if (Const.OrderStatusEnum.PAID.getCode() <= order.getStatus()) {
			response = ServerResponse.createBySucessResReturnMsg("支付宝重复调用");
			return response;
		}

		// 更新订单信息和支付信息
		String tradeStatus = paramMap.get("trade_status");
		if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
			order.setStatus(Const.OrderStatusEnum.PAID.getCode());
			order.setPaymentTime(DateTimeUtil.now());
			orderMapper.updateByPrimaryKeySelective(order);
		}

		PayInfo payInfo = new PayInfo();
		payInfo.setUserId(order.getUserId());
		payInfo.setCreateTime(DateTimeUtil.strToDate(paramMap.get("gmt_payment")));
		payInfo.setUpdateTime(DateTimeUtil.strToDate(paramMap.get("gmt_payment")));
		payInfo.setOrderNo(order.getOrderNo());
		payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
		payInfo.setPlatformStatus(tradeStatus);
		payInfo.setPlatformNumber(paramMap.get("trade_no"));
		payInfoMapper.insert(payInfo);

		response = ServerResponse.createBySucessRes();

		return response;
	}

	@Override
	public ServerResponse queryOrderStatus(String OrderNo) {
		ServerResponse response;

		Order order = orderMapper.selectByOrderNo(Long.valueOf(OrderNo));
		if (order == null) {
			response = ServerResponse.createByErrorResReturnMsg("该用户并没有该订单,查询无效");
		}

		if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
			response = ServerResponse.createBySucessResReturnData(true);
			return response;
		}
		response = ServerResponse.createBySucessResReturnData(false);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ServerResponse<T> aliPayTradeRefund(User user, String orderNo, String refundReason) {
		ServerResponse response ;
		
		// 查询该商户的该笔订单是否存在
		Order order = orderMapper.selectByUserIdAndOrderNo(user.getId(), Long.valueOf(orderNo));
		if (order == null) {
			response = ServerResponse.createByErrorResReturnMsg("不存在该笔订单号，不可退款");
			return response;
		}
		
		//验证该笔交易是否已经退款
		RefundInfo refundInfoQuery = refundInfoMapper.selectByOrderNoAndUserId(Long.valueOf(orderNo), user.getId());
		if (refundInfoQuery != null && Const.SUCCESS.equals(refundInfoQuery.getRefundStatus())) {
			response = ServerResponse.createByErrorResReturnMsg("已经退款，不可再次退款");
			return response;
		}
//		if (Const.OrderStatusEnum.REFUND.getCode() == order.getStatus()) {
//			response = ServerResponse.createByErrorResReturnMsg("已经退款，不可再次退款");
//			return response;
//		}
		
        // (必填) 退款金额，该金额必须小于等于订单的支付金额，单位为元
        String refundAmount = order.getPayment().toString();

        // (可选，需要支持重复退货时必填) 商户退款请求号，相同支付宝交易号下的不同退款请求号对应同一笔交易的不同退款申请，
        // 对于相同支付宝交易号下多笔相同商户退款请求号的退款交易，支付宝只会进行一次退款
        String outRequestNo = "";

        // (必填) 商户门店编号，退款情况下可以为商家后台提供退款权限判定和统计等作用，详询支付宝技术支持
        String storeId = "test_store_id";
        
        if (StringUtils.isBlank(refundReason)) {
			refundReason = "其他原因";
		}

        // 创建退款请求builder，设置请求参数
        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder()
            .setOutTradeNo(orderNo).setRefundAmount(refundAmount).setRefundReason(refundReason)
            .setOutRequestNo(outRequestNo).setStoreId(storeId);

        AlipayF2FRefundResult result = tradeService.tradeRefund(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝退款成功: )");
                order.setStatus(Const.OrderStatusEnum.REFUND.getCode());
                order.setUpdateTime(DateTimeUtil.now());
                orderMapper.updateByPrimaryKeySelective(order);
                
                AlipayTradeRefundResponse alipayTradeRefundResponse = result.getResponse();
                RefundInfo refundInfo = new RefundInfo();
                refundInfo.setOrderNo(Long.valueOf(orderNo));
                refundInfo.setBuyLoginId(alipayTradeRefundResponse.getBuyerLogonId());
                refundInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
                refundInfo.setPlatformNumber(alipayTradeRefundResponse.getTradeNo());
                refundInfo.setRefundFee(BigDecimalUtil.setDefalutScale(alipayTradeRefundResponse.getRefundFee()));
                if (Const.AliRefundStatus.SUCCESS.getRefundChange().equals(alipayTradeRefundResponse.getFundChange())) {
                	refundInfo.setRefundStatus(Const.AliRefundStatus.SUCCESS.getVaule());
				} else {
					refundInfo.setRefundStatus(Const.AliRefundStatus.FAIL.getVaule());
				}
                refundInfo.setPlatformUserId(alipayTradeRefundResponse.getBuyerUserId());
                refundInfo.setUserId(user.getId());
                refundInfo.setRefundReason(refundReason);
                refundInfo.setRefundDetailDesc(JSON.toJSONString(alipayTradeRefundResponse.getRefundDetailItemList()));
                refundInfoMapper.insert(refundInfo);
                response = ServerResponse.createBySucessRes();
                return response;

            case FAILED:
                AlipayTradeRefundResponse alipayTradeRefundResponse1 = result.getResponse();
                RefundInfo refundInfo1 = new RefundInfo();
                refundInfo1.setOrderNo(Long.valueOf(orderNo));
                refundInfo1.setBuyLoginId(alipayTradeRefundResponse1.getBuyerLogonId());
                refundInfo1.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
                refundInfo1.setPlatformNumber(alipayTradeRefundResponse1.getTradeNo());
                refundInfo1.setRefundFee(BigDecimalUtil.setDefalutScale(alipayTradeRefundResponse1.getRefundFee()));
				refundInfo1.setRefundStatus(Const.AliRefundStatus.FAIL.getVaule());
                refundInfo1.setPlatformUserId(alipayTradeRefundResponse1.getBuyerUserId());
                refundInfo1.setUserId(user.getId());
                refundInfo1.setRefundDetailDesc(alipayTradeRefundResponse1.getSubMsg());
                refundInfoMapper.insert(refundInfo1);
            	logger.error("支付宝退款失败!!!");
            	response = ServerResponse.createByErrorResReturnMsg("支付宝退款失败!!!");
            	return response;

            case UNKNOWN:
                AlipayTradeRefundResponse alipayTradeRefundResponse11 = result.getResponse();
                RefundInfo refundInfo11 = new RefundInfo();
                refundInfo11.setOrderNo(Long.valueOf(orderNo));
                refundInfo11.setBuyLoginId(alipayTradeRefundResponse11.getBuyerLogonId());
                refundInfo11.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
                refundInfo11.setPlatformNumber(alipayTradeRefundResponse11.getTradeNo());
                refundInfo11.setRefundFee(BigDecimalUtil.setDefalutScale(alipayTradeRefundResponse11.getRefundFee()));
				refundInfo11.setRefundStatus(Const.AliRefundStatus.UNKNOWN.getVaule());
                refundInfo11.setPlatformUserId(alipayTradeRefundResponse11.getSubMsg());
                refundInfo11.setUserId(user.getId());
                refundInfo11.setRefundDetailDesc("系统异常，订单退款状态未知!!!");
                refundInfoMapper.insert(refundInfo11);
            	logger.error("系统异常，订单退款状态未知!!!");
            	response = ServerResponse.createByErrorResReturnMsg("系统异常，订单退款状态未知!!!");
            	return response;

            default:
            	logger.error("不支持的交易状态，交易返回异常!!!");
            	response = ServerResponse.createByErrorResReturnMsg("不支持的交易状态，交易返回异常!!!");
            	return response;
        }
	}

	@Override
	public <T> ServerResponse<T> wapPayForAliPay(User user, String orderNo) {
		ServerResponse response = null;

		// 查询该商户的该笔订单是否存在
		Order order = orderMapper.selectByUserIdAndOrderNo(user.getId(), Long.valueOf(orderNo));
		if (order == null) {
			response = ServerResponse.createByErrorResReturnMsg("不存在该笔订单号，请重新下单");
			return response;
		}

		String subject = String.format("手机网站支付商品--%s", orderNo);
		String total_amount = order.getPayment().toString();
		String body = String.format("购买商品%s,共%s元", orderNo, total_amount);
		String timeout_express = "2m";
		// 销售产品码 必填
		String product_code = "QUICK_WAP_PAY";
		/**********************/
		// SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
		// 调用RSA签名方式
		AlipayClient client = new DefaultAlipayClient(Configs.getOpenApiDomain(), Configs.getAppid(),
				Configs.getPrivateKey(), Constants.getProperty("FORMAT"), Constants.getProperty("CHARSET"),
				Configs.getAlipayPublicKey(), Configs.getSignType());
		AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();

		// 封装请求支付信息
		AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
		model.setOutTradeNo(orderNo);
		model.setSubject(subject);
		model.setTotalAmount(total_amount);
		model.setBody(body);
		model.setTimeoutExpress(timeout_express);
		model.setProductCode(product_code);
		alipay_request.setBizModel(model);
		// 设置异步通知地址
		alipay_request.setNotifyUrl(Constants.getProperty("notify_url"));
		// 设置同步地址
		alipay_request.setReturnUrl(Constants.getProperty("return_url"));

		// form表单生产
		String form = "";
		try {
			// 调用SDK生成表单
			form = client.pageExecute(alipay_request).getBody();
			response = ServerResponse.createBySucessResReturnData(form);
		} catch (AlipayApiException e) {
			response = ServerResponse.createByErrorResReturnMsg("手机支付下单异常");
			logger.error("手机支付下单异常", e);
			e.printStackTrace();
		}
		return response;
	}

}
