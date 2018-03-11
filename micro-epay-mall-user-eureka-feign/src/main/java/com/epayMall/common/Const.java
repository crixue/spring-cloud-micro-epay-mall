package com.epayMall.common;

import com.google.common.collect.Sets;

import java.util.Set;


/**
 * @author crixus
 *
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String PHONE = "phone";
    
    public static final String TOKEN_PREFIX = "token_";
    
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String UNKNOWN = "unknown";
    
    public static final int COOKIE_EXPIRE_TIME = 60 * 60 * 24 * 365;
    public static final Long REDIS_SESSION_EXPIRE_TIME = 60 * 30L;
    
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Cart{
        int CHECKED = 1;//即购物车选中状态
        int UN_CHECKED = 0;//购物车中未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public interface Role{
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    public enum ProductStatusEnum{
        ON_SALE(1,"在线");
        private String value;
        private int code;
        ProductStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }


    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭"),
        REFUND(70,"已退款");


        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("么有找到对应的枚举");
        }
    }
    public interface  AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }



    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum codeOf(int code){
        	for(PaymentTypeEnum paymentTypeEnum: values()){
        		if (code == paymentTypeEnum.getCode()) {
					return paymentTypeEnum;
				}
        	}
        	throw new RuntimeException("没有找到对应的枚举类");
        }

    }

    public enum AliRefundStatus{
    	SUCCESS("Y", Const.SUCCESS),
    	FAIL("N", Const.FAIL),
    	UNKNOWN("UNKNOWN", Const.UNKNOWN);
    	
    	private String refundChange;
    	private String vaule;
		public String getRefundChange() {
			return refundChange;
		}
		public void setRefundChange(String refundChange) {
			this.refundChange = refundChange;
		}
		public String getVaule() {
			return vaule;
		}
		public void setVaule(String vaule) {
			this.vaule = vaule;
		}
		private AliRefundStatus(String refundChange, String vaule) {
			this.refundChange = refundChange;
			this.vaule = vaule;
		}
    	
    }

    public enum WxRefundStatus{
    	SUCCESS("SUCCESS", Const.SUCCESS),
    	FAIL("FAIL", Const.FAIL),
    	UNKNOWN("UNKNOWN", Const.UNKNOWN);
    	
    	private String resultCode;
    	private String value;
		public String getResultCode() {
			return resultCode;
		}
		public void setResultCode(String resultCode) {
			this.resultCode = resultCode;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		private WxRefundStatus(String resultCode, String value) {
			this.resultCode = resultCode;
			this.value = value;
		}
    	
    	
    }


}
