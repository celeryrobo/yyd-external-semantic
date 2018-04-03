package com.yyd.external.semantic.resource;

public class MusicResource {
	private Integer id;
	private String singer;
	private String song;
	private String url;

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getSong() {
		return song;
	}

	public void setSong(String song) {
		this.song = song;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "MusicEntity [id=" + id + ", song=" + song + ", url=" + url + "]";
	}
}
