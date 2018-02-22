package com.epayMall.common;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * 可复用的服务器响应
 * @author crixus
 * @param <T>
 *
 */
@JsonSerialize(include = Inclusion.NON_NULL)
//用于对该对象bean进行json序列化，如果对象为null则忽略将该key-value
public class ServerResponse<T> implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int status;
	private String msg;
	private T data;
	
	public ServerResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	private ServerResponse(int status) {
		super();
		this.status = status;
	}
	private ServerResponse(int status, String msg) {
		super();
		this.status = status;
		this.msg = msg;
	}
	private ServerResponse(int status, String msg, T data) {
		super();
		this.status = status;
		this.msg = msg;
		this.data = data;
	}
	private ServerResponse(int status, T data) {
		super();
		this.status = status;
		this.data = data;
	}
	
	@JsonIgnore
	public boolean isSuccess(){
		return ResponseCodeEnum.SUCCESS.getStatus() == this.status;
	}
	
	public static <T> ServerResponse<T> createBySucessRes(){
		return new ServerResponse<T>(ResponseCodeEnum.SUCCESS.getStatus());
	}
	public static <T> ServerResponse<T> createBySucessResReturnMsg(String msg){
		return new ServerResponse<T>(ResponseCodeEnum.SUCCESS.getStatus(), msg);
	}
	public static <T> ServerResponse<T> createBySucessResReturnMsgAndData(String msg, T data){
		return new ServerResponse<T>(ResponseCodeEnum.SUCCESS.getStatus(), msg, data);
	}
	public static <T> ServerResponse<T> createBySucessResReturnData(T data){
		return new ServerResponse<T>(ResponseCodeEnum.SUCCESS.getStatus(), data);
	}
	
	public static <T> ServerResponse<T> createByErrorRes(){
		return new ServerResponse<T>(ResponseCodeEnum.ERROR.getStatus());
	}
	public static <T> ServerResponse<T> createByErrorResReturnMsg(String msg){
		return new ServerResponse<T>(ResponseCodeEnum.ERROR.getStatus(), msg);
	}
	public static <T> ServerResponse<T> createByOtherErrorRes(int status){
		return new ServerResponse<T>(status);
	}
	public static <T> ServerResponse<T> createByOtherErrorResReturnMsg(int status, String msg){
		return new ServerResponse<T>(status, msg);
	}
	public int getStatus() {
		return status;
	}
	public String getMsg() {
		return msg;
	}
	public T getData() {
		return data;
	}
	
}
