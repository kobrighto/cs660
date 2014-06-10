package org.apache.lucene.demo;

/*
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

import ireval.Main;
import ireval.RetrievalEvaluator;
import ireval.SetRetrievalEvaluator;
import ireval.RetrievalEvaluator.Judgment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/** Simple command-line based search demo. */
public class SearchFiles {

	private SearchFiles() {}

	/** Simple command-line based search demo. */
	public static void main(String[] argv) throws Exception {
		String usage =
				"Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] "
						+ "[-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\n"
						+ "See http://lucene.apache.org/core/4_1_0/demo/ for details.";
		usage = "-index Index -field contents -query english -topics dataset/testing_topics.txt "
				+ "-output output/testing_topics_result-[PorterStemAnalyzer].txt";
		String[] args = usage.split(" ");

		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String index = "index";
		String field = "contents";
		String queries = null;
		String rankingFile = "output.txt";
		String topicsFile = null;
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		int hitsPerPage = 10;

		for(int i = 0;i < args.length;i++) {
			if ("-index".equals(args[i])) {
				index = args[i+1];
				i++;
			} else if ("-field".equals(args[i])) {
				field = args[i+1];
				i++;
			} else if ("-queries".equals(args[i])) {
				queries = args[i+1];
				i++;
			} else if ("-query".equals(args[i])) {
				queryString = args[i+1];
				i++;
			}else if ("-topics".equals(args[i])) {
				topicsFile = args[i + 1];
				i++;
			}else if ("-output".equals(args[i])){
				rankingFile = args[i + 1];
				i++;
			}else if ("-repeat".equals(args[i])) {
				repeat = Integer.parseInt(args[i+1]);
				i++;
			} else if ("-raw".equals(args[i])) {
				raw = true;
			} else if ("-paging".equals(args[i])) {
				hitsPerPage = Integer.parseInt(args[i+1]);
				if (hitsPerPage <= 0) {
					System.err.println("There must be at least 1 hit per page.");
					System.exit(1);
				}
				i++;
			}
		}
		File f = new File(rankingFile);
		if (f.createNewFile()){
			System.out.println("File is created");
		}else{
			System.out.println("File already existed");
		}		

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = null;
		analyzer = new StandardAnalyzer(Version.LUCENE_48);

		if (topicsFile != null) {
			TrecTopicsReader qReader = new TrecTopicsReader();
			org.apache.lucene.benchmark.quality.QualityQuery[] qqs = 
					qReader.readQueries(new BufferedReader(new FileReader(topicsFile)));
			//				for (int i=0; i<qqs.length; i++) {
			//					System.out.print(qqs[i].getQueryID());
			//					System.out.println(" "+qqs[i].getValue("title"));
			//					System.out.println("<description> "+qqs[i].getValue("description"));
			//					System.out.println("<narrative> "+qqs[i].getValue("narrative"));
			//					System.out.println("");
			//				}

			//QueryParser parser = new QueryParser("contents", analyzer);
			String[] fields = {"contents", "title"};
			Map<String, Float> boosts = new HashMap<String, Float>();
			boosts.put(fields[0], Float.valueOf("1.2"));
			boosts.put(fields[1], Float.valueOf("0.016"));
			MultiFieldQueryParser multiParser = new MultiFieldQueryParser(Version.LUCENE_48, fields, analyzer, boosts);

			/*
			// Rocchio's pseudo relevance feedback QueryExpansion
			// TODO: alpha, beta, decay don't work because field 'contents' has no terms to be expanded
			Properties props = new Properties();
			props.load(new FileReader("src/query_expansion.prop"));
			//				props.setProperty("QE.method", "rocchio");
			//				props.setProperty("QE.decay", "0.04");
			if (!docNum.equals("") && !termNum.equals("")) {
				// command line parameters have higher priority than property file (query_expansion.prop)
				props.setProperty("QE.doc.num", docNum);
				props.setProperty("QE.term.num", termNum);
			}
			//				props.setProperty("rocchio.alpha", "1");
			//				props.setProperty("rocchio.beta", "0.75");

			// for WordNet Query Expansion
			Searcher wnSearcher = new IndexSearcher("indexWordNet");
			Analyzer wnAnalyzer = analyzer;
			 */

			// Search
			Query trecQuery = null;
			//Query expandedQuery = null; // for WordNet Query Expansion
			TopDocs td = null;
			for (int i=0; i<qqs.length; i++) {
				String queryStr = qqs[i].getValue("title")+" "+qqs[i].getValue("description")+" "+qqs[i].getValue("narrative");
				queryStr = queryStr.replaceAll("\\*", "").replaceAll("\\?", ""); // replace * and ?
				// query of TREC topic
				trecQuery = multiParser.parse(queryStr);

				// WordNet Query Expansion
				/*if (isWordNet) {
					// expanded query using WordNet
					expandedQuery = SynExpand.expand(qqs[i].getValue("title"), wnSearcher, wnAnalyzer, "contents", 0.2f);
					// combine the above two queries
					((BooleanQuery) query).add(expandedQuery, BooleanClause.Occur.SHOULD);
				}*/

				// First search
				System.out.println("\nSearching for : (" + qqs[i].getQueryID()+") "+trecQuery);
				td = doTopHitSearch(searcher, trecQuery, 100);

				// Rocchio's pseudo relevance feedback Query Expansion
				/*if (isRelevanceFeedback) {
					for (int k=1; k <= relevanceFeedbackRuns; k++) {
						Vector<Document> hits = new Vector<Document>();
						ScoreDoc[] sd = td.scoreDocs;
						for (int j = 0; j < sd.length; j++) {
							int docId = sd[j].doc;
							Document doc = searcher.doc(docId);
							hits.add(doc);
						}

						// Rocchio's pseudo relevance feedback QueryExpansion
						Similarity similarity = query.getSimilarity(searcher);
						QueryExpansion queryExpansion = new QueryExpansion(analyzer, searcher, similarity, props, "contents");
						query = queryExpansion.expandQuery(queryStr, hits, props);
						// Second search using Rocchio's pseudo relevance feedback
						System.out.println("Searching for : (" + qqs[i].getQueryID()+") "+query);
						if (k < relevanceFeedbackRuns) {
							td = doTopHitSearch(searcher, query, 10*k);
						} else { // k == runs (ready for output)
							td = doTopHitSearch(searcher, query, 100); //project setting
						}
					}
				}*/

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
		printEvaluation(baselineRankingFile, rankingFile, judgmentsFile);
		}else{
			System.err.println(usage);
			return;
		}
	}

				// :Post-Release-Update-Version.LUCENE_XY:
				/*Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

				BufferedReader in = null;
				if (queries != null) {
					in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), StandardCharsets.UTF_8));
				} else {
					in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
				}
				// :Post-Release-Update-Version.LUCENE_XY:
				QueryParser parser = new QueryParser(Version.LUCENE_48, field, analyzer);
				while (true) {
					if (queries == null && queryString == null) {                        // prompt the user
						System.out.println("Enter query: ");
					}

					String line = queryString != null ? queryString : in.readLine();

					if (line == null || line.length() == -1) {
						break;
					}

					line = line.trim();
					if (line.length() == 0) {
						break;
					}

					Query query = parser.parse(line);
					System.out.println("Searching for: " + query.toString(field));

					if (repeat > 0) {                           // repeat & time as benchmark
						Date start = new Date();
						for (int i = 0; i < repeat; i++) {
							searcher.search(query, null, 100);
						}
						Date end = new Date();
						System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
					}



					//doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null, rankingFile);

					if (queryString != null) {
						break;
					}
				}
				reader.close();*/

			//added by Minh Nguyen
			private static TopDocs doTopHitSearch(IndexSearcher searcher, Query query, int numHits) throws IOException {		
				/*TopDocsCollector collector = new TopDocsCollector(numHits);
		searcher.search(query, collector);
		TopDocs td = collector.topDocs();
		return td;*/

				TopDocs results = searcher.search(query, numHits);
				return results;
			}

			/**
			 * This demonstrates a typical paging search scenario, where the search engine presents 
			 * pages of size n to the user. The user can then go to the next page if interested in
			 * the next hits.
			 * 
			 * When the query is executed for the first time, then only enough results are collected
			 * to fill 5 result pages. If the user wants to page beyond this limit, then the query
			 * is executed another time and all hits are collected.
			 * 
			 */
			public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
					int hitsPerPage, boolean raw, boolean interactive, String rankingFile) throws IOException {

				// Collect enough docs to show 5 pages
				TopDocs results = searcher.search(query, 5 * hitsPerPage);
				ScoreDoc[] hits = results.scoreDocs;

				int numTotalHits = results.totalHits;
				System.out.println(numTotalHits + " total matching documents");

				int start = 0;
				int end = Math.min(numTotalHits, hitsPerPage);

				while (true) {
					if (end > hits.length) {
						System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
						System.out.println("Collect more (y/n) ?");
						String line = in.readLine();
						if (line.length() == 0 || line.charAt(0) == 'n') {
							break;
						}

						hits = searcher.search(query, numTotalHits).scoreDocs;
					}

					end = Math.min(hits.length, start + hitsPerPage);

					for (int i = start; i < end; i++) {
						if (raw) {                              // output raw format
							System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
							continue;
						}

						Document doc = searcher.doc(hits[i].doc);
						String path = doc.get("path");
						if (path != null) {
							System.out.println((i+1) + ". " + path);
							String title = doc.get("title");
							if (title != null) {
								System.out.println("   Title: " + doc.get("title"));
							}
						} else {
							System.out.println((i+1) + ". " + "No path for this document");
						}

					}

					if (!interactive || end == 0) {
						break;
					}

					if (numTotalHits >= end) {
						boolean quit = false;
						while (true) {
							System.out.print("Press ");
							if (start - hitsPerPage >= 0) {
								System.out.print("(p)revious page, ");  
							}
							if (start + hitsPerPage < numTotalHits) {
								System.out.print("(n)ext page, ");
							}

							System.out.println("(q)uit or enter number to jump to a page.");         

							String line = in.readLine();
							if (line.length() == 0 || line.charAt(0)=='q') {
								quit = true;
								break;
							}
							if (line.charAt(0) == 'p') {
								start = Math.max(0, start - hitsPerPage);
								break;
							} else if (line.charAt(0) == 'n') {
								if (start + hitsPerPage < numTotalHits) {
									start+=hitsPerPage;
								}
								break;
							} else {
								int page = Integer.parseInt(line);
								if ((page - 1) * hitsPerPage < numTotalHits) {
									start = (page - 1) * hitsPerPage;
									break;
								} else {
									System.out.println("No such page");
								}
							}
						}
						if (quit) break;
						end = Math.min(numTotalHits, start + hitsPerPage);
					}
				}
			}

			public static void printEvaluation(String baselineRankingFile, String rankingFile, String judgmentsFile) throws IOException {
				if ("".equals(baselineRankingFile) || "".equals(rankingFile) || "".equals(judgmentsFile)) {
					System.err.println("Ranking files or judgment file not exist.");
					return;
				}
				System.out.println(rankingFile);
				File f = new File(rankingFile);
				if (f.exists()){
					System.out.println("File Exists!");
				}else{
					System.out.println("File not exists");
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
			}
		}
