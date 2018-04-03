package com.yyd.external.semantic.resource;

public class StoryResource {
	private Integer id;
	private String story;
	private String url;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getStory() {
		return story;
	}

	public void setStory(String story) {
		this.story = story;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "StoryEntity [id=" + id + ", story=" + story + ", url=" + url + "]";
	}
}
