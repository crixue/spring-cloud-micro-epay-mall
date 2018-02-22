package com.epayMall.util;

import java.util.Random;

public class RandomGenerator {

	public static String sixRandomDigit(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			sb.append(new Random().nextInt(10));
		}
		return sb.toString();
	}
}
