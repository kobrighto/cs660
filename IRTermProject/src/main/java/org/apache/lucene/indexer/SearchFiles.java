package org.apache.lucene.indexer;

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

//import ireval.*;
//import ireval.RetrievalEvaluator.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import kaist.irproject.lucene.analysis.PorterStemAnalyzer;
import kaist.irproject.lucene.trec.QualityQuery;
import kaist.irproject.lucene.trec.SubmissionReport;
import kaist.irproject.lucene.trec.TrecTopicsReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Simple command-line based search demo. 
 * Modified by T.Y.Tung (2009/06/22) for NTU 2009 term project
 */
public class SearchFiles {

	private SearchFiles() {
	}

	/** Simple command-line based search demo. */
	public static void main(String[] argv) throws Exception {
		String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles " +
				"[-index dir] [-analyzer name] [-topics file] [-output file] " +
				"[-WordNet yes-no] [-RelevanceFeedback intTimes docNum termNum]";
		usage += "\n\n\t-analyzer [StandardAnalyzer | PorterStemAnalyzer]";
		
		usage = "-index indexHTML_StandardAnalyzer -analyzer StandardAnalyzer -topics dataset/testing_topics.txt "
				+ "-output output/testing/testing_topics_result-[StandardAnalyzer].txt -WordNet no";
		String[] args = usage.split(" ");
				
		if (args.length > 0
				&& ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}
		if (args.length == 0) {
			System.err.println(usage);
			return;
		}

		String index = "indexHTML";
		String analyzerName = "";
		String topicsFile = null;
		String rankingFile = "output.txt";
		boolean isWordNet = false;
		boolean isRelevanceFeedback = false;
		int relevanceFeedbackRuns = 0;
		String docNum = "";
		String termNum = "";

		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				index = args[i + 1];
				i++;
			} else if ("-analyzer".equals(args[i])) {
				analyzerName = args[i + 1];
				i++;
			} else if ("-topics".equals(args[i])) {
				topicsFile = args[i + 1];
				i++;
			} else if ("-output".equals(args[i])) {
				rankingFile = args[i + 1];
				i++;
			} else if ("-WordNet".equals(args[i])) {
				if (args[i + 1].equals("yes"))
					isWordNet = true;
				i++;
			} else if ("-RelevanceFeedback".equals(args[i])) {
				isRelevanceFeedback = true;
				try { // must
					if (args[i + 1] != null)
						relevanceFeedbackRuns = Integer.parseInt(args[i + 1]);
				} catch (Exception e) {
					System.err.println(usage);
					return;
				}					
				try { // optional
					if (args[i + 2] != null)
						docNum = args[i + 2];
					if (args[i + 3] != null)
						termNum = args[i + 3];
				} catch (Exception e) {
				}
				i++;
			}
		}
		
		//IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = null;
		// modified by T.Y.Tung
		//File stopwords = new File("ftp.cs.cornell.edu_pub_smart_english.stop");
		if (analyzerName.equals("PorterStemAnalyzer")) {
			analyzer = new PorterStemAnalyzer();
		} else if (analyzerName.equals("StandardAnalyzer")) {
			analyzer = new StandardAnalyzer(Version.LUCENE_48);
		} else {
			System.err.println(usage);
			return;
		}

		if (topicsFile != null) {
			TrecTopicsReader qReader = new TrecTopicsReader();
			QualityQuery qqs[] = qReader.readQueries(new BufferedReader(new FileReader(topicsFile)));
//			for (int i=0; i<qqs.length; i++) {
//				System.out.print(qqs[i].getQueryID());
//				System.out.println(" "+qqs[i].getValue("title"));
//				System.out.println("<description> "+qqs[i].getValue("description"));
//				System.out.println("<narrative> "+qqs[i].getValue("narrative"));
//				System.out.println("");
//			}
			
			//QueryParser parser = new QueryParser("contents", analyzer);
			String[] fields = {"contents", "title"};
			Map<String, Float> boosts = new HashMap<String, Float>();
			boosts.put(fields[0], Float.valueOf("1.2"));
			boosts.put(fields[1], Float.valueOf("0.016"));
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_48, fields, analyzer, boosts);

			// Rocchio's pseudo relevance feedback QueryExpansion
			// TODO: alpha, beta, decay don't work because field 'contents' has no terms to be expanded
			Properties props = new Properties();
			props.load(new FileReader("src/query_expansion.prop"));
//			props.setProperty("QE.method", "rocchio");
//			props.setProperty("QE.decay", "0.04");
			if (!docNum.equals("") && !termNum.equals("")) {
				// command line parameters have higher priority than property file (query_expansion.prop)
				props.setProperty("QE.doc.num", docNum);
				props.setProperty("QE.term.num", termNum);
			}
