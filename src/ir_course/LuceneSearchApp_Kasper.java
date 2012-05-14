/*
 * Skeleton class for the Lucene search program implementation
 * Created on 2011-12-21
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */
package ir_course;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
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
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicSimilarityProvider;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.DefaultSimilarityProvider;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.SimilarityProvider;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
//import org.apache.lucene.search.similarities;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.TermContext;
import org.apache.lucene.util.Version;

public class LuceneSearchApp_Kasper {
	// Queries for A3 can be hard-coded
	public int relevantsInDocument;
	public int relevantsInSearch;
	private double document_count;
	private final String[] queries = {
			"simulation industrial environment",
			"computer and physical model simulation",
			"industrial process simulation",
			"manufacturing process models"
	};

	private StandardAnalyzer analyzer;
	private Directory index;

	public LuceneSearchApp_Kasper() {
		document_count=200;
		this.relevantsInSearch=0;
	}

	public void index(List<DocumentInCollection> docs) 
	throws CorruptIndexException, LockObtainFailedException, IOException {

		analyzer = new StandardAnalyzer(Version.LUCENE_40);

		index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		IndexWriter w = new IndexWriter(index, config);

		for(DocumentInCollection xmlDoc : docs) {
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
		if (boolean_relevance && xmlDoc.getSearchTaskNumber()==8) relevance="1";
		
		// Index all the searchable content into one field and the title to another for reference
		doc.add(new Field("title", xmlDoc.getTitle(), textFieldType));
		doc.add(new Field("content", xmlDoc.getTitle() + " " + xmlDoc.getAbstractText(), textFieldType));
		doc.add(new Field("relevance", relevance, textFieldType));
		// TODO: Is indexing relevance, query etc necessary for calculating precicion / recall?

		w.addDocument(doc);
	}

	// TODO: implement
	public List<String> VSMsearch(String query,int limit) throws CorruptIndexException, IOException {
		this.relevantsInSearch=0;
		
		List<String> results = new LinkedList<String>();
		IndexReader reader = IndexReader.open(index);

		IndexSearcher searcher = new IndexSearcher(reader);
		// Ilmeisesti Defaultprovider toteuttaa VSM- similarityn...
		DefaultSimilarityProvider provider = new DefaultSimilarityProvider();
		searcher.setSimilarityProvider(provider);
		
		BooleanQuery bq = new BooleanQuery();
		String[] words = query.split(" ");
		TermStatistics[] termStats = new TermStatistics[words.length];

		for (String word : words) {
			Term t = new Term("content", word);
			TermQuery tq = new TermQuery(t);
			bq.add(tq,BooleanClause.Occur.SHOULD);
		}
		
		ScoreDoc[] hits = searcher.search(bq, limit).scoreDocs;
		for(ScoreDoc hit : hits) {
			Document doc = searcher.doc(hit.doc);
			results.add(doc.get("title"));
			
			// if not empty => relevant
			if (!doc.get("relevance").isEmpty()) this.relevantsInSearch++;
		}
	
		return results;
	}
	
	public double getAverage(List<Double> precisions) {
		double sum = 0.0;
		for (double precision : precisions) sum +=precision;
		return sum/precisions.size();
	}

	public List<String> search(List<String> inTitle, List<String> notInTitle, 
			List<String> inDescription, List<String> notInDescription, 
			String startDate, String endDate) throws CorruptIndexException, IOException {

		printQuery(inTitle, notInTitle, inDescription, notInDescription, startDate, endDate);

		List<String> results = new LinkedList<String>();

		// implement the Lucene search here
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);


		BooleanQuery bq = new BooleanQuery();

		// Title
		if(inTitle != null) {
			for(String title : inTitle) {
				bq.add(new TermQuery(new Term("title", title)), BooleanClause.Occur.MUST);
			}
		}

		if(notInTitle != null) {
			for(String title : notInTitle) {
				bq.add(new TermQuery(new Term("title", title)), BooleanClause.Occur.MUST_NOT);
			}
		}

		// Desc
		if(inDescription != null) {
			for(String desc : inDescription) {
				bq.add(new TermQuery(new Term("description", desc)), BooleanClause.Occur.MUST);
			}
		}
		if(notInDescription != null) {
			for(String desc : notInDescription) {
				bq.add(new TermQuery(new Term("description", desc)), BooleanClause.Occur.MUST_NOT);
			}
		}

		// Pubdate
		if(startDate != null || endDate != null) {
			// start/end date or null, which leaves the search half open
			Long start = startDate != null ? stringToTime(startDate, false) : null;
			Long end = endDate != null ? stringToTime(endDate, true) : null;

			// Ideal value in most cases for 64 bit data types (long, double) is 6 or 8.
			int precisionStep = 8;
			boolean inclusive = true;

			bq.add(NumericRangeQuery.newLongRange("pubdate", precisionStep, start, end, 
					inclusive, inclusive), BooleanClause.Occur.MUST);
		}

		int bigEnoughHitCount = 100;

		ScoreDoc[] hits = searcher.search(bq, bigEnoughHitCount).scoreDocs;

		for(ScoreDoc hit : hits) {
			Document doc = searcher.doc(hit.doc);
			results.add(doc.get("title"));
		}


		return results;
	}

