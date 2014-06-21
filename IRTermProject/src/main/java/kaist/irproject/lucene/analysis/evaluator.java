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

public class evaluator {
	public static void main(String args[]) throws FileNotFoundException, IOException, ParseException {
		File file = new File("evaluation data");
		int filenumber = file.list().length;
		if(filenumber != 0) {filenumber = filenumber/2;}
		String fileName = filenumber + " - " + (new Date()).toString();
		FileWriter extensionFile = new FileWriter("evaluation data/"+fileName + " - extension.csv");
		FileWriter luceneFile = new FileWriter("evaluation data/"+fileName + " - lucene.csv");
		setRows(extensionFile);
		setRows(luceneFile);
		
		Topic[] topics = (new TopicHandler()).getTopics();
		Searcher searchEngine = new Searcher();
		
		ScoreDoc[] retrievedDocs; // INITIALIZE THIS
		int shortcut = 0;
		for (Topic topic:topics){
			if(shortcut == 5){break;}
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
			QueryParser parser = new QueryParser(Version.LUCENE_48, "contents", analyzer);
			Query query = parser.parse(topic.getQuery().trim());
			retrievedDocs = Searcher.indexSearch(query);
			computeAnalysis(luceneFile, topic, searchEngine.toStringList(retrievedDocs));
			
			LDA lda = new LDA();
		    lda.saveDocumentsToFile(retrievedDocs);
		    lda.LDAModel();
		    
		    System.out.println("Current topic: ");
			System.out.println(topic);
		    String topicNumber = choseTopic();
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

	private static String choseTopic() throws IOException {
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

	private static void computeAnalysis(FileWriter writer, Topic topic,
			List<String> docs) throws IOException {
		int relDocCount = topic.getTopicNumber();
		writer.append(Integer.toString(relDocCount));
		writer.append(',');
		writer.append(topic.getQuery());
		writer.append(',');
		writer.append(Integer.toString(topic.getRelevantDocs().size()));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 100))/100.));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 20))/20.));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 10))/10.));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 5))/5.));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 100))/(double) relDocCount));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 20))/(double) relDocCount));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 10))/(double) relDocCount));
		writer.append(',');
		writer.append(Double.toString(topic.getRelevantDocCount(docs.subList(0, 5))/(double) relDocCount));
		writer.append('\n');
		
		
	}

	private static void setRows(FileWriter writer) throws IOException {
		writer.append("Topic");
	    writer.append(',');
	    writer.append("Title");
	    writer.append(',');
	    writer.append("Relevant Docs");
	    writer.append(',');
	    writer.append("Precision @100");
	    writer.append(',');
	    writer.append("Precision @20"); 
	    writer.append(',');
	    writer.append("Precision @10");
	    writer.append(',');
	    writer.append("Precision @5");
	    writer.append(',');
	    writer.append("Recall @100");
	    writer.append(',');
	    writer.append("Recall @20"); 
	    writer.append(',');
	    writer.append("Recall @10");
	    writer.append(',');
	    writer.append("Recall @5");
	    writer.append('\n');
	}
}
