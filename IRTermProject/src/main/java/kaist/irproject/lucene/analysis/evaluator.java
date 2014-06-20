package kaist.irproject.lucene.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class evaluator {
	public static void main(String args[]) throws FileNotFoundException, IOException {
		File file = new File("evaluation data");
		String fileName = file.list().length + " - " + (new Date()).toString();
		FileWriter extensionFile = new FileWriter("evaluation data/"+fileName + " - extension");
		FileWriter luceneFile = new FileWriter("evaluation data/"+fileName + " - lucene");
		setRows(extensionFile);
		setRows(luceneFile);
		
		Topic[] topics = (new TopicHandler()).getTopics();
		
		List<String> retrievedDocs = null; // INITIALIZE THIS
		for (Topic topic:topics){
			// retrievedDocs = search(topic.getQuery();
			computeAnalysis(luceneFile, topic, retrievedDocs);
			// retrievedDocs = expandQuery();
			computeAnalysis(extensionFile, topic, retrievedDocs);
		}
		
		luceneFile.close();
		extensionFile.close();
		
	}

	private static void computeAnalysis(FileWriter writer, Topic topic,
			List<String> docs) throws IOException {
		int relDocCount = topic.getTopicNumber();
		writer.append(Integer.toString(relDocCount));
		writer.append(',');
		writer.append(topic.getQuery());
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
