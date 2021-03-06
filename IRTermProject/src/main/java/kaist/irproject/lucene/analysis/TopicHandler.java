package kaist.irproject.lucene.analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * 
 * @author Emil Bunk
 *
 *
 *	Extract topics and relevant document information.
 *	Providing an easy access to the individual topics for evaluation needs.
 */
public class TopicHandler {
		
		private static Topic[] topics;
		
	public TopicHandler() throws FileNotFoundException, IOException{
		this("topics.txt", "qrels.txt", 50);
	}
	public TopicHandler(String topicFile, String queryRelevanceFile, int numberOfTopics) throws FileNotFoundException, IOException {
		topics = new Topic[numberOfTopics];
		topics = extractTopicData(topicFile);
		updateRelevanceData(queryRelevanceFile, topics);
		
	}

	private static Topic[] extractTopicData(String filename) throws FileNotFoundException,
			IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		
		String line;
		String topicData = "";
		int i = 0;

		while ((line = in.readLine()) != null){
			if(line.equals("</top>")) {
				topics[i] = new Topic(topicData);
				topicData = "";
				i++;
			} else if (!line.equals("") && !line.equals("<top>")) {
				topicData += (line);
			}
			
		}
		in.close();
		return topics;
	}
	
	private static void updateRelevanceData(String filename, Topic[] topics) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		int idx = 451;
		
		while((line = in.readLine()) != null) {
			if(line.equals("")) continue;
			if(!line.startsWith(Integer.toString(idx))){
				idx++;
			}
			topics[idx-451].addDoc(line.substring(6));
		}
		in.close();
		
		int total = 0;
		
		for(Topic topic:topics){
			
			total+= topic.getNonRelevantDocs().size()+topic.getRelevantDocs().size();
		}
	}

	public Topic getTopicByNumber(int topicNumber) {
		for (Topic topic:topics){
			if(topic.getTopicNumber() == topicNumber) return topic;
		}
		return null;
	}
	
	public Topic getRandomTopic() {
		Random ran = new Random();
		return topics[ran.nextInt(topics.length)];
	}
	
	public Topic[] getTopics() {
		return topics;
	}
	
	
}
