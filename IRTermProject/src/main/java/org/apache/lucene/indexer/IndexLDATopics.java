package org.apache.lucene.indexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class IndexLDATopics {
	private int topics = 0;

	public IndexLDATopics() {
		loadMetaData();
		if (topics > 0) {
			loadTopics(topics);
		}

	}

	private void loadMetaData() {
		BufferedReader br = null;
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader("model//model-final.others"));
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
				String[] currentLineSplit = sCurrentLine.split("=");
				if (currentLineSplit[0].equals("ntopics")) {
					topics = Integer.parseInt(currentLineSplit[1]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}

	private void loadTopics(int numberOfTopics) {
		BufferedReader br = null;
		String topics = "";
		String[] topicWords = new String[numberOfTopics];
		try {
			int topicCount = 0;
			String sCurrentLine;

			br = new BufferedReader(new FileReader("model//model-final.twords"));
			String currentTopic = "";
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
				
				if (!sCurrentLine.toLowerCase().contains("topic")){
					topicWords[topicCount-1] += sCurrentLine;
				}
				else {
					topicWords[topicCount] = "";
					topicCount++;
					currentTopic = "";
					
				}
				topics += sCurrentLine;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		String[] splitTopics = topics.split("\t");
		System.out.println(splitTopics.length);
	}

	public static void main(String args[]) throws IOException {
		new IndexLDATopics();
	}
}
