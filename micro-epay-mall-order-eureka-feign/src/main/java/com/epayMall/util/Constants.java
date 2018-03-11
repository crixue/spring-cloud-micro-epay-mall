package com.epayMall.util;


import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;


/**
 * @author crixus
 * 读取constants.properties的配置文件
 *
 */
public class Constants {

	private static String CONSTANTS_FILE = "constants";

	private static ResourceBundle BUNDLE = ResourceBundle
			.getBundle(CONSTANTS_FILE);

	/**
	 * 获得constant.properties里的参数
	 * 
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		String value = BUNDLE.getString(key.trim());
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return BUNDLE.getString(key).trim();
	}
	
	public static String getProperty(String key, String defaultValue){
		String value = BUNDLE.getString(key.trim());
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		return value.trim();
	}
	
	
}