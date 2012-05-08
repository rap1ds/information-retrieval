package ir_course;
/*
 * Class for representing a document in an RSS feed
 * Created on 2011-12-21
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */


import java.util.Date;

public class RssFeedDocument {

	private String title;
	private String description;
	private Date pubDate;
	
	public RssFeedDocument() {
		this(null, null, null);
	}
	
	public RssFeedDocument(String title, String description, Date pubDate) {
		this.title = title;
		this.description = description;
		this.pubDate = pubDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getPubDate() {
		return pubDate;
	}

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}
	
	public String toString() {
		return "Title: "+title+"\n description: "+description+"\n publication date: "+pubDate;
	}
}