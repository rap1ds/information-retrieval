package ir_course;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicSimilarityProvider;
import org.apache.lucene.store.Directory;

public class BM25Searcher extends Searcher {
	
	public BM25Searcher(Directory index) {
		super(index);
	}

	public SearchResults BM25search(String query, int limit) 
	throws CorruptIndexException, IOException {
		
		// Score
		BM25Similarity similarity = new BM25Similarity();		
		BasicSimilarityProvider similarityProvider = new BasicSimilarityProvider(similarity);
		this.setSimilarityProvider(similarityProvider);
		
		return this.search(query, limit);
	}

}
