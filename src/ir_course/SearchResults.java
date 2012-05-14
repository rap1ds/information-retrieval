package ir_course;

import java.util.LinkedList;
import java.util.List;

public class SearchResults {
	public List<String> list;
	public int relevantResults;
	
	public SearchResults() {
		this.list = new LinkedList<String>();
		this.relevantResults = 0;
	}
}
