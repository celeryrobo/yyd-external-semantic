package com.yyd.external.semantic.turing;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yyd.external.util.Http;
import com.yyd.external.util.StringTool;

public class TuringUserIdService {
	public static String url = "http://api.turingos.cn/getuserid.do";
	
	/**
	 * 获取图灵设备id
	 * @param key           图灵机器人Apikey
	 * @param secret        图灵机器人Secret
	 * @param userDefineId  用户自定义的设备(或用户)id
	 * @param encryp        是否加密数据
	 * @return              图灵设备(或用户)id 或 null
	 */
	public static String getTuringUserId(String key,String secret,String userDefineId,boolean encrypt) {
		if(StringTool.isEmpty(key) || StringTool.isEmpty(secret) || StringTool.isEmpty(userDefineId)) {
			return null;
		}
				
		String data = "{\"uniqueId\":\""+userDefineId+"\"}";	
		String timestamp = String.valueOf(System.currentTimeMillis());
		if(encrypt) {
			data = EncryptUtils.encrypt(data, key, secret, timestamp);
		}
				
		JSONObject jsonParam = new JSONObject();
		try {
			jsonParam.put("key", key);
			jsonParam.put("timestamp", timestamp);
			jsonParam.put("data", data);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;		
		}
				
		try {
			Http http = new Http(url);
			http.setCharset("utf-8");
			http.setRequestProperty("Content-type", "application/json");
			http.setRequestProperty( "Accept", "application/json" );
			http.setRequestProperty( "Authorization", "token" );
			http.setReadTimeout(1200);
			
			String result = http.post(jsonParam.toString());
			if(StringTool.isEmpty(result)) {
				return null;
			}
						
			JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
			int ret = obj.get("ret").getAsInt();
			if(0 != ret) {
				return null;
			}
			
			String userid = obj.get("userid").getAsString();
			return userid;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	
		return null;
	}

}
