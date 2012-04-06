package net.yihabits.english.db;

import java.util.Date;

public class EnglishModel {

	private long id = -1;
	private String name;
	private String category;
	private String content;
	private String url;
	private String reportLocation;
	private String wordsLocation;
	private String reportUrl;
	private String wordsUrl;
	private String publishedAt; 
	private int source; 
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getReportLocation() {
		return reportLocation;
	}
	public void setReportLocation(String reportLocation) {
		this.reportLocation = reportLocation;
	}
	public String getWordsLocation() {
		return wordsLocation;
	}
	public void setWordsLocation(String wordsLocation) {
		this.wordsLocation = wordsLocation;
	}
	public String getReportUrl() {
		return reportUrl;
	}
	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}
	public String getWordsUrl() {
		return wordsUrl;
	}
	public void setWordsUrl(String wordsUrl) {
		this.wordsUrl = wordsUrl;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getPublishedAt() {
		return publishedAt;
	}
	public void setPublishedAt(String publishedAt) {
		this.publishedAt = publishedAt;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	
	
}
