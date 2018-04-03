package test.java;

import java.util.HashMap;
import java.util.Map;

import com.yyd.external.semantic.ExternalSemanticResult;
import com.yyd.external.semantic.lingju.LingjuSemanticService;
import com.yyd.external.semantic.sanjiaoshou.SanjiaoshouSemanticService;
import com.yyd.external.semantic.turing.TuringSemanticService;
import com.yyd.external.semantic.turing.TuringUserIdService;
import com.yyd.external.semantic.xunfei.XunfeiSemanticService;


public class TestSemantic {
	public static void main(String[] args) {
		TestSemantic test = new TestSemantic();
		test.testXunFei();
		//test.testLingju();
		//test.testSanjiaoshou();
	
		//test.testTuring();
//		test.testTuringUserId();
		
//		String text = "\"深圳\"今天\"阴\"，\"7℃\"，\"无持续风向微风\"";
//		System.out.println(text);
//		text = text.replace("\"", "");
//		System.out.println(text);
	}
	
	void testTuringUserId() {
		String key = "62ee2709d9b5484998875ef6ecf84b11"; //测试demo:7758358de3ef4f34a2502f64d09f9e91
		String secret = "CpsJ11V9SGE9rbJ1"; //密钥,测试demo:z4ENrqOX2768G2U8
		String userDefineId = "909631918328721410"; //userId:909631918328721410  180820935
		
		String userId = TuringUserIdService.getTuringUserId(key, secret, userDefineId,false);
		System.out.println(userId);
	}
	
