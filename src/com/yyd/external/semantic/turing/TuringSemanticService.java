package com.yyd.external.semantic.turing;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yyd.external.semantic.ExternalCommonBean;
import com.yyd.external.semantic.ExternalSemanticError;
import com.yyd.external.semantic.ExternalSemanticResult;
import com.yyd.external.semantic.ExternalSemanticService;
import com.yyd.external.semantic.resource.MusicResource;
import com.yyd.external.semantic.resource.PoetryResource;
import com.yyd.external.semantic.resource.StoryResource;
import com.yyd.external.semantic.ExternalSemanticResult.OperationEx;
import com.yyd.external.semantic.ExternalSemanticResult.ParamTypeEx;
import com.yyd.external.util.Http;
import com.yyd.external.util.StringTool;

public class TuringSemanticService implements ExternalSemanticService{
	public static final String url = "http://api.turingos.cn/turingosapi"; //接口地址
	
	@Override
	public ExternalSemanticResult handleSemantic(String text,Map<String,String> params)throws Exception{
		ExternalSemanticResult result = null;
		if(StringTool.isEmpty(text) || params == null) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_INPUT_PARAM_EMPTY);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INPUT_PARAM_EMPTY));			
			return result;
		}
		
		String key = params.get("key");
		String secret = params.get("secret");
		String userId = params.get("userId");
		String encrypt = params.get("encrypt");
		if(StringTool.isEmpty(key) || StringTool.isEmpty(secret) || StringTool.isEmpty(userId)) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM));			
			return result;
		}
		
		boolean bEncrypt = "true".equals(encrypt)?true:false;
		
		long start = System.currentTimeMillis();	
		result = getAnswer(key,secret,userId,text,bEncrypt);		
		long total = System.currentTimeMillis() - start;
		result.setTime(total);
				
		return result;
	}
	
	
	public ExternalSemanticResult getAnswer(String key,String secret,String userId,String question,boolean encrypt) {
		ExternalSemanticResult semanticResult = new ExternalSemanticResult();
		
		String data = "{\"perception\":{\"audition\":{\"text\":\"" + question+ "\"}},\"reqType\":-1,\"userInfo\":{\"key\":\""+key+"\",\"userId\":\""+userId+"\"}}";
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
			semanticResult.setRet(ExternalSemanticError.ERROR_SYSTEM_EXCEPTION);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SYSTEM_EXCEPTION));	
			return semanticResult;
		}
		
		
		try {
			Http http = new Http(url);
			http.setCharset("utf-8");
			http.setReadTimeout(1200);
			
			String result = http.post(jsonParam.toString());
			if(StringTool.isEmpty(result)) {
				semanticResult.setRet(ExternalSemanticError.ERROR_INVALID_RESULT_DATA);
				semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INVALID_RESULT_DATA));	
				return semanticResult;
			}
			semanticResult.setSrcResult(result);
			parseResult(result,semanticResult);			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			semanticResult.setRet(ExternalSemanticError.ERROR_SYSTEM_EXCEPTION);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SYSTEM_EXCEPTION));	
		}		
		
		return semanticResult;
	}
	
	
	private void parseResult(String result,ExternalSemanticResult semanticResult) {
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));	
			return;
		}
		
		JsonArray behaviorsArray = obj.get("behaviors").getAsJsonArray();
		if(null == behaviorsArray || behaviorsArray.size() <= 0) {
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));	
			return;
		}
		
		JsonObject behaviorObj = behaviorsArray.get(0).getAsJsonObject();
		
		//解析场景信息
		//TODO:暂时只取第一条语义信息		
		JsonObject intentObj = behaviorObj.get("intent").getAsJsonObject();
		if(null == intentObj)
		{
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));	
			return;
		}		
		//appKey 可能没有，太扯了吧
		String code = intentObj.get("code").getAsString();		
		if(TuringCode.isErrorCode(code)) {
			semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));	
			semanticResult.setSemanticRet(code);
			semanticResult.setSemanticMsg(TuringCode.getMsg(code));	
			return;
		}
		JsonElement serviceObject = intentObj.get("appKey");
		String service = null;
		if(null!= serviceObject) {
			service = serviceObject.getAsString();
		}
		else
		{
			service = TuringCode.getMsg(code);
		}
		semanticResult.setService(service);//
		semanticResult.setData(new ExternalCommonBean());
		
		//解析语义槽
		JsonElement parametersElem = intentObj.get("parameters");
		if(null != parametersElem) {
			parseParameters(code,parametersElem,semanticResult);
		}
		
		String operateState = parseString(intentObj,"operateState");
		if(!StringTool.isEmpty(operateState)) {
			Map<String,Object> slots = semanticResult.getSlots();
			if(null == slots) {
				slots = new HashMap<String,Object>();
				semanticResult.setSlots(slots);
			}
			
			slots.put("operateState", operateState);				
		}
		
		//解析回答结果
		//一些控制类的results数据为空，但存在该字段
		JsonArray resultArray = behaviorObj.get("results").getAsJsonArray();
		if(null != resultArray && resultArray.size() > 0) {
			JsonObject resultObject = resultArray.get(0).getAsJsonObject();
			String resultType = resultObject.get("resultType").getAsString();
			if("text".equalsIgnoreCase(resultType)) {
				JsonObject valueObject =resultObject.get("values").getAsJsonObject();
				String text = parseString(valueObject,"text");
				semanticResult.setAnswer(text);
			}
		}		
		semanticResult.getData().setText(semanticResult.getAnswer());
		
		//合成返回资源数据
		buildResource(code,operateState,semanticResult);
		
		//设置默认操作方式和返回数据类型
		semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
		semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));			
		if(semanticResult.getOperation() == null) {
			semanticResult.setOperation(OperationEx.SPEAK);
			semanticResult.setParamType(ParamTypeEx.T);
		}
		
	}
	
	
	private void buildResource(String code,String operateState,ExternalSemanticResult semanticResult) {
		switch(code) {
			case "200101":{
								BuildMusicResource(operateState,semanticResult);
								break;
					      }
			case "200201":{
								BuildStoryResource(operateState,semanticResult);
								break;
						  }
			case "200401":{
								buildPoemResource(operateState,semanticResult);
								break;
			}			
			default:
				break;
		}
	}
	
	
	private void BuildMusicResource(String operateState,ExternalSemanticResult semanticResult) {
		Map<String,Object> slots = semanticResult.getSlots();
		MusicResource resource = new MusicResource();
		Object song = slots.get("song");
		if(null != song) {
			resource.setSong(song.toString());
		}	
		Object singer = slots.get("singer");
		if(null != singer) {
			resource.setSinger(singer.toString());
		}
		Object id = slots.get("id");
		if(null != id) {
			resource.setId((Integer)id);
		}
		
		
		resource.setUrl(semanticResult.getData().getUrl());		
		
		semanticResult.setResource(resource);
	}
	
	private void BuildStoryResource(String operateState,ExternalSemanticResult semanticResult) {
		Map<String,Object> slots = semanticResult.getSlots();
		StoryResource resource = new StoryResource();
		Object name = slots.get("storyName");
		if(null != name) {
			resource.setStory(name.toString());
		}		
		Object id = slots.get("id");
		if(null != id) {
			resource.setId((Integer)id);
		}
		
		resource.setUrl(semanticResult.getData().getUrl());		
		
		semanticResult.setResource(resource);
	}
	
	private void buildPoemResource(String operateState,ExternalSemanticResult semanticResult) {
		Map<String,Object> slots = semanticResult.getSlots();
		PoetryResource resource = new PoetryResource();
		Object author = slots.get("poetryAuthor");
		if(null != author) {
			resource.setAuthorName(author.toString());
		}		
		Object title = slots.get("poetryTitle");
		if(null != title) {
			resource.setTitle(title.toString());
		}		
		Object dynasty = slots.get("dynasty");
		if(null != dynasty) {
			resource.setDynasty(dynasty.toString());//
		}
		
		//返回诗文格式：诗名  朝代 作者  正文
		//但不确定是不是所有的诗都包含这四部分内容
		if(semanticResult.getAnswer() != null) {
			String content = semanticResult.getAnswer();
			String[] segs = content.split(" ");
			if(segs.length > 1) {
				content = segs[segs.length-1];
				resource.setContent(content);
			}
			
		}
		
		semanticResult.setResource(resource);
	}
	
	/**
	 * 解析返回结果听语义槽数据
	 * @param code
	 * @param parametersElem
	 * @param semanticResult
	 */
	private void parseParameters(String code,JsonElement parametersElem,ExternalSemanticResult semanticResult) {
		JsonObject parameterObject = parametersElem.getAsJsonObject();		
		switch(code) {
			case "200101":{
								parserSong(parameterObject,semanticResult);
								break;
					      }
			case "200201":{
								parserStory(parameterObject,semanticResult);
								break;
						  }
			case "200401":{
								parserPoem(parameterObject,semanticResult);
								break;
			}
			case "200301":
			case "200303":
			case "200302":{
								parserAnimalSound(parameterObject,semanticResult);
								break;
			              }
			case "200501":
			case "900110":
			case "900101":{
								parsetSetting(parameterObject,semanticResult);
								break;
			              }
			case "200710":{
								parsetMemo(parameterObject,semanticResult);
								break;
                          }
			case "300101":{
							parseAction(parameterObject,semanticResult);
							break;
          }
			default:
				break;
		}
	}
	
	private void parseAction(JsonObject parameterObject,ExternalSemanticResult semanticResult) {
		String direction = parseString(parameterObject,"direction");		
		Map<String,Object> slots = new HashMap<String,Object>();
		if(!StringTool.isEmpty(direction)) {
			slots.put("direction", direction);
		}
				
		semanticResult.setSlots(slots);
		semanticResult.setOperation(OperationEx.CONTROL);
		semanticResult.setParamType(ParamTypeEx.C);
	}
	
	private void parsetMemo(JsonObject parameterObject,ExternalSemanticResult semanticResult) {
		String memoContent = parseString(parameterObject,"memoContent");
		String alarmType = parseString(parameterObject,"alarmType");
		String endDate = parseString(parameterObject,"endDate");
		String startDate = parseString(parameterObject,"startDate");
				
		Map<String,Object> slots = new HashMap<String,Object>();
		if(!StringTool.isEmpty(memoContent)) {
			slots.put("memoContent", memoContent);
		}
		if(!StringTool.isEmpty(alarmType)) {
			slots.put("alarmType", alarmType);
		}
		if(!StringTool.isEmpty(endDate)) {
			slots.put("endDate", endDate);
		}
		if(!StringTool.isEmpty(startDate)) {
			slots.put("startDate", startDate);
		}
				
		semanticResult.setSlots(slots);
		semanticResult.setOperation(OperationEx.CONTROL);
		semanticResult.setParamType(ParamTypeEx.C);
	}
	
	private void parsetSetting(JsonObject parameterObject,ExternalSemanticResult semanticResult) {
		semanticResult.setOperation(OperationEx.CONTROL);
		semanticResult.setParamType(ParamTypeEx.C);
	}
	
	private void parserAnimalSound(JsonObject parameterObject,ExternalSemanticResult semanticResult) {
		JsonElement resourcesElem = parameterObject.get("resources");
		if(null == resourcesElem) {
			return;
		}
		
		JsonElement urlElem = resourcesElem.getAsJsonObject().get("url");
		if(null == urlElem) {
			return;
		}
		
		String url = urlElem.getAsString();
		if(null == url) {
			return;
		}		
		semanticResult.getData().setUrl(url);		
		
		semanticResult.setOperation(OperationEx.PLAY);
		semanticResult.setParamType(ParamTypeEx.U);
	}
	
	private String parseString(JsonObject jsonObject,String name) {
		JsonElement jsonElem = jsonObject.get(name);
		if(null == jsonElem) {
			return null;
		}
		
		String result = jsonElem.getAsString();
		return result;		
	}
	
	private Integer parseInt(JsonObject jsonObject,String name) {
		JsonElement jsonElem = jsonObject.get(name);
		if(null == jsonElem) {
			return null;
		}
		
		Integer result = jsonElem.getAsInt();
		return result;		
	}
	
	private void parserStory(JsonObject parameterObject,ExternalSemanticResult semanticResult) {
		Map<String,Object> slots = new HashMap<String,Object>();
		String name = parseString(parameterObject,"name");
		String author = parseString(parameterObject,"author");
		String url = parseString(parameterObject,"url");
		
		Integer id = parseInt(parameterObject,"id");		
		slots.put("id", id);
		
		if(!StringTool.isEmpty(name)) {
			slots.put("storyName", name);
		}
		if(!StringTool.isEmpty(author)) {
			slots.put("author", author);
		}
		if(!StringTool.isEmpty(url)) {
			semanticResult.getData().setUrl(url);
		}			
		
		semanticResult.setSlots(slots);	
		semanticResult.setOperation(OperationEx.PLAY);
		semanticResult.setParamType(ParamTypeEx.TU);
	}
	
	private void parserPoem(JsonObject parameterObject,ExternalSemanticResult semanticResult) {
		Map<String,Object> slots = new HashMap<String,Object>();
		String year = parseString(parameterObject,"year");
		String author = parseString(parameterObject,"author");
		String name = parseString(parameterObject,"name");
		if(!StringTool.isEmpty(name)) {
			slots.put("poetryTitle", name);
		}
		if(!StringTool.isEmpty(author)) {
			slots.put("poetryAuthor", author);
		}
		if(!StringTool.isEmpty(year)) {
			slots.put("dynasty", year);
		}			
		
		semanticResult.setSlots(slots);		
	}
	
	
	private void parserSong(JsonObject parameterObject,ExternalSemanticResult semanticResult) {
		Map<String,Object> slots = new HashMap<String,Object>();
		String song = parseString(parameterObject,"song");
		String singer = parseString(parameterObject,"singer");
		String url = parseString(parameterObject,"url");
		Integer id = parseInt(parameterObject,"id");
		
		slots.put("id", id);
		if(!StringTool.isEmpty(song)) {
			slots.put("song", song);
		}
		if(!StringTool.isEmpty(singer)) {
			slots.put("singer", singer);
		}
		if(!StringTool.isEmpty(url)) {			
			semanticResult.getData().setUrl(url);
		}			
		
		semanticResult.setSlots(slots);
		semanticResult.setOperation(OperationEx.PLAY);
		semanticResult.setParamType(ParamTypeEx.TU);
	}
	
}
