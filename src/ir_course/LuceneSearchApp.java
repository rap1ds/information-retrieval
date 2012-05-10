/*
 * Skeleton class for the Lucene search program implementation
 * Created on 2011-12-21
 * Jouni Tuominen <jouni.tuominen@aalto.fi>
 */
package ir_course;

import java.io.IOException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneSearchApp {
	
	// Queries for A3 can be hard-coded
	private final String[] queries = {
			"simulation industrial environment",
			"computer and physical model simulation",
			"industrial process simulation",
			"manufacturing process models"
	};

	private StandardAnalyzer analyzer;
	private Directory index;

	public LuceneSearchApp() {

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

		doc.add(new Field("title", xmlDoc.getTitle(), textFieldType));
		doc.add(new Field("description", xmlDoc.getAbstractText(), textFieldType));

		/*
		Lucene API: For indexing a Date or Calendar, just get the unix timestamp as long using 
		Date.getTime() or Calendar.getTimeInMillis() and index this as a numeric 
		value with LongField and use NumericRangeQuery to query it.
		 */

		w.addDocument(doc);
	}
	
	public List<String> VSMsearch() throws CorruptIndexException, IOException {
		// TODO: implement
		return null;
	}
	
	public List<String> BM25search(String query) throws CorruptIndexException, IOException {

		// TODO: implement (Noora)
		List<String> results = new LinkedList<String>();

		// implement the Lucene search here
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);

		BooleanQuery bq = new BooleanQuery();

		return results;
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
			LuceneSearchApp engine = new LuceneSearchApp();
			
			// Read and index XML collection
			DocumentCollectionParser parser = new DocumentCollectionParser();
			parser.parse(args[0]);
			List<DocumentInCollection> docs = parser.getDocuments();
			
			engine.index(docs);
			
			// TODO: search and rank with VSM and BM25
			for(String query : engine.queries) {
				List<String> VSMresults = engine.VSMsearch();
				List<String> BM25results = engine.BM25search(query);
			
				// TODO: print results
				engine.printResults(VSMresults);
				engine.printResults(BM25results);
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