	void testTuring() {
//		String key = "7758358de3ef4f34a2502f64d09f9e91"; //测试demo:7758358de3ef4f34a2502f64d09f9e91
//		String secret = "z4ENrqOX2768G2U8"; //密钥,测试demo:z4ENrqOX2768G2U8
//		String userId = "146265020"; //userId,测试demo:146265020
		
		TuringSemanticService service = new TuringSemanticService();			
		String key = "62ee2709d9b5484998875ef6ecf84b11"; //测试demo:7758358de3ef4f34a2502f64d09f9e91
		String secret = "CpsJ11V9SGE9rbJ1"; //密钥,测试demo:z4ENrqOX2768G2U8
		String userId = "180820935"; //userId,测试demo:146265020
				
		Map<String,String> params = new HashMap<String,String>();
		params.put("key",key);
		params.put("secret",secret);
		params.put("userId",userId);
		//params.put("encrypt","false");
		
		//我要听刘德华的冰雨,背诵李白的诗（返回text)
		String question = "今天几号";//北京今天的天气,床前明月光的下一句,我喜欢你,我想吃苹果
		try {
			ExternalSemanticResult semanticResult = service.handleSemantic(question,params);			
			System.out.println(semanticResult.getSrcResult());
			System.out.println("ret="+semanticResult.getRet());
			System.out.println("msg="+semanticResult.getMsg());
			System.out.println("service="+semanticResult.getService());
			System.out.println("intent="+semanticResult.getIntent());
			System.out.println("slot="+semanticResult.getSlots());
			//System.out.println("time="+semanticResult.getTime());
			System.out.println("param type="+semanticResult.getParamType());
			System.out.println("opera type="+semanticResult.getOperation());
			System.out.println("data="+semanticResult.getData());
			System.out.println("answer="+semanticResult.getAnswer());
			System.out.println("resource="+semanticResult.getResource());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void testXunFei(){		
		XunfeiSemanticService service = new XunfeiSemanticService();
		try {
			String XAppid = "5a4dd786";
			String apiKey = "34530edc51b34bae86fcda03cfec7463";			
			//String XAppid = "5ac1f8ca";
			//String apiKey = "56ab2351ed624376bc57b88171a9c6eb";
			//死亡黄昏当是什么东西啊,你来自哪里,现在几点了
			//深圳天气怎么样,1+2等于多少,给我跳一个舞,深圳天气怎么样，周星驰的百科,中国移动的股票,把麻婆豆腐翻译成英文,今年出生的是什么生肖
			//白羊座今年的运势,国庆节放假安排,放假安排,马到成功的出处,茄子怎么烧,解释一下梦见大学,来个英语句子,成语接龙马到成功
			//提醒我明天早上吃午饭,我想听安徒生的故事,播放故事丑小鸭,科大讯飞的行情,安静的近义词是什么
			//疑是地上霜的下一句,背李白的静夜思
			//成语接龙马到成功,白羊座的特点,白羊座的日期
			String question = "我想听故事"; 
			
			Map<String,String> params = new HashMap<String,String>();
			params.put("userId","123");
			params.put("appid",XAppid);
			params.put("apiKey",apiKey);
			
			ExternalSemanticResult semanticResult = service.handleSemantic(question,params);
			System.out.println(semanticResult.getSrcResult());
			System.out.println("ret="+semanticResult.getRet());
			System.out.println("msg="+semanticResult.getMsg());
			System.out.println("service="+semanticResult.getService());
			System.out.println("intent="+semanticResult.getIntent());
			System.out.println("slot="+semanticResult.getSlots());
			System.out.println("param type="+semanticResult.getParamType());
			System.out.println("opera type="+semanticResult.getOperation());
			System.out.println("data="+semanticResult.getData());
			System.out.println("answer="+semanticResult.getAnswer());
			System.out.println("resource="+semanticResult.getResource());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	void testLingju() {
		LingjuSemanticService service = new LingjuSemanticService();
		String userId = "31936";
		String userIp = "163.125.210.158"; //目前应该是可以随便填的，没有和用户绑定
		String accessToken = "2f38945bcb388ff135e1fc1d19505ddd"; //用userId绑定的，
		String authcode = "f6d5305a06963ca8db532f010997e2d5";   //数量有限制，花钱才能增加，但目前似乎只有一个在用。
		String appKey = "dff8d355a221cf981cb646398a39eb37";     //每个app唯一
		
		Map<String,String> params = new HashMap<String,String>();
		params.put("userId",userId);
		params.put("userIp",userIp);
		params.put("token",accessToken);
		params.put("authCode", authcode);
		params.put("appKey", appKey);
		
		try {
			String question = "播放周杰伦的青花瓷"; ////1+1等于多少,花儿为什么这么红,今天的股指是多少,播放张学友的歌，你好啊,讲个故事
			ExternalSemanticResult semanticResult =service.handleSemantic(question, params);
			System.out.println(semanticResult.getSrcResult());
			System.out.println("ret="+semanticResult.getRet());
			System.out.println("msg="+semanticResult.getMsg());
			System.out.println("service="+semanticResult.getService());
			System.out.println("intent="+semanticResult.getIntent());
			System.out.println("slot="+semanticResult.getSlots());
			System.out.println("param type="+semanticResult.getParamType());
			System.out.println("opera type="+semanticResult.getOperation());
			System.out.println("data="+semanticResult.getData());
			System.out.println("answer="+semanticResult.getAnswer());
			System.out.println("resource="+semanticResult.getResource());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void testSanjiaoshou(){
		String userId = "1000861";//userId用户自定义即可
		String question = "你喜欢我吗"; //你喜欢我吗,雨后为什么有彩虹,背首诗,我要听健康养生
		Map<String,String> params = new HashMap<String,String>();
		params.put("userId",userId);
		
		SanjiaoshouSemanticService service = new SanjiaoshouSemanticService();
		try {
			ExternalSemanticResult semanticResult =service.handleSemantic(question, params);
			System.out.println(semanticResult.getSrcResult());
			System.out.println("ret="+semanticResult.getRet());
			System.out.println("msg="+semanticResult.getMsg());
			System.out.println("service="+semanticResult.getService());
			System.out.println("intent="+semanticResult.getIntent());
			System.out.println("slot="+semanticResult.getSlots());
			System.out.println("param type="+semanticResult.getParamType());
			System.out.println("opera type="+semanticResult.getOperation());
			System.out.println("data="+semanticResult.getData());
			System.out.println("answer="+semanticResult.getAnswer());
			System.out.println("resource="+semanticResult.getResource());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
