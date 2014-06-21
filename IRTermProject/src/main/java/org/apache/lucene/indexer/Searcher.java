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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {
		private static final String FIELD = "contents";
		private static final String INDEX = "Index_TREC";
		 
		private IndexReader reader;
	    private static IndexSearcher searcher;
	    private static Analyzer analyzer;
		
	public Searcher(String index) throws IOException {
		reader = DirectoryReader.open(FSDirectory.open(new File(index)));
	    searcher = new IndexSearcher(reader);
	    analyzer = new StandardAnalyzer(Version.LUCENE_48);
	}
	
	public Searcher() throws IOException {
		this(INDEX);
	}
	
	public static ScoreDoc[] indexSearch(Query query) throws IOException {
	    System.out.println("Searching for: " + query.toString(FIELD));
	    
	    TopDocs results = searcher.search(query, 100);
	    return results.scoreDocs;
	}
	
	public static ScoreDoc[] expandQuery(Query query, ScoreDoc[] retrievedDocuments, String topicNumber, int[] topics) throws IOException, ParseException {
		ArrayList<ScoreDoc> relDocuments = new ArrayList<ScoreDoc>();
		ArrayList<ScoreDoc> nonRelDocuments = new ArrayList<ScoreDoc>();
		splitRetrievedDocuments(retrievedDocuments, topicNumber, topics, relDocuments, nonRelDocuments);
		Query expandedQuery = Rocchio.RocchioQueryExpander(query, relDocuments, nonRelDocuments, (float) 2, (float) 0.5, (float) 0.1, analyzer, searcher);
		return indexSearch(expandedQuery);
	}
	
	private static void splitRetrievedDocuments(ScoreDoc[] retrievedDocuments, String topicNumber, int[] topics, ArrayList<ScoreDoc> relDocuments,
			ArrayList<ScoreDoc> nonRelDocuments) {
		
		for(int i = 0; i < retrievedDocuments.length; i++) {
			if(topicNumber.equals("")){
				System.out.println("No topic was selected, use searcher.setTopicNumber()");
			} else if(Integer.toString(topics[i]).equals(topicNumber)){
				relDocuments.add(retrievedDocuments[i]);
			} else {
				nonRelDocuments.add(retrievedDocuments[i]);
			}
		}
		
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
