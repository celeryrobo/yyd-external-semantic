package com.yyd.external.semantic.turing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TuringCode {
	
	private  static Map<String,String> codeMap = new HashMap<String,String>();
	private  static Set<String> codeError = new HashSet<String>();
	
	static {
		codeError.add("40001");
		codeError.add("40002");
		codeError.add("40004");
		codeError.add("40005");
		codeError.add("40007");
		codeError.add("40008");
		
		codeMap.put("40001", "apiKey输入格式错误（长度、格式）；系统中不存在该apiKey信息");
		codeMap.put("40002", "用户选择加密时data参数为空。");
		codeMap.put("40004", "userId或apiKey调用次数达到上限，userId创建个数达到上限");
		codeMap.put("40005", "没有任何用户功能权限");
		codeMap.put("40007", "请求参数为空；请求参数无法格式化；请求参数加密异常");
		codeMap.put("40008", "userId没有上传");
		
		//TODO:会多100000和900110，200702，300101分别多应多个场景
		codeMap.put("200101", "os.sys.song");
		codeMap.put("200301", "os.sys.animalSounds");
		codeMap.put("200302", "os.sys.natureSounds");
		codeMap.put("200303", "os.sys.musicInstrumentSounds");
		codeMap.put("900110", "os.sys.setting");
		codeMap.put("900101", "os.sys.exit");
		codeMap.put("200710", "os.sys.memo");
		codeMap.put("300101", "os.sys.action");
		codeMap.put("900110", "os.sys.setting");
		codeMap.put("200501", "os.sys.photograph");
		codeMap.put("200205", "os.sys.ask");
		codeMap.put("200201", "os.sys.story");
		codeMap.put("200209", "os.sys.wiki");
		codeMap.put("200401", "os.sys.poem");
		codeMap.put("201711", "os.sys.translate");
		codeMap.put("201501", "os.sys.calculate");
		codeMap.put("100102", "os.sys.chat");
		codeMap.put("201401", "os.sys.weather");
		codeMap.put("200702", "os.sys.date");
		codeMap.put("404003", "os.sys.whovoice");
		codeMap.put("100000", "os.sys.chat");
		codeMap.put("100302", "os.sys.smartfaq");
		codeMap.put("201204", "os.sys.joke");
		codeMap.put("200212", "os.sys.doggerel");
		codeMap.put("200207", "os.sys.tongueTwister");
		codeMap.put("200211", "os.sys.brainTwister");
				
	}
	
	public static String getMsg(String code) {
		return codeMap.get(code);
	}

	public static boolean isErrorCode(String code) {
		return codeError.contains(code)?true:false;
	}
	
}
