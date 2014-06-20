package org.apache.lucene.indexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class IndexLDATopics {
	private int topics = 20; // default - loadMetaData overwrites
	private int ndocs = 262; // default - loadMetaData overwrites

	public IndexLDATopics() {
		 loadMetaData();
		

	}

	private int[] assignTopicsToDoc(String[] topics, int ndocs) {
		BufferedReader br = null;
		String sCurrentLine;
		int[] topicsAssigned = new int[ndocs];
		int docCount = 0;
		try {
			br = new BufferedReader(new FileReader("model//model-final.theta"));
			while ((sCurrentLine = br.readLine()) != null) {
				// System.out.println(sCurrentLine);
				String[] currentLineSplit = sCurrentLine.split(" ");
				float max = 0;
				int topicNumber = 0;
				int index = 0;
				for (String p : currentLineSplit) {
					try {
						float parsedP = Float.parseFloat(p);
						if (parsedP > max) {
							max = parsedP;
							index = topicNumber;

						}

					}

					catch (Exception e) {
						System.out.println("Parsing failed for " + p);
					}
					topicNumber++;
				}
				//System.out.println(max);
				topicsAssigned[docCount] = index;
				docCount++;
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
		return topicsAssigned;
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

	private String[] loadTopics(int numberOfTopics) {
		BufferedReader br = null;
		String topics = "";
		String[] topicWords = new String[numberOfTopics];
		try {
			int topicCount = 0;
			String sCurrentLine;

			br = new BufferedReader(new FileReader("model//model-final.twords"));
			String currentTopic = "";
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);

				if (!sCurrentLine.toLowerCase().contains("topic")) {
					topicWords[topicCount - 1] += sCurrentLine;
				} else {
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
		//System.out.println(splitTopics.length);
		return topicWords;
	}

	public static void main(String args[]) throws IOException {
		IndexLDATopics indexLDATopics = new IndexLDATopics();
		String [] topicWordsArray = indexLDATopics.loadTopics(indexLDATopics.topics);
		int[] t = indexLDATopics.assignTopicsToDoc(topicWordsArray, indexLDATopics.ndocs);
		System.out.println(t[0]);
	}
}
