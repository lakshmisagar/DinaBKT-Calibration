package com.asu.dinabkt.utils;

public class Operations {

	public static Double addDouble(Double value1, Double value2) {
		return value1+value2;
	}

	public static Double substractDouble(Double value1, Double value2) {
		return value1-value2;
	}
	
	public static Double multiplyDouble(Double value1, Double value2) {
		return value1*value2;
	}

	public static Double divideDouble(Double value1, Double value2) {
		if(value1==(double)0) return (double)0;
		return value1/value2;
	}
	
}
