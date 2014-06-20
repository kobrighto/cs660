package kaist.irproject.lucene.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.indexer.Searcher;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;
import org.jsoup.parser.Parser;

public class evaluator {
	public static void main(String args[]) throws FileNotFoundException, IOException, ParseException {
		File file = new File("evaluation data");
		String fileName = file.list().length + " - " + (new Date()).toString();
		FileWriter extensionFile = new FileWriter("evaluation data/"+fileName + " - extension");
		FileWriter luceneFile = new FileWriter("evaluation data/"+fileName + " - lucene");
		setRows(extensionFile);
		setRows(luceneFile);
		
		Topic[] topics = (new TopicHandler()).getTopics();
		Searcher searchEngine = new Searcher();
		
		ScoreDoc[] retrievedDocs; // INITIALIZE THIS
		for (Topic topic:topics){
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
			QueryParser parser = new QueryParser(Version.LUCENE_48, "contents", analyzer);
			Query query = parser.parse(topic.getQuery().trim());
			retrievedDocs = Searcher.search(query);
			computeAnalysis(luceneFile, topic, searchEngine.toStringList(retrievedDocs));
			ScoreDoc[] relDocuments = null;
			ScoreDoc[] nonRelDocuments = null;
			choseTopic(retrievedDocs, relDocuments, nonRelDocuments);
			retrievedDocs = Searcher.expandQuery(query, relDocuments, nonRelDocuments);
			computeAnalysis(extensionFile, topic, searchEngine.toStringList(retrievedDocs));
		}
		
		luceneFile.close();
		extensionFile.close();
		
	}

	private static void choseTopic(ScoreDoc[] retrievedDocs, ScoreDoc[] relDocuments,
			ScoreDoc[] nonRelDocuments) {
		
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
