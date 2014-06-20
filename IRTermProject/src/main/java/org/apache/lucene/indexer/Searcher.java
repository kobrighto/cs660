package org.apache.lucene.indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kaist.irproject.queryexpansion.Rocchio;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {
	private static final String FIELD = "contents";
	static private String INDEX = "Index_TREC";
	 
	private IndexReader reader;
    private static IndexSearcher searcher;
    private static Analyzer analyzer;
    private static QueryParser parser;
	
	public Searcher(String index) throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
	    QueryParser parser = new QueryParser(Version.LUCENE_48, FIELD, analyzer);
	}
	
	public Searcher() throws IOException {
		this(INDEX);
	}
	
	public static ScoreDoc[] search(Query query) throws ParseException, IOException {
	    System.out.println("Searching for: " + query.toString(FIELD));
	    
	    TopDocs results = searcher.search(query, 5);
	    return results.scoreDocs;
	}
	
	public static ScoreDoc[] expandQuery(Query query, ScoreDoc[] relDocuments, ScoreDoc[] nonRelDocuments) throws IOException, ParseException {
		Query expandedQuery = Rocchio.RocchioQueryExpander(query, relDocuments, nonRelDocuments, (float) 1, (float) 0.5, (float) 0.5, analyzer, searcher);
		return search(expandedQuery);
	}
	
	public List<String> toStringList(ScoreDoc[] docs) throws IOException {
		List<String> docList = new ArrayList<String>();
		
		for (int i = 0; i < Math.min(docs.length, 100); i++) {

	        Document doc = searcher.doc(docs[i].doc);
	        String docnum= doc.get("docnumber");
	        if (docnum != null) {
	        	docList.add(docnum);
	        }
	                  
	    }
		return docList;
	}
	
	public Document getDoc(ScoreDoc doc) throws IOException {
		return searcher.doc(doc.doc);
	}
}
