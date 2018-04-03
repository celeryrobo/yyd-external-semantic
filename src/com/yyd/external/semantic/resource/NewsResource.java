package com.yyd.external.semantic.resource;

public class NewsResource {
	private String keyWords;
	private String publishDateTime;
	private String title;
	private String url;
	
	public String getKeyWords() {
		return keyWords;
	}
	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	public String getPublishDateTime() {
		return publishDateTime;
	}
	public void setPublishDateTime(String publishDateTime) {
		this.publishDateTime = publishDateTime;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "NewsEntity [title=" + title + ", keyWords=" + keyWords + ", url=" + url + "]";
	}
}
