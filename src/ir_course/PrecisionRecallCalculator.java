package ir_course;

import java.util.ArrayList;
import java.util.List;

public class PrecisionRecallCalculator {

	public List<Double> precisions;
	public double recallStep;
	public int relevantDocumentCount;

	public PrecisionRecallCalculator(int relevantDocumentCount) {
		this.precisions = new ArrayList<Double>();
		recallStep = 0;
		this.relevantDocumentCount = relevantDocumentCount;
	}

	public void calculate(SearchResults results, int hitLimit) {
		
		double tp = results.relevantResults;                         // true positives
		double fp = results.list.size() - tp; 						  // false positives
		double fn = relevantDocumentCount - results.relevantResults; // false negatives
		
		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn); 
		
		if (recall > recallStep / 10 - 0.01 
				&& recall < recallStep / 10 + 0.01
				&& recallStep < 11) {
			
			precisions.add(precision);
			System.out.println("R : " + recall + "        P: " + precision);
			recallStep++;
		}
	}
}
