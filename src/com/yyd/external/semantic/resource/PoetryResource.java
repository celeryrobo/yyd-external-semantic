package com.yyd.external.semantic.resource;

public class PoetryResource {
	private Integer id;
	private String title;
	private String authorName;
	private String content;
	private String dynasty;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDynasty() {
		return dynasty;
	}

	public void setDynasty(String dynasty) {
		this.dynasty = dynasty;
	}

	@Override
	public String toString() {
		return "PoetryEntity [id=" + id + ", title=" + title + ", authorName=" + authorName + ", content=" + content
				+ ", dynasty=" + dynasty + "]";
	}
}
