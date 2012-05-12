package ir_course;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.store.Directory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity.ExactDocScorer;
import org.apache.lucene.search.similarities.Similarity.Stats;
import org.apache.lucene.util.TermContext;

public class BM25Searcher {
	
	// TODO: implement (Noora)
	public List<String> BM25search(Directory index, String query, int hitLimit) 
	throws CorruptIndexException, IOException {

		List<String> results = new LinkedList<String>();

		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);

		BooleanQuery bq = new BooleanQuery();
		
		// Process query and get term stats
		String[] keywords = query.split(" ");
		TermStatistics[] termStats = new TermStatistics[keywords.length];
		for(int i = 0; i<keywords.length; i++) {
			Term t = new Term("content", keywords[i]);
			TermContext context = new TermContext(searcher.getTopReaderContext());
			termStats[i] = searcher.termStatistics(t, context);
			bq.add(new TermQuery(t), BooleanClause.Occur.SHOULD);
		}
		
		// Get collection stats
		Terms terms = MultiFields.getTerms(reader, "content");
		CollectionStatistics collectionStats = new CollectionStatistics(
				"content", reader.maxDoc(), terms.getDocCount(), terms.getSumTotalTermFreq(), 
				terms.getSumDocFreq());
		
		// Score
		BM25Similarity similarity = new BM25Similarity();
		Stats stats = similarity.computeStats(collectionStats, hitLimit, termStats);
		AtomicReaderContext context = new AtomicReaderContext(reader);		
		// ExactDocScorer scorer = similarity.exactDocScorer(stats, "content", context);
		
		/*
		ScoreDoc[] hits = searcher.search(bq, hitLimit).scoreDocs;

		for(ScoreDoc hit : hits) {
			Document doc = searcher.doc(hit.doc);
			results.add(doc.get("title"));
		}
		*/
		 
		return results;
	}

}
