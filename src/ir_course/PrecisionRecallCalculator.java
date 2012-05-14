package ir_course;

import java.util.ArrayList;
import java.util.List;

public class PrecisionRecallCalculator {

	private class PrecisionRecall {
		
		public double precision;
		public double recall;
		
		public PrecisionRecall(double precision, double recall) {
			this.precision = precision;
			this.recall = recall;
		}
	}
	
	public List<PrecisionRecall> stepResults;
	public List<Double> precisions;
	public double recallStep;
	public int relevantDocumentCount;

	public PrecisionRecallCalculator(int relevantDocumentCount) {
		this.precisions = new ArrayList<Double>();
		this.stepResults = new ArrayList<PrecisionRecallCalculator.PrecisionRecall>();
		recallStep = 0;
		this.relevantDocumentCount = relevantDocumentCount;
	}

	public void calculate(SearchResults results, int hitLimit) {
		
		double tp = results.relevantResults;                         // true positives
		double fp = results.list.size() - tp; 						  // false positives
		double fn = relevantDocumentCount - results.relevantResults; // false negatives
		
		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn); 
		
		// if (recall > recallStep / 10 - 0.01 
		//		&& recall < recallStep / 10 + 0.01
		//		&& recallStep < 11) {
		
		if(recall >= recallStep / 10 && recallStep < 11) {
			
		
			precisions.add(precision);
			stepResults.add(new PrecisionRecall(precision, recall));
			
			recallStep++;
		}
	}
	
	public void printPrecisionRecallSteps() {
		System.out.println("Recall, Precision");
		for(int step = 0; step < this.stepResults.size(); step++) {
			PrecisionRecall stepResult = this.stepResults.get(step);
			
			System.out.println(stepResult.recall + ", " + stepResult.precision);
		}
	}
}