	private static long stringToTime(String timeString, boolean isEndDate) {
		String[] parts = timeString.split("-");

		int year = Integer.parseInt(parts[0]);
		int month = Integer.parseInt(parts[1]) - 1; // 0-based
		int day = Integer.parseInt(parts[2]);

		if(isEndDate) {
			day += 1;
		}

		GregorianCalendar c = new GregorianCalendar(year, month, day);

		return c.getTimeInMillis();
	}

	public void printQuery(List<String> inTitle, List<String> notInTitle, 
			List<String> inDescription, List<String> notInDescription, 
			String startDate, String endDate) {

		System.out.print("Search (");
		if (inTitle != null) {
			System.out.print("in title: "+inTitle);
			if (notInTitle != null || inDescription != null || notInDescription != null || startDate != null || endDate != null)
				System.out.print("; ");
		}
		if (notInTitle != null) {
			System.out.print("not in title: "+notInTitle);
			if (inDescription != null || notInDescription != null || startDate != null || endDate != null)
				System.out.print("; ");
		}
		if (inDescription != null) {
			System.out.print("in description: "+inDescription);
			if (notInDescription != null || startDate != null || endDate != null)
				System.out.print("; ");
		}
		if (notInDescription != null) {
			System.out.print("not in description: "+notInDescription);
			if (startDate != null || endDate != null)
				System.out.print("; ");
		}
		if (startDate != null) {
			System.out.print("startDate: "+startDate);
			if (endDate != null)
				System.out.print("; ");
		}
		if (endDate != null)
			System.out.print("endDate: "+endDate);
		System.out.println("):");
	}

