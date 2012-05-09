/*
 * Parser for a document collection
 * Created on 2012-01-04
 * Modified on 2012-04-19
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 * Matias Frosterus <matias.frosterus@aalto.fi>
 */
package ir_course;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DocumentCollectionParser extends DefaultHandler {
	
	private List<DocumentInCollection> docs;
	
	private boolean item;
	private boolean title;
	private boolean abstractText;
	private boolean searchTaskNumber;
	private boolean query;
	private boolean relevance;
	
	private String currentText;
	private DocumentInCollection currentDoc;
	
	public DocumentCollectionParser() {
		this.docs = new LinkedList<DocumentInCollection>();
		
		this.item = false;
		this.title = false;
		this.abstractText = false;
		this.searchTaskNumber = false;
		this.query = false;
		this.relevance = false;
	}
	
	// parses the document collection in the given URI
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
	
	// returns the documents of the collection as a list
	public List<DocumentInCollection> getDocuments() {
		return this.docs;
	}
	
	
	// methods for the SAX parser below
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		this.currentText = "";
		if (qName.equals("item")) {
			this.item = true;
			this.currentDoc = new DocumentInCollection();
		}
		else if (qName.equals("title"))
			this.title = true;
		else if (qName.equals("abstract"))
			this.abstractText = true;
		else if (qName.equals("search_task_number"))
			this.searchTaskNumber = true;
		else if (qName.equals("query"))
			this.query = true;
		else if (qName.equals("relevance"))
			this.relevance = true;
	}
	
	public void endElement(String uri, String localName, String qName)  {
		this.currentText = this.currentText.trim();
		if (qName.equals("item")) {
			this.item = false;
			if (this.currentDoc.getTitle() != null)
				docs.add(this.currentDoc);
		}
		else if (qName.equals("title")) {
			this.currentDoc.setTitle(this.currentText);
			this.title = false;
		}
		else if (qName.equals("abstract")) {
			this.currentDoc.setAbstractText(this.currentText);
			this.abstractText = false;
		}
		else if (qName.equals("search_task_number")) {
			this.currentDoc.setSearchTaskNumber(Integer.valueOf(this.currentText));
			this.searchTaskNumber = false;
		}
		else if (qName.equals("query")) {
			this.currentDoc.setQuery(this.currentText);
			this.query = false;
		}
		else if (qName.equals("relevance")) {
			if (Integer.valueOf(this.currentText) == 1)
				this.currentDoc.setRelevant(true);
			this.relevance = false;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		String text = "";
		for (int i=0; i<length; i++)
			text += ch[start+i];
		this.currentText += text;
	}

}