//			props.setProperty("rocchio.alpha", "1");
//			props.setProperty("rocchio.beta", "0.75");
			
			// for WordNet Query Expansion
			/*IndexSearcher wnSearcher = new IndexSearcher("indexWordNet");
			Analyzer wnAnalyzer = analyzer;*/
			
			File f = new File(rankingFile);
			if (f.exists()) {
				f.delete();
			}
			// Search
			Query query = null;
			Query expandedQuery = null; // for WordNet Query Expansion
			TopDocs td = null;
			for (int i=0; i<qqs.length; i++) {
				String queryStr = qqs[i].getValue("title")+" "+qqs[i].getValue("description")+" "+qqs[i].getValue("narrative");
				queryStr = queryStr.replaceAll("\\*", "").replaceAll("\\?", ""); // replace * and ?
				// query of TREC topic
				query = parser.parse(queryStr);
				
				// WordNet Query Expansion
				/*if (isWordNet) {
					// expanded query using WordNet
					expandedQuery = SynExpand.expand(qqs[i].getValue("title"), wnSearcher, wnAnalyzer, "contents", 0.2f);
					// combine the above two queries
					((BooleanQuery) query).add(expandedQuery, BooleanClause.Occur.SHOULD);
				}*/
				
				// First search
				System.out.println("\nSearching for : (" + qqs[i].getQueryID()+") "+query);
				td = doTopHitSearch(searcher, query, 100);
				
				// Rocchio's pseudo relevance feedback Query Expansion
				if (isRelevanceFeedback) {
					for (int k=1; k <= relevanceFeedbackRuns; k++) {
						Vector<Document> hits = new Vector<Document>();
						ScoreDoc[] sd = td.scoreDocs;
						for (int j = 0; j < sd.length; j++) {
							int docId = sd[j].doc;
							Document doc = searcher.doc(docId);
							hits.add(doc);
						}
		
						// Rocchio's pseudo relevance feedback QueryExpansion
						/*Similarity similarity = query.getSimilarity(searcher);
						QueryExpansion queryExpansion = new QueryExpansion(analyzer, searcher, similarity, props, "contents");
						query = queryExpansion.expandQuery(queryStr, hits, props);
						// Second search using Rocchio's pseudo relevance feedback
						System.out.println("Searching for : (" + qqs[i].getQueryID()+") "+query);
						if (k < relevanceFeedbackRuns) {
							td = doTopHitSearch(searcher, query, 10*k);
						} else { // k == runs (ready for output)
							td = doTopHitSearch(searcher, query, 100); //project setting
						}*/
					}
				}
				
				// Output
				PrintWriter writer = new PrintWriter(new FileWriter(rankingFile, true));
				SubmissionReport report = new SubmissionReport(writer, "test");
				report.report(qqs[i], td, "DOCNO", searcher);
				writer.close();
			}
			
			// IR Evaluation
			String type = "";
			if (topicsFile.indexOf("training") > -1)
				type = "training";
			else
				type = "testing";
			String baselineRankingFile = "output/"+type+"/"+type+"_topics_result-[StandardAnalyzer].txt";
			String judgmentsFile = "dataset/qrels_"+type+"_topics.txt";
			System.out.println(args);
			
			//printEvaluation(baselineRankingFile, rankingFile, judgmentsFile);
			//System.out.println(baselineRankingFile);
			//System.out.println(rankingFile);
			//System.out.println(judgmentsFile);
			
		} else {
			System.err.println(usage);
			analyzer.close();
			return;
		}
		
	}

	// added by T.Y.Tung
	private static TopDocs doTopHitSearch(IndexSearcher searcher, Query query, int numHits) throws IOException {		
		TopScoreDocCollector collector = TopScoreDocCollector.create(numHits, false);
		searcher.search(query, collector);
		TopDocs td = collector.topDocs();
		return td;
	}
	
	// added by T.Y.Tung
	/*public static void printEvaluation(String baselineRankingFile, String rankingFile, String judgmentsFile) throws IOException {
		if ("".equals(baselineRankingFile) || "".equals(rankingFile) || "".equals(judgmentsFile)) {
			System.err.println("Ranking files or judgment file not exist.");
			return;
		}
		TreeMap<String, ArrayList<RetrievalEvaluator.Document>> ranking = Main.loadRanking(rankingFile);
		TreeMap<String, ArrayList<Judgment>> judgments = Main.loadJudgments(judgmentsFile);
		
		SetRetrievalEvaluator improvedEvaluator = Main.create(ranking, judgments);
		System.out.println("\n"+Main.singleEvaluation(improvedEvaluator, true));
		
		//TreeMap<String, ArrayList<RetrievalEvaluator.Document>> baselineRanking = Main.loadRanking(baselineRankingFile);
		//SetRetrievalEvaluator baselineEvaluator = Main.create(baselineRanking, judgments);
		//System.out.println(Main.comparisonEvaluation(baselineEvaluator, improvedEvaluator, baselineRankingFile, rankingFile));
		
		System.out.println(rankingFile);
		DecimalFormat df = new DecimalFormat("#0.0000");
		System.out.println("MAP = "+df.format(improvedEvaluator.meanAveragePrecision()));
		System.out.println("NDCG = "+df.format(improvedEvaluator.meanNormalizedDiscountedCumulativeGain()));
		System.out.println("NDCG15 = "+df.format(improvedEvaluator.meanNormalizedDiscountedCumulativeGain(15)));
	}*/

}