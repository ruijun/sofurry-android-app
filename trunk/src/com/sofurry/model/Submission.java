package com.sofurry.model;

public class Submission {

	public enum SUBMISSION_TYPE {ARTWORK, STORY, JOURNAL, MUSIC};
	
	private SUBMISSION_TYPE type;
	private int id;
	private String name;
	private String content;
	private String tags;
	private String authorName;
	private String authorID;
	
	public SUBMISSION_TYPE getType() {
		return type;
	}
	public void setType(SUBMISSION_TYPE type) {
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getAuthorID() {
		return authorID;
	}
	public void setAuthorID(String authorID) {
		this.authorID = authorID;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
