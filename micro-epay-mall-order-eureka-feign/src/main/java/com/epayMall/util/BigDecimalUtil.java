package com.epayMall.util;

import java.math.BigDecimal;


/**
 * @author crixus
 *
 */
public class BigDecimalUtil {
	private static final int DEFAULTSCALE = 2;

    private BigDecimalUtil(){

    }


    public static BigDecimal add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }


    public static BigDecimal mul(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, DEFAULTSCALE, BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数

        //除不尽的情况
    }
    
    public static BigDecimal setDefalutScale(BigDecimal b1){
    	return b1.setScale(DEFAULTSCALE, BigDecimal.ROUND_HALF_UP);
    }


	public static BigDecimal setDefalutScale(String str) {
		BigDecimal b = new BigDecimal(str);
		return setDefalutScale(b);
	}





}
