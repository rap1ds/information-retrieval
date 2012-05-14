package ir_course;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.similarities.DefaultSimilarityProvider;
import org.apache.lucene.store.Directory;

public class VSMSearcher extends Searcher {
	
	public VSMSearcher(Directory index) {
		super(index);
	}

	public SearchResults VSMsearch(String query, int limit)
			throws CorruptIndexException, IOException {
		
		// Implements similarity with the Vector Space Model
		DefaultSimilarityProvider provider = new DefaultSimilarityProvider();
		this.setSimilarityProvider(provider);

		return this.search(query, limit);
	}

}
