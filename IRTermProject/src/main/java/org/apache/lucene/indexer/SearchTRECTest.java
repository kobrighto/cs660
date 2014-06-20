package org.apache.lucene.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

import kaist.irproject.queryexpansion.Rocchio;

public class SearchTRECTest {
	public static void main(String args[]) throws IOException, ParseException {
		String index = "Index_TREC";
		String field = "contents";
		 
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    // :Post-Release-Update-Version.LUCENE_XY:
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48,
				IndexerTREC.stopWordsSet);
	    QueryParser parser = new QueryParser(Version.LUCENE_48, field, analyzer);
	    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	    System.out.println("Enter query: ");
	    
	    String line = in.readLine();
	    
	    line = line.trim();
	    
	    Query query = parser.parse(line);
	    System.out.println("Searching for: " + query.toString(field));
	    int n = 100;
	    TopDocs results = searcher.search(query, n);
	    ScoreDoc[] hits = results.scoreDocs;
	    
	    int numTotalHits = results.totalHits;
	    System.out.println(numTotalHits + " total matching documents");
	    int start = 0;
	    int end = Math.min(numTotalHits, n);
	    
	    for (int i = start; i < end; i++) {

	        Document doc = searcher.doc(hits[i].doc);
	        String docnum= doc.get("docnumber");
	        if (docnum != null) {
	          System.out.println((i+1) + ". " + docnum);
	        } else {
	          System.out.println((i+1) + ". " + "No path for this document");
	        }
	                  
	      }
	    LDA lda = new LDA();
	    lda.saveDocumentsToFile(hits);
	    lda.LDAModel();
	    Query expandedQuery = Rocchio.RocchioQueryExpander(query, hits, null, (float) 1, (float) 0.5, (float) 0.5, analyzer, searcher);
	    System.out.println(expandedQuery);
	    
	    
	    results = searcher.search(expandedQuery, 5);
	    hits = results.scoreDocs;
	    
	    numTotalHits = results.totalHits;
	    System.out.println(numTotalHits + " total matching documents");
	    start = 0;
	    end = Math.min(numTotalHits, 5);
	    Document doc;
	    
	    for (int i = start; i < end; i++) {

	        doc = searcher.doc(hits[i].doc);
	        String docnum= doc.get("docnumber");
	        if (docnum != null) {
	          System.out.println((i+1) + ". " + docnum);
	        } else {
	          System.out.println((i+1) + ". " + "No path for this document");
	        }
	                  
	      }
	}
}
