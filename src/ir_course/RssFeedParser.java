/*
 * Parser for an RSS feed
 * Created on 2011-12-21
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */
package ir_course;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RssFeedParser extends DefaultHandler {
	
	private List<RssFeedDocument> docs;
	
	private DateFormat formatter;
	
	private boolean item;
	private boolean title;
	private boolean description;
	private boolean pubDate;
	
	private RssFeedDocument currentDoc;
	
	public RssFeedParser() {
		this.docs = new LinkedList<RssFeedDocument>();
		
		this.formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		
		this.item = false;
		this.title = false;
		this.description = false;
		this.pubDate = false;
	}
	
	// parses the RSS feed in the given URI
	public void parse(String uri) {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(uri, this);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// returns the documents of the RSS feed as a list
	public List<RssFeedDocument> getDocuments() {
		return this.docs;
	}
	
	
	// methods for the SAX parser below
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("item")) {
			this.item = true;
			this.currentDoc = new RssFeedDocument();
		}
		else if (qName.equals("title"))
			this.title = true;
		else if (qName.equals("description"))
			this.description = true;
		else if (qName.equals("pubDate"))
			this.pubDate = true;
	}
	
	public void endElement(String uri, String localName, String qName)  {
		if (qName.equals("item")) {
			this.item = false;
			if (this.currentDoc.getTitle() != null)
				docs.add(this.currentDoc);
		}
		else if (qName.equals("title"))
			this.title = false;
		else if (qName.equals("description"))
			this.description = false;
		else if (qName.equals("pubDate"))
			this.pubDate = false;
	}
	
	public void characters(char[] ch, int start, int length) {
		String text = "";
		for (int i=0; i<length; i++)
			text += ch[start+i];
		if (this.item) {
			if (this.title)
				this.currentDoc.setTitle(text);
			else if (this.description)
				this.currentDoc.setDescription(text);
			else if (this.pubDate) {
				try {
					this.currentDoc.setPubDate(this.formatter.parse(text));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}