package com.epayMall.common;

/**
 * 响应码枚举类
 * @author crixus
 *
 */
public enum ResponseCodeEnum {
	
	SUCCESS(0, "SUCCESS"),
	ERROR(1, "ERROR"),
    NEED_LOGIN(10, "NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT");
	
	private int status;
	private String desc;
	
	private ResponseCodeEnum(int status, String desc) {
		this.status = status;
		this.desc = desc;
	}

	public int getStatus() {
		return status;
	}

	public String getDesc() {
		return desc;
	}
	
	
}