	public void printResults(List<String> results) { 
		if (results.size() > 0) {
			Collections.sort(results);
			for (int i=0; i<results.size(); i++)
				System.out.println(" " + (i+1) + ". " + results.get(i));
		}
		else {
			System.out.println(" no results");
		}
	}

	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
		if (args.length > 0) {
			int allRelevants = 0; // 138
			LuceneSearchApp_Kasper engine = new LuceneSearchApp_Kasper();
			BM25Searcher searcher2 = new BM25Searcher();
			// Read and index XML collection
			DocumentCollectionParser parser = new DocumentCollectionParser();
			parser.parse(args[0]);
			List<DocumentInCollection> docs = parser.getDocuments();
			List<DocumentInCollection> docs2 = new LinkedList<DocumentInCollection>();
			for (DocumentInCollection doc : docs) {
				if (doc.getSearchTaskNumber()==8) docs2.add(doc);
				if (doc.getSearchTaskNumber()==8 && doc.isRelevant()) allRelevants++;
			}
			engine.index(docs2);

			// TODO: search and rank with VSM and BM25
			for(String query : engine.queries) {
				System.out.println();
				System.out.println(query);
				
				List<Double> precisions = new ArrayList<Double>() ;
				
				int hitLimit = docs2.size(); // = 200
				double count = 0;
				
				// Inner loop is for calculating precision and recall for different limit counts.
				for (int i=1; i<hitLimit;i++) {
				List<String> VSMresults = engine.VSMsearch(query,i);
				//List<String> BM25results = searcher2.BM25search(engine.index, query, hitLimit);
				

				// TODO: print results
				
				// Jokainen query pitais looppaa hitLimit:ia kasvattamalla niin kunnes ollaan saatu recall-arvoksi 1.0
				// aina kun recall saa mahdollisimman lahelle arvon 0.0,0.1,0.2.. 1.0 => precisions.add
				double precision_vsm = (double)engine.relevantsInSearch/(double)i;
				double recall_vsm = (double)engine.relevantsInSearch/(double)allRelevants;
				if (recall_vsm>count/10 -0.01 && recall_vsm<count/10 +0.01 && count <11) {
					precisions.add(precision_vsm);
					//System.out.println("P: " + precision_vsm);
					//System.out.println("R : " + recall_vsm);
					count++;
				}
				
				//System.out.println("HITS: " + VSMresults.size());
				
				//engine.printResults(VSMresults);
				//System.out.println("-------------------------------------------------------------------------------------");
				//engine.printResults(BM25results);
				
				// HITS SHOULD BE 200
				if (i==hitLimit-1) System.out.println("HITS "+ VSMresults.size());
				}
				System.out.println("LIST SIZE: " + precisions.size()); // Pitaisi olla 11
				System.out.println("INTERPOLATION VALUE:" + engine.getAverage(precisions));
			}

			// TODO: evaluate & compare


			/*
			 * Assignment 1 code for reference:
			 * 
			RssFeedParser parser = new RssFeedParser();
			parser.parse(args[0]);
			List<RssFeedDocument> docs = parser.getDocuments();

			engine.index(docs);

			List<String> inTitle;
			List<String> notInTitle;
			List<String> inDescription;
			List<String> notInDescription;
			List<String> results;

			// 1) search documents with words "kim" and "korea" in the title
			inTitle = new LinkedList<String>();
			inTitle.add("kim");
			inTitle.add("korea");
			results = engine.search(inTitle, null, null, null, null, null);
			engine.printResults(results);

			// 2) search documents with word "kim" in the title and no word "korea" in the description
			inTitle = new LinkedList<String>();
			notInDescription = new LinkedList<String>();
			inTitle.add("kim");
			notInDescription.add("korea");
			results = engine.search(inTitle, null, null, notInDescription, null, null);
			engine.printResults(results);

			// 3) search documents with word "us" in the title, no word "dawn" in the title and word "" and "" in the description
			inTitle = new LinkedList<String>();
			inTitle.add("us");
			notInTitle = new LinkedList<String>();
			notInTitle.add("dawn");
			inDescription = new LinkedList<String>();
			inDescription.add("american");
			inDescription.add("confession");
			results = engine.search(inTitle, notInTitle, inDescription, null, null, null);
			engine.printResults(results);

			// 4) search documents whose publication date is 2011-12-18
			results = engine.search(null, null, null, null, "2011-12-18", "2011-12-18");
			engine.printResults(results);

			// 5) search documents with word "video" in the title whose publication date is 2000-01-01 or later
			inTitle = new LinkedList<String>();
			inTitle.add("video");
			results = engine.search(inTitle, null, null, null, "2000-01-01", null);
			engine.printResults(results);

			// 6) search documents with no word "canada" or "iraq" or "israel" in the description whose publication date is 2011-12-18 or earlier
			notInDescription = new LinkedList<String>();
			notInDescription.add("canada");
			notInDescription.add("iraq");
			notInDescription.add("israel");
			results = engine.search(null, null, null, notInDescription, null, "2011-12-18");
			engine.printResults(results);
			 */
		}
		else
			System.out.println("ERROR: the path of a XML Feed file has to be passed " +
			"as a command line argument.");
	}

}
