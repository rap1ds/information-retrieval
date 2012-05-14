package ir_course;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.SimilarityProvider;
import org.apache.lucene.store.Directory;

public class Searcher {

	protected Directory index;
	protected SimilarityProvider similarityProvider;
	
	public Searcher(Directory index) {
		this.index = index;
	}
	
	protected void setSimilarityProvider(SimilarityProvider similarityProvider) {
		this.similarityProvider = similarityProvider;
	}
	
	protected SearchResults search(String query, int limit) throws CorruptIndexException, IOException {
		BooleanQuery bq = new BooleanQuery();
		String[] words = query.split(" ");
		for (String word : words) {
			Term t = new Term("content", word);
			TermQuery tq = new TermQuery(t);
			bq.add(tq, BooleanClause.Occur.SHOULD);
		}
		bq.add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD);
		
		SearchResults results = new SearchResults();

		IndexReader reader = IndexReader.open(this.index);
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarityProvider(this.similarityProvider);
		
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
	
}
