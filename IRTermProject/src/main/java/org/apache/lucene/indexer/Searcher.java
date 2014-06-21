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
    
    private static ScoreDoc[] retrievedDocuments = null;
    private static String[] topics = new String[3];
    private static String topicNumber = "";
	
	public Searcher(String index) throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
	    QueryParser parser = new QueryParser(Version.LUCENE_48, FIELD, analyzer);
	}
	
	public Searcher() throws IOException {
		this(INDEX);
	}
	
	public static ScoreDoc[] indexSearch(Query query) throws ParseException, IOException {
	    System.out.println("Searching for: " + query.toString(FIELD));
	    
	    TopDocs results = searcher.search(query, 5);
	    retrievedDocuments = results.scoreDocs;
	    return retrievedDocuments;
	}
	
	public static ScoreDoc[] expandQuery(Query query) throws IOException, ParseException {
		ArrayList<ScoreDoc> relDocuments = new ArrayList<ScoreDoc>();
		ArrayList<ScoreDoc> nonRelDocuments = new ArrayList<ScoreDoc>();
		splitRetrievedDocuments(relDocuments, nonRelDocuments);
		Query expandedQuery = Rocchio.RocchioQueryExpander(query, relDocuments, nonRelDocuments, (float) 1, (float) 0.5, (float) 0.5, analyzer, searcher);
		retrievedDocuments = indexSearch(expandedQuery);
		return retrievedDocuments;
	}
	
	private static void splitRetrievedDocuments(ArrayList<ScoreDoc> relDocuments,
			ArrayList<ScoreDoc> nonRelDocuments) {
		
		for(int i = 0; i < retrievedDocuments.length; i++) {
			if(topicNumber.equals("")){
				System.out.println("No topic was selected, use searcher.setTopic()");
			} else if(topics[i].equals(topicNumber)){
				relDocuments.add(retrievedDocuments[i]);
			} else {
				nonRelDocuments.add(retrievedDocuments[i]);
			}
		}
		
	}
	
	public void setTopic(String topicNumber) {
		this.topicNumber = topicNumber;
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
