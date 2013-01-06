package websearch;

/**
 * @author Sebastian Mai
 */

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class QueryEngine {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private QueryParser parser;
	
	public QueryEngine(String indexPath) {
		reader = null;
		searcher = null;
		analyzer = null;
		parser = null;
		
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
			searcher = new IndexSearcher(reader);
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			parser = new QueryParser(Version.LUCENE_40, "text", analyzer);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public void query(String queryString) {
		Query query;
		try {
			query = parser.parse(queryString);
			TopDocs results = null;
			results = searcher.search(query, 100);
			ScoreDoc[] hits = results.scoreDocs;
			System.out.println("Number of results: " + hits.length);
			for(ScoreDoc h : hits) {
				Document doc = searcher.doc(h.doc);
				
				System.out.println(doc.getField("title").stringValue());
				System.out.println(doc.getField("url").stringValue());
				System.out.println(h);
				
				System.out.println();
			}
		} catch (ParseException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
