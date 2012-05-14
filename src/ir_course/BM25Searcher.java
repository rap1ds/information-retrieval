package ir_course;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicSimilarityProvider;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.TermContext;

public class BM25Searcher {
	
	// TODO: implement (Noora)
	public SearchResults BM25search(Directory index, String query, int hitLimit) 
	throws CorruptIndexException, IOException {
		SearchResults results = new SearchResults();

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
		
		// Score
		BM25Similarity similarity = new BM25Similarity();		
		BasicSimilarityProvider similarityProvider = new BasicSimilarityProvider(similarity);
		searcher.setSimilarityProvider(similarityProvider);
		ScoreDoc[] hits = searcher.search(bq, hitLimit).scoreDocs;

		for(ScoreDoc hit : hits) {
			Document doc = searcher.doc(hit.doc);
			results.list.add(doc.get("title"));
			
			// if not empty => relevant
			if (!doc.get("relevance").isEmpty()) {
				results.relevantResults++;
			}
		}
		
		 
		return results;
	}

}
