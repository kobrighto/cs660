package org.apache.lucene.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexerTREC {
	public static void main(String args[]) throws IOException {
		String INDEXPATH = "Index_TREC";
		Boolean CREATE = true; 
		String DOCPATH = "B01.gz";
		
		Directory dir = FSDirectory.open(new File(INDEXPATH));
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
	    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
	    
	    if (CREATE) {
	        // Create a new index in the directory, removing any
	        // previously indexed documents:
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        // Add new documents to an existing index:
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }
	    
	    IndexWriter writer = new IndexWriter(dir, iwc);
	    indexDoc(writer, DOCPATH);
	    
	    writer.close();
	   
	}

	private static void indexDoc(IndexWriter writer, String docCollection) throws IOException {
		InputStream fileStream = new FileInputStream(docCollection);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(decoder);
		
		// BufferedReader in = new BufferedReader(new FileReader(docCollection));
		String line;
		
		while ((line = in.readLine()) != null){
			if(line.equals("<DOC>")){
				Document doc = new Document();
				line = in.readLine();
				
				String docnum = line.substring(7, line.indexOf("</"));
				doc.add(new StringField("docnumber", docnum, Field.Store.YES));
				
				String content = "";
				while (!(line = in.readLine()).equals("</DOC>")) {
					content += line + " ";
				}
				doc.add(new TextField("contents", content, Field.Store.YES));
				
				if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		            System.out.println("adding " + docnum);
		            //System.out.println(doc);
		            writer.addDocument(doc);
		          } else {
		            System.out.println("updating " + docnum);
		            writer.updateDocument(new Term("docnumber", docnum), doc);
		          }
			}
		}
		
		in.close();
	}
}
