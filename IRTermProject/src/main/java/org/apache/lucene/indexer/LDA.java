package org.apache.lucene.indexer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import jgibblda.Estimator;
import jgibblda.Inferencer;
import jgibblda.LDACmdOption;
import jgibblda.Model;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.tartarus.snowball.ext.PorterStemmer;

public class LDA {

	public static void main(String[] args) {
		//new LDA().LDAModel();
	}
	public void saveDocumentsToFile(ScoreDoc[] hits) throws IOException{
		String index = "Index_TREC";
		String field = "contents";
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48,
				IndexerTREC.stopWordsSet);
	    List<String> stopwords = new ArrayList<String>();
	    for(int i = 0; i < IndexerTREC.STOP_WORDS.length; i++) {
	    	stopwords.add(IndexerTREC.STOP_WORDS[i]);
	    }
	    
	    String docFileName = "model\\newdocs.dat";
		File file = new File(docFileName);
		FileWriter filerWriterDocs = new FileWriter(file.getAbsoluteFile(),
				false);
		BufferedWriter bufferedWriterDocs = new BufferedWriter(filerWriterDocs);
		PrintWriter out = new PrintWriter(bufferedWriterDocs);
		Document doc;
		PorterStemmer stemmer = new PorterStemmer();

	    
		
		System.out.println("Saving " + hits.length + " documents");
		for (int i=0; i<hits.length; i++){
			String docContent = "";

			
			doc = searcher.doc(hits[i].doc);
			String content= doc.get("contents").toLowerCase();
			TokenStream tokenStream = analyzer.tokenStream("contents", doc.get("contents"));
		    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	
		    tokenStream.reset();
		    while (tokenStream.incrementToken()) {
		        String term = charTermAttribute.toString();
		        if(!stopwords.contains(term.toLowerCase())){
		        	docContent = docContent + " " + term;
		        }
		    }
		    tokenStream.end();
		    tokenStream.close();
		    
			System.out.println(docContent.substring(1));
			bufferedWriterDocs.write(docContent
					+ "\n");
		}
		analyzer.close();
		bufferedWriterDocs.close();
		RandomAccessFile f = new RandomAccessFile(new File(docFileName), "rw");
		f.seek(0); // to the beginning
		String documentCount = Integer.toString(hits.length) + "\n";
		f.write(documentCount.getBytes());
		f.close();
	}

	public void LDAModel() {

		// TODO Auto-generated method stub
		LDACmdOption option = new LDACmdOption();
		CmdLineParser parser = new CmdLineParser(option);
		System.out.println("Working Directory = "
				+ System.getProperty("user.dir"));
		String[] inputSplits = ("-est -alpha 0.5 -beta 0.1 -ntopics 3 -niters 1000 -savestep 100 -twords 5 -dfile newdocs.dat -inf -dir "
				+ System.getProperty("user.dir") + "\\model").split(" ");
		String[] args = inputSplits;
		System.out.println(args[0]);
		try {
			if (args.length == 0) {
				System.out.println("No arguments");
				return;
			}

			parser.parseArgument(args);

			if (option.est || option.estc) {
				Estimator estimator = new Estimator();
				estimator.init(option);
				estimator.estimate();
			} else if (option.inf) {
				Inferencer inferencer = new Inferencer();
				inferencer.init(option);

				Model newModel = inferencer.inference();

				for (int i = 0; i < newModel.phi.length; ++i) {
					// phi: K * V
					System.out.println("-----------------------\ntopic" + i
							+ " : ");
					for (int j = 0; j < 10; ++j) {
						System.out.println(inferencer.globalDict.id2word.get(j)
								+ "\t" + newModel.phi[i][j]);
					}
				}
			}
		} catch (CmdLineException cle) {
			System.out.println("Command line error: " + cle.getMessage());

			return;
		} catch (Exception e) {
			System.out.println("Error in main: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

}
