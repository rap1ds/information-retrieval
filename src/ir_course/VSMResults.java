package ir_course;

import java.util.LinkedList;
import java.util.List;

public class VSMResults {
	public List<String> list;
	public int relevantResults;
	
	public VSMResults() {
		this.list = new LinkedList<String>();
		this.relevantResults = 0;
	}
}
