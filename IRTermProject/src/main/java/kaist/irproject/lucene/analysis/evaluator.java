package kaist.irproject.lucene.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.indexer.LDA;
import org.apache.lucene.indexer.Searcher;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;
import org.apache.lucene.indexer.IndexLDATopics;
/**
 * 
 * @author Emil Bunk
 *
 */
public class evaluator {
	/**
	 * Runs the search for all predefined queries produced for the Trec dataset
	 * Having user input for choice of topic and stores performance in 
	 * CSV files for later analysis
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String args[]) throws FileNotFoundException, IOException, ParseException {
		File file = new File("evaluation data");
		int filenumber = file.list().length;
		if(filenumber != 0) {filenumber = filenumber/2;}
		String fileName = Integer.toString(filenumber);
		FileWriter extensionFile = new FileWriter("evaluation data/"+fileName + "_extension.csv");
		FileWriter luceneFile = new FileWriter("evaluation data/"+fileName + " - lucene.csv");
		setRows(extensionFile);
		setRows(luceneFile);
		
		Topic[] topics = (new TopicHandler()).getTopics();
		Searcher searchEngine = new Searcher();
		
		ScoreDoc[] retrievedDocs; // INITIALIZE THIS
		int shortcut = 0;
		for (Topic topic:topics){
			//if(shortcut > 5){break;}
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
			QueryParser parser = new QueryParser(Version.LUCENE_48, "contents", analyzer);
			Query query = parser.parse(topic.getQuery().trim());
			retrievedDocs = Searcher.indexSearch(query);
			if (retrievedDocs.length < 50) {continue;}
			
			computeAnalysis(luceneFile, topic, searchEngine.toStringList(retrievedDocs));
			
			LDA lda = new LDA();
		    lda.saveDocumentsToFile(retrievedDocs);
		    lda.LDAModel();
		    
		    System.out.println("Current topic: ");
			System.out.println(topic);
		    String topicNumber = chooseTopic();
			retrievedDocs = Searcher.expandQuery(query, retrievedDocs, topicNumber, IndexLDATopics.assignTopicsToDoc(100));
			computeAnalysis(extensionFile, topic, searchEngine.toStringList(retrievedDocs));
			System.out.println();
			System.out.println();
			shortcut++;
		}
		
		luceneFile.close();
		extensionFile.close();
		System.out.println("Evaluation complete.");
		
	}
	
	/**
	 * Request user input for choice of topic.
	 * @return
	 * @throws IOException
	 */
	private static String chooseTopic() throws IOException {
		System.out.println("Choose between these topics:");
		String[] topics = IndexLDATopics.loadTopics(3);
		for(int i=0; i<3; i++){
			System.out.println(Integer.toString(i) + ": " + topics[i]);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	    System.out.println("Enter topicnumber: ");
	    
	    String topicNumber = in.readLine();
	    
	   	return topicNumber.trim();
		
	}
	
	/**
	 * Write performance to CSV file.
	 * @param writer
	 * @param topic
	 * @param docs
	 * @throws IOException
	 */
	private static void computeAnalysis(FileWriter writer, Topic topic,
			List<String> docs) throws IOException {
		// Topic number
		writer.append(Integer.toString(topic.getTopicNumber()));
		writer.append(',');
		
		// Topic title/query
		writer.append(topic.getQuery());
		writer.append(',');
		
		// Number of relevant docs in the full set
		writer.append(Integer.toString(topic.getRelevantDocs().size()));
		writer.append(',');
		
		// Number of retrieved documents from search
		writer.append(Integer.toString(docs.size()));
		writer.append(',');
		
		// Number of relevant documents in the retrieved set
		// At first 100, 20, 10 and 5 retrieved documents.
		writer.append(Integer.toString(topic.getRelevantDocCount(docs.subList(0, Math.min(docs.size(), 100)))));
		writer.append(',');
		writer.append(Integer.toString(topic.getRelevantDocCount(docs.subList(0, Math.min(docs.size(), 20)))));
		writer.append(',');
		writer.append(Integer.toString(topic.getRelevantDocCount(docs.subList(0, Math.min(docs.size(), 10)))));
		writer.append(',');
		writer.append(Integer.toString(topic.getRelevantDocCount(docs.subList(0, Math.min(docs.size(), 5)))));
		writer.append('\n');
		
		
	}

	private static void setRows(FileWriter writer) throws IOException {
		writer.append("Topic");
	    writer.append(',');
	    writer.append("Title");
	    writer.append(',');
	    writer.append("Relevant Docs");
	    writer.append(',');
	    writer.append("Retrieved Docs");
	    writer.append(',');
	    writer.append("rel @100");
	    writer.append(',');
	    writer.append("rel @20"); 
	    writer.append(',');
	    writer.append("rel @10");
	    writer.append(',');
	    writer.append("rel @5");
	    writer.append('\n');
	}
}
