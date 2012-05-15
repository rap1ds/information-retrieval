/*
 * Skeleton class for the Lucene search program implementation
 * Created on 2011-12-21
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */
package ir_course;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneSearchApp {
	// Queries for A3 can be hard-coded
	public static final int OUR_SEARCH_TASK = 8;
	public int relevantsInDocument;
	private final String[] queries = { "simulation industrial environment",
			"computer and physical model simulation",
			"industrial process simulation", 
			"manufacturing process models" };

	private int relevantDocumentCount;

	private StandardAnalyzer analyzer;
	private Directory index;

	public LuceneSearchApp() {
	}

	public void index(List<DocumentInCollection> docs)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {

		analyzer = new StandardAnalyzer(Version.LUCENE_40);

		index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
				analyzer);
		IndexWriter w = new IndexWriter(index, config);

		for (DocumentInCollection xmlDoc : docs) {
			addDoc(w, xmlDoc);
		}

		w.close();
	}

	private void addDoc(IndexWriter w, DocumentInCollection xmlDoc)
			throws CorruptIndexException, IOException {

		Document doc = new Document();

		FieldType textFieldType = new FieldType();
		textFieldType.setIndexed(true);
		textFieldType.setStored(true);
		textFieldType.setTokenized(true);
		boolean boolean_relevance = xmlDoc.isRelevant();

		// String cast for relevance. Empty = False, 1 = True
		String relevance = "";
		if (boolean_relevance && xmlDoc.getSearchTaskNumber() == OUR_SEARCH_TASK)
			relevance = "1";

		// Index all the searchable content into one field and the title to
		// another for reference
		doc.add(new Field("title", xmlDoc.getTitle(), textFieldType));
		doc.add(new Field("content", xmlDoc.getTitle() + " "
				+ xmlDoc.getAbstractText(), textFieldType));
		doc.add(new Field("relevance", relevance, textFieldType));

		w.addDocument(doc);
	}

	/**
	 * Counts the number of documents and number of relevant document
	 */
	private void analyzeDocumentCollection(List<DocumentInCollection> docs) {
		int documentCount = 0;
		int relevantDocumentCount = 0;

		for (DocumentInCollection doc : docs) {
			// Relevant if our search task and isRelevant is true
			if (doc.getSearchTaskNumber() == OUR_SEARCH_TASK
					&& doc.isRelevant()) {
				relevantDocumentCount++;
			}

			documentCount++;
		}

		// Save the values
		this.relevantDocumentCount = relevantDocumentCount;
	}

	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		if (args.length > 0) {
			LuceneSearchApp engine = new LuceneSearchApp();
			
			// Read and index XML collection
			DocumentCollectionParser parser = new DocumentCollectionParser();
			parser.parse(args[0]);
			List<DocumentInCollection> docs = parser.getDocuments();

			engine.analyzeDocumentCollection(docs);
			engine.index(docs);
			
			// Create searchers
			BM25Searcher bm25Searcher = new BM25Searcher(engine.index);
			VSMSearcher vsmSearcher = new VSMSearcher(engine.index);

			int queryNumber = 0;
			
			// Save the 11-point results and calculate average precision later
			List<List<PrecisionRecall>> vsm11points = new ArrayList<List<PrecisionRecall>>();
			List<List<PrecisionRecall>> bm2511points = new ArrayList<List<PrecisionRecall>>();
			
			for (String query : engine.queries) {
				int docsSize = docs.size();
				
				String name = "q" + queryNumber + " " + query;
				name = name.replace(" ", "_");
				
				PrecisionRecallCalculator vsmPrecisionRecall = new PrecisionRecallCalculator("vsm_" + name, engine.relevantDocumentCount);
				PrecisionRecallCalculator bm25PrecisionRecall = new PrecisionRecallCalculator("bm25_" + name, engine.relevantDocumentCount);
				SearchResults vsmResults = null;
				SearchResults bm25Results = null;
				
				// Inner loop is for calculating precision and recall for
				// different limit counts.
				for(int limit = 1; limit < docsSize; limit++) {
					vsmResults = vsmSearcher.VSMsearch(query, limit);
					bm25Results = bm25Searcher.BM25search(query, limit);
					
					vsmPrecisionRecall.calculate(vsmResults, limit);
					bm25PrecisionRecall.calculate(bm25Results, limit);
				}
				
				vsmPrecisionRecall.calculate11pointInterpolated();
				bm25PrecisionRecall.calculate11pointInterpolated();
				
				vsm11points.add(vsmPrecisionRecall.steps11Results);
				bm2511points.add(vsmPrecisionRecall.steps11Results);
				
				// Print results (to file)
				// Uncomment if you want to write these results to file
				/*
				vsmPrecisionRecall.printAllResults();
				bm25PrecisionRecall.printAllResults();
				vsmPrecisionRecall.printInterpolatedResults();
				bm25PrecisionRecall.printInterpolatedResults();
				*/
				
				// Print results (to console)
				
				System.out.println("\nQuery: ");
				System.out.println(query);
				
				System.out.println("\nVSM\n");
				
				vsmPrecisionRecall.print11pointInterpolatedAverage();
				
				System.out.println("\nTop documents:\n");
				
				for(int i = 0; i < 30; i++) {
					System.out.println(vsmResults.list.get(i));
				}
				
				System.out.println("\nBM25\n");
				
				bm25PrecisionRecall.print11pointInterpolatedAverage();
				
				System.out.println("\nTop documents:\n");
				
				for(int i = 0; i < 30; i++) {
					System.out.println(bm25Results.list.get(i));
				}
				
				System.out.println("-------------------------------------------------------------------------------------");

				queryNumber++;
			}

			List<PrecisionRecall> vsmElevenPointInterpolatedAvgPrecision = new ArrayList<PrecisionRecall>();
			List<PrecisionRecall> bm25ElevenPointInterpolatedAvgPrecision = new ArrayList<PrecisionRecall>();
			
			// Calculate arithmetic mean
			for(int recallStep = 0; recallStep < 11; recallStep++) {
				int queries = engine.queries.length;
				
				double vsmPrecisionAvg = 0;
				double bm25PrecisionAvg = 0;
				
				// Let's calculate recall avg, although they should be the same
				double vsmRecallAvg = 0;
				double bm25RecallAvg = 0;
				
				for(int query = 0; query < queries; query++) {
					PrecisionRecall vsm = vsm11points.get(query).get(recallStep);
					PrecisionRecall bm25 = bm2511points.get(query).get(recallStep);
					
					vsmPrecisionAvg += vsm.precision;
					vsmRecallAvg += vsm.recall;
					bm25PrecisionAvg += bm25.precision;
					bm25RecallAvg += bm25.recall;
					
					// Debugging System.out.println("VSM recall: " + vsm.recall);
					// Debugging System.out.println("BM25 recall: " + bm25.recall);
					
				}
				
				vsmPrecisionAvg /= ((double) queries);
				vsmRecallAvg /= ((double) queries);
				bm25PrecisionAvg /= ((double) queries);
				bm25RecallAvg /= ((double) queries);
				
				vsmElevenPointInterpolatedAvgPrecision.add(new PrecisionRecall(vsmPrecisionAvg, vsmRecallAvg));
				bm25ElevenPointInterpolatedAvgPrecision.add(new PrecisionRecall(bm25PrecisionAvg, bm25RecallAvg));
				
				// Debugging System.out.println("Step: " + recallStep + ", vsmPresicion: " + vsmPrecisionAvg + ", vsmRecall: " + vsmRecallAvg + ", bm25Precision: " + bm25PrecisionAvg + ", bm25Recall " + bm25RecallAvg);
			}
			
			System.out.println("\nVSM Eleven-point interpolated average precision:\n");
			
			System.out.println("Recall, Precision");
			for(PrecisionRecall stepResult : vsmElevenPointInterpolatedAvgPrecision) {
				System.out.println(stepResult.recall + ", " + stepResult.precision);
			}
			
			System.out.println("\nBM25 Eleven-point interpolated average precision:\n");
			
			System.out.println("Recall, Precision");
			for(PrecisionRecall stepResult : bm25ElevenPointInterpolatedAvgPrecision) {
				System.out.println(stepResult.recall + ", " + stepResult.precision);
			}
			
		} else
			System.out
					.println("ERROR: the path of a XML Feed file has to be passed "
							+ "as a command line argument.");
	}

}
