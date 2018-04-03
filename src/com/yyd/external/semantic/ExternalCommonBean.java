package com.yyd.external.semantic;

public class ExternalCommonBean {
	private String text;
	private String url;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String toString() {
		return "text="+text+",url="+url;
	}
}
