package kaist.irproject.lucene.index;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import kaist.irproject.lucene.analysis.PorterStemAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Indexer for HTML files.
 * Modified by T.Y.Tung (2009/06/22) for NTU 2009 term project
 */
public class IndexHTML {
	private IndexHTML() {
	}

	private static boolean deleting = false; // true during deletion pass
	private static IndexReader reader; // existing index
	private static IndexWriter writer; // new index being built
	
//	private static PageRanker pageRanker = null; // for PageRank

	/** Indexer for HTML files. */
	public static void main(String[] args) {
		try {
			String index = "index";
			boolean create = false;
			File root = null;

			//String usage = "IndexHTML [-create] [-index <index>] <root_directory>";
			
			String usage =  "-index indexHTML_StandardAnalyzer -create WT10G";
			String[] argv = usage.split (" ");

			if (argv.length == 0) {
				System.err.println("Usage: " + usage);
				return;
			}

			for (int i = 0; i < argv.length; i++) {
				if (argv[i].equals("-index")) { // parse -index option
					index = argv[++i];
				} else if (argv[i].equals("-create")) { // parse -create option
					create = true;
				} else if (i != argv.length - 1) {
					System.err.println("Usage: " + usage);
					return;
				} else
					root = new File(argv[i]);
			}
			Date start = new Date();

			if (!create) { // delete stale docs
				deleting = true;				
				indexDocs(root, index, create);
			}
			// modified by T.Y.Tung => PorterStemAnalyzer(stopwords)
			//File stopwords = new File("ftp.cs.cornell.edu_pub_smart_english.stop");
			//Analyzer analyzer = new PorterStemAnalyzer();
			//writer = new IndexWriter(index, analyzer, create, new IndexWriter.MaxFieldLength(1000000));
			
			Directory dir = FSDirectory.open(new File(index));
			Analyzer indexAnalyzer = new StandardAnalyzer(Version.LUCENE_48);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, indexAnalyzer);				
			writer = new IndexWriter(dir, iwc);
			
//			pageRanker = new PageRanker(); // for PageRank
			
			indexDocs(root, index, create); // add new docs

			//System.out.println("Optimizing index...");
			//writer.optimize();
			writer.close();

			Date end = new Date();

			System.out.print(end.getTime() - start.getTime());
			System.out.println(" total milliseconds");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}

	/*
	 * Walk directory hierarchy in uid order, while keeping uid iterator from /*
	 * existing index in sync. Mismatches indicate one of: (a) old documents to
	 * /* be deleted; (b) unchanged documents, to be left alone; or (c) new /*
	 * documents, to be indexed.
	 */

	private static void indexDocs(File file, String index, boolean create)
			throws Exception {
		/*if (!create) { // incrementally update

			reader = IndexReader.open(index); // open existing index

			indexDocs(file);

			if (deleting) { // delete rest of stale docs
				deleting = false;
			}
			
			reader.close(); // close existing index

		} else*/
			// don't have exisiting
			indexDocs(file);
	}

	private static void indexDocs(File file) throws Exception {
		if (file.isDirectory()) { // if a directory
			String[] files = file.list(); // list its files
			Arrays.sort(files); // sort the files
			for (int i = 0; i < files.length; i++)
				// recursively index them
				indexDocs(new File(file, files[i]));

		} // modified by T.Y.Tung
		else if (file.getPath().substring(file.getPath().lastIndexOf("\\")+1).startsWith("B") || // index TREC training data
				file.getPath().endsWith(".html") || // index .html files
				file.getPath().endsWith(".htm") || // index .htm files
				file.getPath().endsWith(".txt")) { // index .txt files
			 // add a new index
				 // modified by T.Y.Tung
				System.out.println("adding " + file.getPath());
				addDocuments(writer, file);
		}
	}

	// added by T.Y.Tung
	private static void addDocuments(IndexWriter writer, File file)
			throws IOException, InterruptedException {
		ArrayList<HashMap<String, String>> docsList = readDocs(file);
		for (int i=0; i<docsList.size(); i++) {
			HashMap<String, String> docsMap = docsList.get(i);
			String docNo = docsMap.get("DOCNO");
			String html = docsMap.get("DOC");
			InputStream is = new ByteArrayInputStream(html.getBytes());
			Document doc = HTMLDocument.Document(is, docNo);
			// add PageRank weights to index
//			boolean isPageRank = true; //quick debugging, it should be extracted to an input parameter
//			if (isPageRank) {
//				float score = pageRanker.getPageRank(docNo); //WTX010-B01-1 = 1.4805996E-6
//				doc.setBoost(score*1000*1000);
//				float boost = doc.getBoost();
//				//System.out.println("PageRank score for "+docNo+":\t"+boost);
//			}
			//System.out.println(doc.get("DOCNO"));
			//System.out.println(doc.get("contents"));
			//System.out.println(writer.toString());
			
			writer.addDocument(doc);
		}
	}

	// added by T.Y.Tung
	public static ArrayList<HashMap<String, String>> readDocs(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		ArrayList<HashMap<String, String>> docsList = new ArrayList<HashMap<String, String>>();
		try {
			String line = null;
			String sep = null;
			String newline = System.getProperty("line.separator");
			StringBuffer sb = null;
			HashMap<String, String> docsMap = null;
			while (null != (line=reader.readLine())) {
				sep = newline;
				if (line.equals("<DOC>")) {
					sb = new StringBuffer();
					docsMap = new HashMap<String, String>();
					sep = "";
				}
				sb.append(sep+line);
				if (line.startsWith("<DOCNO>")) {
		    		int endIndex = line.indexOf("</DOCNO>");
		    		String docNo = line.substring(7, endIndex);
		    		docsMap.put("DOCNO", docNo);
				}
				if (line.equals("</DOC>")) {
					docsMap.put("DOC", sb.toString());
					docsList.add(docsMap);
				}
			}
		} finally {
			reader.close();
		}
		return docsList;
	}

}
