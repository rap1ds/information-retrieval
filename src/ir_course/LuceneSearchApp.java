/*
 * Skeleton class for the Lucene search program implementation
 * Created on 2011-12-21
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */
package ir_course;

import java.io.IOException;
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
			"industrial process simulation", "manufacturing process models" };

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

	public double getAverage(List<Double> precisions) {
		double sum = 0.0;
		for (double precision : precisions)
			sum += precision;
		return sum / precisions.size();
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

			for (String query : engine.queries) {
				int docsSize = docs.size();
				
				PrecisionRecallCalculator vsmPrecisionRecall = new PrecisionRecallCalculator(engine.relevantDocumentCount);
				PrecisionRecallCalculator bm25PrecisionRecall = new PrecisionRecallCalculator(engine.relevantDocumentCount);
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
				
				// Print results
				
				System.out.println("\nQuery: ");
				System.out.println(query);
				
				System.out.println("\nVSM\n");
				
				vsmPrecisionRecall.printPrecisionRecallSteps();
				
				System.out.println("\nTop documents:\n");
				
				for(int i = 0; i < 30; i++) {
					System.out.println(vsmResults.list.get(i));
				}
				
				System.out.println("\nAverage precision: "
				 		+ engine.getAverage(vsmPrecisionRecall.precisions));
				
				System.out.println("\nBM25\n");
				
				bm25PrecisionRecall.printPrecisionRecallSteps();
				
				System.out.println("\nTop documents:\n");
				
				for(int i = 0; i < 30; i++) {
					System.out.println(bm25Results.list.get(i));
				}
				
				System.out.println("\nAverage precision: "
						+ engine.getAverage(bm25PrecisionRecall.precisions));
				
				System.out.println("-------------------------------------------------------------------------------------");
			}
		} else
			System.out
					.println("ERROR: the path of a XML Feed file has to be passed "
							+ "as a command line argument.");
	}

}
