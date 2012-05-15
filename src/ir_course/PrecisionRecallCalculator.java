package ir_course;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class PrecisionRecallCalculator {

	public List<PrecisionRecall> allResults;
	public List<PrecisionRecall> steps11Results;
	public List<PrecisionRecall> interpolatedResults;

	public int relevantDocumentCount;

	String name;

	public PrecisionRecallCalculator(String name, int relevantDocumentCount) {
		this.name = name;
		this.relevantDocumentCount = relevantDocumentCount;
		this.allResults = new ArrayList<PrecisionRecall>();
	}

	public void calculate(SearchResults results, int hitLimit) {

		double tp = results.relevantResults; // true positives
		double fp = results.list.size() - tp; // false positives
		double fn = relevantDocumentCount - results.relevantResults; // false
																		// negatives

		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);

		this.allResults.add(new PrecisionRecall(precision, recall));

		/*
		 * 
		 * if(recall >= recallStep / 10 && recallStep < 11) {
		 * 
		 * 
		 * precisions.add(precision); stepResults.add(new
		 * PrecisionRecall(precision, recall));
		 * 
		 * recallStep++; }
		 */
	}

	/**
	 * Calculates the interpolated precision
	 */
	private void interpolate() {
		interpolatedResults = new ArrayList<PrecisionRecall>();

		// Pinterp(r) = max p(r1) r1 >= r
		for (int r = 0; r < allResults.size(); r++) {
			PrecisionRecall precisionRecall = allResults.get(r);
			double recall = precisionRecall.recall;
			double maxPrecision = precisionRecall.precision;

			for (int r1 = r; r1 < allResults.size(); r1++) {
				double precision = allResults.get(r1).precision;
				if (precision > maxPrecision) {
					maxPrecision = precision;
				}
			}

			// Save the interpolated precision
			interpolatedResults.add(new PrecisionRecall(maxPrecision, recall));
		}
	}

	private void do11pointInterpolatedAverage() {
		this.steps11Results = new ArrayList<PrecisionRecall>();

		double recallStep = 0;

		for (int i = 0; i < interpolatedResults.size(); i++) {
			PrecisionRecall interpolatedPrecisionRecall = interpolatedResults.get(i);

			if (interpolatedPrecisionRecall.recall >= recallStep
					&& recallStep <= 1) {
				this.steps11Results.add(interpolatedPrecisionRecall);
				recallStep += 0.1;
			}
		}
	}

	public void calculate11pointInterpolated() {

		// Interpolate
		this.interpolate();

		// 11-point
		this.do11pointInterpolatedAverage();
	}

	public void printAllResults() throws IOException {
		// Over 2000 lines, better to write them to a file
		StringBuffer sb = new StringBuffer();
		
		sb.append("Recall, Precision\n");
		
		for (PrecisionRecall precisionRecall : this.allResults) {
			sb.append(precisionRecall.recall + ", "
					+ precisionRecall.precision + "\n");
		}
		
		this.writeToFile(sb, this.name + "_all.txt");
	}

	public void printInterpolatedResults() throws IOException {
		// Over 2000 lines, better to write them to a file
		StringBuffer sb = new StringBuffer();
		
		sb.append("Recall, Precision\n");
		
		for (PrecisionRecall precisionRecall : this.interpolatedResults) {
			sb.append(precisionRecall.recall + ", "
					+ precisionRecall.precision + "\n");
		}
		
		this.writeToFile(sb, this.name + "_interpolated.txt");
	}

	public void print11pointInterpolatedAverage() {
		System.out.println("Recall, Precision");
		for (int step = 0; step < this.steps11Results.size(); step++) {
			PrecisionRecall stepResult = this.steps11Results.get(step);

			System.out.println(stepResult.recall + ", " + stepResult.precision);
		}
	}

	private void writeToFile(StringBuffer sb, String filename) throws IOException {

		Writer out = new OutputStreamWriter(new FileOutputStream(filename),
				"UTF-8");

		try {
			out.write(sb.toString());
		} finally {
			out.close();
		}
	}
}
