/*
 * Skeleton class for the Lucene search program implementation
 * Created on 2011-12-21
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */
package ir_course;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.DefaultSimilarityProvider;
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

	private int documentCount;
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
		if (boolean_relevance && xmlDoc.getSearchTaskNumber() == 8)
			relevance = "1";

		// Index all the searchable content into one field and the title to
		// another for reference
		doc.add(new Field("title", xmlDoc.getTitle(), textFieldType));
		doc.add(new Field("content", xmlDoc.getTitle() + " "
				+ xmlDoc.getAbstractText(), textFieldType));
		doc.add(new Field("relevance", relevance, textFieldType));

		// TODO: Is indexing relevance, query etc necessary for calculating
		// precicion / recall?

		w.addDocument(doc);
	}

	public SearchResults VSMsearch(String query, int limit)
			throws CorruptIndexException, IOException {
		SearchResults results = new SearchResults();

		IndexReader reader = IndexReader.open(index);

		IndexSearcher searcher = new IndexSearcher(reader);
		// Ilmeisesti Defaultprovider toteuttaa VSM- similarityn...
		DefaultSimilarityProvider provider = new DefaultSimilarityProvider();
		searcher.setSimilarityProvider(provider);

		BooleanQuery bq = new BooleanQuery();
		String[] words = query.split(" ");
		for (String word : words) {
			Term t = new Term("content", word);
			TermQuery tq = new TermQuery(t);
			bq.add(tq, BooleanClause.Occur.SHOULD);
		}

		ScoreDoc[] hits = searcher.search(bq, limit).scoreDocs;

		for (ScoreDoc hit : hits) {
			Document doc = searcher.doc(hit.doc);
			results.list.add(doc.get("title"));

			// if not empty => relevant
			if (!doc.get("relevance").isEmpty()) {
				results.relevantResults++;
			}
		}

		return results;
	}

	public double getAverage(List<Double> precisions) {
		double sum = 0.0;
		for (double precision : precisions)
			sum += precision;
		return sum / precisions.size();
	}

	public void printResults(List<String> results) {
		if (results.size() > 0) {
			Collections.sort(results);
			for (int i = 0; i < results.size(); i++)
				System.out.println(" " + (i + 1) + ". " + results.get(i));
		} else {
			System.out.println(" no results");
		}
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
		this.documentCount = documentCount;
		this.relevantDocumentCount = relevantDocumentCount;
	}

	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		if (args.length > 0) {
			LuceneSearchApp engine = new LuceneSearchApp();
			BM25Searcher searcher2 = new BM25Searcher();
			// Read and index XML collection
			DocumentCollectionParser parser = new DocumentCollectionParser();
			parser.parse(args[0]);
			List<DocumentInCollection> originalDocs = parser.getDocuments();

			// Why are we adding only documents relevant to our search task.
			// Doing this gives us wrong presicion/recall in my opinion-

			List<DocumentInCollection> docs = new LinkedList<DocumentInCollection>();
			for (DocumentInCollection doc : originalDocs) {
				if (doc.getSearchTaskNumber() == 8)
					docs.add(doc);
				;
			}

			engine.analyzeDocumentCollection(docs);
			engine.index(docs);

			// TODO: search and rank with VSM and BM25
			for (String query : engine.queries) {
				System.out.println();
				System.out.println(query);

				int docsSize = docs.size();
				
				PrecisionRecallCalculator vsmPrecisionRecall = new PrecisionRecallCalculator(engine.relevantDocumentCount);
				PrecisionRecallCalculator bm25PrecisionRecall = new PrecisionRecallCalculator(engine.relevantDocumentCount);
				SearchResults vsmResults = null;
				SearchResults bm25Results = null;
				
				// Inner loop is for calculating precision and recall for
				// different limit counts.
				for(int limit = 1; limit < docsSize; limit++) {
					vsmResults = engine.VSMsearch(query, limit);
					bm25Results = searcher2.BM25search(engine.index, query, limit);
					
					vsmPrecisionRecall.calculate(vsmResults, limit);
					bm25PrecisionRecall.calculate(bm25Results, limit);
				}
				
				// Print results
				System.out.println("VSM Results");
				
				if(vsmResults != null) {
					System.out.println("HITS " + vsmResults.list.size());
				}
				
				System.out.println("LIST SIZE: " + vsmPrecisionRecall.precisions.size()); // Pitaisi
																		// olla
																		// 11
				System.out.println("INTERPOLATION VALUE:"
						+ engine.getAverage(vsmPrecisionRecall.precisions));
				
				System.out.println();
				
				System.out.println("BM25 Results");
				
				if(vsmResults != null) {
					System.out.println("HITS " + bm25Results.list.size());
				}
				
				System.out.println("LIST SIZE: " + bm25PrecisionRecall.precisions.size()); // Pitaisi
																		// olla
																		// 11
				System.out.println("INTERPOLATION VALUE:"
						+ engine.getAverage(bm25PrecisionRecall.precisions));
				
				System.out.println("-------------------------------------------------------------------------------------");
			}

			// TODO: evaluate & compare
		} else
			System.out
					.println("ERROR: the path of a XML Feed file has to be passed "
							+ "as a command line argument.");
	}

}
