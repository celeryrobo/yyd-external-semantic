package com.yyd.external.semantic.turing;

public class EncryptUtils {

	private static TuringAES getTuringAES(String key, String secret, String timestamp){
		String secretKey = secret + timestamp + key;
		String aesKey = TuringMd5.MD5(secretKey);
		TuringAES ma = new TuringAES(aesKey);
		return ma;
	}
	
	public static String encrypt(String data, String key, String secret, String timestamp){
		TuringAES ma = getTuringAES(key, secret, timestamp);
		return ma.encrypt(data);
	}
	
	public static String decrypt(String data, String key, String secret, String timestamp){
		TuringAES ma = getTuringAES(key, secret, timestamp);
		return ma.decrypt(data);
	}
	
}
