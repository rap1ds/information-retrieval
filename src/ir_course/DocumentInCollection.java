/*
 * Class for representing a document in a document collection
 * Created on 2012-01-04
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */
package ir_course;

public class DocumentInCollection {

	private String title;
	private String abstractText;
	private int searchTaskNumber;
	private String query;
	private boolean relevant;
	
	public DocumentInCollection() {
		this(null, null, 0, null, false);
	}
	
	public DocumentInCollection(String title, String abstractText, int searchTaskNumber, String query, boolean relevant) {
		this.title = title;
		this.abstractText = abstractText;
		this.searchTaskNumber = searchTaskNumber;
		this.query = query;
		this.relevant = relevant;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public int getSearchTaskNumber() {
		return searchTaskNumber;
	}

	public void setSearchTaskNumber(int searchTaskNumber) {
		this.searchTaskNumber = searchTaskNumber;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}

	public String toString() {
		return "Title: "+title+"\n abstract: "+abstractText+"\n search task number: "+searchTaskNumber+"\n query: "+query+"\n relevant: "+relevant;
	}
}