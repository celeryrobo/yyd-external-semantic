package com.yyd.external.semantic.xunfei;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.yyd.external.util.Http;
import com.yyd.external.util.StringTool;
import com.yyd.external.semantic.ExternalCommonBean;
import com.yyd.external.semantic.ExternalSemanticError;
import com.yyd.external.semantic.ExternalSemanticResult;
import com.yyd.external.semantic.ExternalSemanticService;
import com.yyd.external.semantic.resource.NewsResource;
import com.yyd.external.semantic.resource.PoetryResource;
import com.yyd.external.semantic.resource.StoryResource;
import com.yyd.external.semantic.ExternalSemanticResult.OperationEx;
import com.yyd.external.semantic.ExternalSemanticResult.ParamTypeEx;

/**
 * 访问讯飞语义
 * @author pc
 *
 */
public class XunfeiSemanticService implements ExternalSemanticService{
	private  String web_api_url_text_semantic = "http://api.xfyun.cn/v1/aiui/v1/text_semantic";	
	private  String XAppid = null;
	private  String apiKey = null;
	
	@Override
	public ExternalSemanticResult handleSemantic(String text,Map<String,String> params)throws Exception{
		ExternalSemanticResult result = null;
		if(StringTool.isEmpty(text) || params == null) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_INPUT_PARAM_EMPTY);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INPUT_PARAM_EMPTY));
			return result;
		}
		
		String userId = params.get("userId");
		XAppid = params.get("appid");
		apiKey = params.get("apiKey");
		if(StringTool.isEmpty(userId) || StringTool.isEmpty(XAppid) || StringTool.isEmpty(apiKey)) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM));
			return result;
		}
		
		long start = System.currentTimeMillis();	
		result = xunfeiWebApiTextUnderstand("main",userId,text);
		long total = System.currentTimeMillis() - start;
		result.setTime(total);
		
		return result;
	}
	
	/**
	 * 迅飞语义：两个重要字段semantic 和answer字段,问答对类的(开放问答和自定义问答)一般没有semantic字段，技能类的一般会有semantic字段，可能没有answer字段
	 * 
	 */
	public  ExternalSemanticResult xunfeiWebApiTextUnderstand(String scene,String userid,String question){
		ExternalSemanticResult semanticResult = new ExternalSemanticResult();
		
		try {			
			String XCurTime = (System.currentTimeMillis() / 1000) + "";
			
			JSONObject XParam = new JSONObject();
			XParam.put("scene", scene);
			XParam.put("userid", userid);
			String XParamBase64Str = Base64.getEncoder().encodeToString(XParam.toString().getBytes());

			String http_body = "text="+Base64.getEncoder().encodeToString(question.getBytes());
			
			Http http = new Http(web_api_url_text_semantic);
			http.setCharset("utf-8");
			http.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			http.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");			
			//授权信息
			http.setRequestProperty("X-Appid", XAppid);
			http.setRequestProperty("X-CurTime", XCurTime);
			StringBuilder sb = new StringBuilder();
			sb.append(apiKey).append(XCurTime).append(XParamBase64Str).append(http_body);
			http.setRequestProperty("X-CheckSum",EncoderByMd5(sb.toString()).toLowerCase());
			//XParam 
			http.setRequestProperty("X-Param", XParamBase64Str);

			//post请求
			String result = http.post(http_body);
			semanticResult.setSrcResult(result);
			parseResult(result,semanticResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return semanticResult;
	}
	
	public void parseResult(String result,ExternalSemanticResult semanticResult) {
		if(null == result || result.isEmpty()) {
			semanticResult.setRet(ExternalSemanticError.ERROR_INVALID_RESULT_DATA);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INVALID_RESULT_DATA));	
			return;
		}
		
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));	
			return;
		}
		
		semanticResult.setData(new ExternalCommonBean());		
		
		String rcCode = obj.get("code").getAsString();
		String desc  = obj.get("desc").getAsString();
		String sid = obj.get("sid").getAsString();
		semanticResult.setSid(sid);
		
		if(!rcCode.equalsIgnoreCase("00000")) {
			semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));	
			semanticResult.setSemanticRet(rcCode);
			semanticResult.setSemanticMsg(desc);
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));	
			return;
		}
		
		int rc = dataObject.get("rc").getAsInt();
		if(rc != 0) {
			semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));	
			semanticResult.setSemanticRet(rcCode);
			semanticResult.setSemanticMsg("rc ="+Integer.valueOf(rc));
			return;
		}
		
		String service = dataObject.get("service").getAsString();
		String text = dataObject.get("text").getAsString();
		semanticResult.setText(text);
		semanticResult.setService(service);
		
		//提取intent和slot等语义信息		
		JsonArray semanticArray = null;
		JsonElement semanticElem = dataObject.get("semantic");
		if(null != semanticElem) {
			semanticArray = semanticElem.getAsJsonArray();
		}
		if(null != semanticArray && semanticArray.size() >0) {
			//TODO:只提取第一条语义信息
			JsonObject first = semanticArray.get(0).getAsJsonObject();
			String intent = first.get("intent").getAsString();
			semanticResult.setIntent(intent);			
			//要提取语义槽
			JsonArray slotArray = first.get("slots").getAsJsonArray();
			Map<String,Object> slots = new HashMap<String,Object>();
			if(null != slotArray) {
				for(int i =0; i < slotArray.size();i++) {
					JsonObject item = slotArray.get(i).getAsJsonObject();
					String name = item.get("name").getAsString();
					String value = item.get("value").getAsString();
					if(null !=name) {
						slots.put(name, value);
					}
					
				}
			}
		
		  if(slots.size() >0) {
			  semanticResult.setSlots(slots);
		  }
		}
		
		
		//提取语义回复结果
		JsonObject answerObj = null;
		JsonElement answerElem = dataObject.get("answer");
		if(null != answerElem) {
			answerObj = answerElem.getAsJsonObject();
		}
		if(null != answerObj) {
			String answer = answerObj.get("text").getAsString();
			semanticResult.setAnswer(answer);
		}
		
		//处理需要单独合成结果的语义
		parseServiceResult(semanticResult.getService(),semanticResult.getIntent(),result,semanticResult.getAnswer(),semanticResult);
		
		
		semanticResult.getData().setText(semanticResult.getAnswer());
		semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
		semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));
		if(semanticResult.getOperation() == null) {
			semanticResult.setOperation(OperationEx.SPEAK);
			semanticResult.setParamType(ParamTypeEx.T);
		}
		
	}
	
	private void parseServiceResult(String service,String intent,String result,String answerResult,ExternalSemanticResult semanticResult) {
			switch(service) {
			case "stock":
			{
				parseStockServiceResult(result,semanticResult);
				break;
			}
			case "translation":
			{
				parseTranslationServiceResult(result,semanticResult);
				break;
			}
			case "weather":
			{
				parseWeatherServiceResult(answerResult,semanticResult);
				break;
			}
			case "holiday":
			{
				parseHolidayServiceResult(result,semanticResult);
				break;
			}
			case "news":
			{
				parseNewsServiceResult(result,semanticResult);
				break;
			}
			case "story":
			{
				parseStoryServiceResult(result,semanticResult);
				break;
			}
			case "poetry":
			{
				parsePoetryServiceResult(result,semanticResult);
				break;
			}
			case "idiom":
			{
				parseIdiomServiceResult(result,semanticResult);
				break;
			}
			case "YYD.app_1":
			case "YYD.robot_control":
			{
				parseYYDServiceResult(result,semanticResult);
				break;
			}
			default:
				break;
		}
		
	
	}
	
	private void  parseYYDServiceResult(String result,ExternalSemanticResult semanticResult) {
		String service = semanticResult.getService();
		service = service.replace("YYD.", "");
		semanticResult.setService(service);
		
		semanticResult.setOperation(OperationEx.COMMAND);
		semanticResult.setParamType(ParamTypeEx.TC);		
	}
	
	private void  parseIdiomServiceResult(String result,ExternalSemanticResult semanticResult) {
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return;
		}
		JsonObject subDataObject = dataObject.get("data").getAsJsonObject();
		if(null == subDataObject) {
			return;
		}
		
		
		JsonArray resultArray = subDataObject.get("result").getAsJsonArray();
		if(null == resultArray || resultArray.size() <= 0) {
			return;
		}
		
		//暂时只取第一条
		String intent = semanticResult.getIntent();
		if(null == intent) {
			return;
		}
		
		//查询成语和查询成语解释
		if(intent.equalsIgnoreCase("QUERY") || intent.equalsIgnoreCase("INTERPRETATION_QUERY")) {
			JsonObject dateObj = resultArray.get(0).getAsJsonObject();
			String name = dateObj.get("name").getAsString();	
			String text = dateObj.get("text").getAsString();	
			semanticResult.setAnswer(name+","+text);
		}
		else if(intent.equalsIgnoreCase("SOURCE_QUERY")) {//查询成语出处
			JsonObject dateObj = resultArray.get(0).getAsJsonObject();
			String name = dateObj.get("name").getAsString();
			String source = dateObj.get("source").getAsString();		
			semanticResult.setAnswer(name+",出自"+source);
		}
		else if(intent.equalsIgnoreCase("SOLITAIRE")) {//成语接龙
			JsonObject dateObj = resultArray.get(0).getAsJsonObject();
			String name = dateObj.get("name").getAsString();					
			semanticResult.setAnswer(name);
		}
		
		semanticResult.setOperation(OperationEx.SPEAK);
		semanticResult.setParamType(ParamTypeEx.T);		
	}
	
	private void  parsePoetryServiceResult(String result,ExternalSemanticResult semanticResult) {
		Map<String,Object> slot = semanticResult.getSlots();
		if(null != slot) {
			if(slot.containsKey("author")) {
				Object value = slot.get("author");
				slot.remove("author");
				slot.put("poetryAuthor", value);
			}
			if(slot.containsKey("name")) {
				Object value = slot.get("name");
				slot.remove("name");
				slot.put("poetryTitle", value);
			}
			if(slot.containsKey("keyword")) {
				Object value = slot.get("keyword");
				slot.remove("keyword");
				slot.put("poetrySentence", value);
			}
		}
		
		
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return;
		}
		JsonObject subDataObject = dataObject.get("data").getAsJsonObject();
		if(null == subDataObject) {
			return;
		}
		
		
		JsonArray resultArray = subDataObject.get("result").getAsJsonArray();
		if(null == resultArray || resultArray.size() <= 0) {
			return;
		}
		
		//暂时只取第一条
		JsonObject dateObj = resultArray.get(0).getAsJsonObject();
		String dynasty = dateObj.get("dynasty").getAsString();
		String author = dateObj.get("author").getAsString();	
		String title = dateObj.get("title").getAsString();
		String content = dateObj.get("content").getAsString();
		Integer id = dateObj.get("id").getAsInt();
		
		PoetryResource resource = new PoetryResource();
		resource.setAuthorName(author);
		resource.setContent(content);
		resource.setDynasty(dynasty);
		resource.setTitle(title);
		resource.setId(id);
		semanticResult.setResource(resource);
		
		//判断具体的意图，迅飞将意图放在两个字段中表示
		String answer = null;
		Object queryField = slot.get("queryField");
		Object queried = slot.get("queried");
		
		if(null != queryField) {
			String field = queryField.toString();
			if(field.equalsIgnoreCase("lastSent")) {
				answer = title+"的最后一句是"+content;
			}
			else if(field.equalsIgnoreCase("firstSent")) {
				answer = title+"的第一句是"+content;
			}
			else if(field.equalsIgnoreCase("nextSent")) {
				String keyword = slot.get("poetrySentence").toString();
				answer = keyword+"的下一句是"+content;
			}
			else if(field.equalsIgnoreCase("pastSent")) {
				String keyword = slot.get("poetrySentence").toString();
				answer = keyword+"的上一句是"+content;
			}
			
			//此类意图下，没有诗文的正文
			resource.setContent(null);
		}
		else if(null != queried) {
			String field = queried.toString();
			if(field.equalsIgnoreCase("author")) {
				answer = title+"的作者是"+author;
			}
			else if(field.equalsIgnoreCase("dynasty")) {
				answer = title+"的朝代是"+dynasty;
			}
		}
		else {
			StringBuilder build = new StringBuilder();
			if(null != title) {
				build.append(title+" ");
			}
			if(null != author) {
				build.append(author+" ");
			}
			build.append(content);			
			answer = build.toString();	
		}
		
		semanticResult.setAnswer(answer);
		
		semanticResult.setOperation(OperationEx.SPEAK);
		semanticResult.setParamType(ParamTypeEx.T);		
	}
	
	
	private void  parseStoryServiceResult(String result,ExternalSemanticResult semanticResult) {
		Map<String,Object> slot = semanticResult.getSlots();
		if(null != slot) {
			if(slot.containsKey("name")) {
				Object value = slot.get("name");
				slot.remove("name");
				slot.put("storyName", value);
			}
		}
		
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return;
		}
		JsonObject subDataObject = dataObject.get("data").getAsJsonObject();
		if(null == subDataObject) {
			return;
		}
		
		
		JsonArray resultArray = subDataObject.get("result").getAsJsonArray();
		if(null == resultArray || resultArray.size() <= 0) {
			return;
		}
		
		//暂时只取第一条故事
		JsonObject dateObj = resultArray.get(0).getAsJsonObject();
		String name = dateObj.get("name").getAsString();
		String url = dateObj.get("playUrl").getAsString();				
		semanticResult.getData().setUrl(url);
		
		StoryResource resource = new StoryResource();
		resource.setStory(name);
		resource.setUrl(url);
		semanticResult.setResource(resource);
		
		semanticResult.setOperation(OperationEx.PLAY);
		semanticResult.setParamType(ParamTypeEx.TU);
		
		String answer = "开始播放"+name;	
		semanticResult.setAnswer(answer);		
	}
	
	private void  parseNewsServiceResult(String result,ExternalSemanticResult semanticResult) {
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return;
		}
		JsonObject subDataObject = dataObject.get("data").getAsJsonObject();
		if(null == subDataObject) {
			return;
		}
		
		
		JsonArray resultArray = subDataObject.get("result").getAsJsonArray();
		if(null == resultArray || resultArray.size() <= 0) {
			return;
		}
		
		//暂时只取第一条新闻
		JsonObject dateObj = resultArray.get(0).getAsJsonObject();
		String title = dateObj.get("title").getAsString();
		String catetory = dateObj.get("category").getAsString();
		String url = dateObj.get("url").getAsString();
		String keyWords = dateObj.get("keyWords").getAsString();
		String publishDateTime = dateObj.get("publishDateTime").getAsString();
		
		semanticResult.getData().setText(title);	
		semanticResult.getData().setUrl(url);
		
		NewsResource resource = new NewsResource();
		resource.setKeyWords(keyWords);
		resource.setPublishDateTime(publishDateTime);
		resource.setTitle(title);
		resource.setUrl(url);
		semanticResult.setResource(resource);
		
		semanticResult.setOperation(OperationEx.PLAY);
		semanticResult.setParamType(ParamTypeEx.TU);

		String answer = null;
		answer = "为您播放"+catetory+"新闻";
		semanticResult.setAnswer(answer);		
	}
	
	private void  parseHolidayServiceResult(String result,ExternalSemanticResult semanticResult) {
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return;
		}
		JsonObject subDataObject = dataObject.get("data").getAsJsonObject();
		if(null == subDataObject) {
			return;
		}
		
		
		JsonArray resultArray = subDataObject.get("result").getAsJsonArray();
		if(null == resultArray || resultArray.size() <= 0) {
			return;
		}
		
		StringBuilder build = new StringBuilder();
		for(int i=0;i < resultArray.size();i++) {
			JsonObject dateObj = resultArray.get(i).getAsJsonObject();
			String name = dateObj.get("name").getAsString();
			String holidayStartDate = dateObj.get("holidayStartDate").getAsString();
			String holidayEndDate = dateObj.get("holidayEndDate").getAsString();
			String duration = dateObj.get("duration").getAsString();
			String desc = name+","+holidayStartDate+"至"+holidayEndDate+",共"+duration+"天";
			build.append(desc);
			if(i != resultArray.size()-1) {
				build.append(";");
			}			
		}

		String answer = null;		
		answer = build.toString();
		semanticResult.setAnswer(answer);
	}
	
	private void parseWeatherServiceResult(String result,ExternalSemanticResult semanticResult) {
		if(null == result || result.isEmpty()) {
			return;
		}
		String answer = result;
		answer = answer.replace("\"", "");
		semanticResult.setAnswer(answer);		
	}
	
	private void  parseTranslationServiceResult(String result,ExternalSemanticResult semanticResult) {
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return ;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return;
		}
		
		JsonObject subDataObject = dataObject.get("data").getAsJsonObject();
		if(null == subDataObject) {
			return;
		}
		
		JsonArray resultArray = subDataObject.get("result").getAsJsonArray();
		if(null == resultArray || resultArray.size() <= 0) {
			return;
		}
		
		JsonObject first = resultArray.get(0).getAsJsonObject();
		String answer = null;	
		answer = first.get("translated").getAsString();
		semanticResult.setAnswer(answer);		
	}
	
	/**
	 * 解析股票技能返回结果
	 * @param result
	 * @return
	 */
	private void parseStockServiceResult(String result,ExternalSemanticResult semanticResult) {
		String answer = null;		
		StringBuilder build = new StringBuilder();
		
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return;
		}
		
		//提取intent和slot等语义信息，主要是为提取股票名称
		Map<String,Object> slots = new HashMap<String,Object>();
		JsonArray semanticArray = null;
		JsonElement semanticElem = dataObject.get("semantic");
		if(null != semanticElem) {
			semanticArray = semanticElem.getAsJsonArray();
		}
		if(null != semanticArray && semanticArray.size() >0) {
			//TODO:只提取第一条语义信息
			JsonObject first = semanticArray.get(0).getAsJsonObject();
			//要提取语义槽
			JsonArray slotArray = first.get("slots").getAsJsonArray();
			if(null != slotArray) {
				for(int i =0; i < slotArray.size();i++) {
					JsonObject item = slotArray.get(i).getAsJsonObject();
					String name = item.get("name").getAsString();
					String value = item.get("value").getAsString();
					if(null !=name) {
						slots.put(name, value);
					}
					
				}
			}		  
		}
		else
		{
			return;
		}
		
		String stockName = slots.get("name").toString();
		String stockCode = "股票编号"+slots.get("market").toString()+slots.get("code").toString();
		build.append(stockName+",");
		build.append(stockCode+",");
		
		//提取股票详细信息
		//提取intent和slot等语义信息	
		String lastClosePrice = null;
		String openPrice = null;
		String currentPrice = null;
		String highPrice = null;
		String lowPrice = null;
		String riseValue = null;
		String riseRate = null;
		String updateDateTime = null;
		
		
		JsonObject stockDataObject = null;
		JsonElement stockDataElem = dataObject.get("data");
		if(null != stockDataElem) {
			stockDataObject = stockDataElem.getAsJsonObject();
		}
		if(null != stockDataObject) {
			//TODO:只提取第一条语义信息
			JsonArray resultArray = stockDataObject.get("result").getAsJsonArray();
			if(null != resultArray && resultArray.size() >0) {
				//要提取语义槽
				JsonObject first = resultArray.get(0).getAsJsonObject();
				
				lastClosePrice = "昨日收盘价"+first.get("closingPrice").getAsString();
				openPrice = "今日开盘价"+first.get("openingPrice").getAsString();
				currentPrice = "当前价"+first.get("currentPrice").getAsString();
				highPrice = "今日最高价"+first.get("highPrice").getAsString();
				lowPrice = "今日最低价"+first.get("lowPrice").getAsString();
				riseValue = "涨跌额"+first.get("riseValue").getAsString();
				riseRate = "涨跌幅"+first.get("riseRate").getAsString();
				updateDateTime = first.get("updateDateTime").getAsString()+"更新";
				
				build.append(lastClosePrice+",");
				build.append(openPrice+",");
				build.append(currentPrice+",");
				build.append(highPrice+",");
				build.append(lowPrice+",");
				build.append(riseValue+",");
				build.append(riseRate+",");
				build.append(updateDateTime);
			}
					  
		}
		
		answer = build.toString();
		semanticResult.setAnswer(answer);		
	}
	
	 /**利用MD5进行加密
     * @param str  待加密的字符串
     * @return  加密后的字符串
     * @throws NoSuchAlgorithmException  没有这种产生消息摘要的算法
     * @throws UnsupportedEncodingException  
     */
    private  String EncoderByMd5(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException{
    	char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
}
