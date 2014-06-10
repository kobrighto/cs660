package kaist.irproject.lucene.analysis;

import java.util.ArrayList;

public class Topic {
	private int topicNumber;
	private String query;
	private String description;
	private String narrative;
	private ArrayList<String> relevantDocs = new ArrayList<String>();
	private ArrayList<String> nonRelevantDocs = new ArrayList<String>();
	
	public Topic(String topicData) {
		int title = topicData.indexOf("<title>");
		int desc = topicData.indexOf("<desc>");
		int narr = topicData.indexOf("<narr>");
		
		topicNumber = Integer.parseInt(topicData.substring(14, 17));
		query = topicData.substring(title+8, desc-1);
		description = topicData.substring(desc+20, narr-1);
		narrative = topicData.substring(narr+18);
	}
	
	public void setRelevantDocs(ArrayList<String> docs) {
		relevantDocs = docs;
	}
	
	public ArrayList<String> getRelevantDocs() {
		return relevantDocs;
	}
	
	public void setNonRelevantDocs(ArrayList<String> docs) {
		nonRelevantDocs = docs;
	}
	
	public ArrayList<String> getNonRelevantDocs() {
		return nonRelevantDocs;
	}
	
	public int getTopicNumber(){
		return topicNumber;
	}
	
	public String getQuery(){
		return query;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getNarrative(){
		return narrative;
	}
	
	public String toString() {
		int rdocs = 0; int nrdocs = 0;
		if(relevantDocs != null) rdocs = relevantDocs.size();
		if(nonRelevantDocs != null) nrdocs = nonRelevantDocs.size();
		
		return String.format("Topic #%d - %s\nDescription: %s\nNarrative: %s\nRelevantdocs: %d\nNonrelevantdocs: %d",
				topicNumber, query, description, narrative, rdocs, nrdocs);
	
	}

	public void addDoc(String line) {
		String doc = line.substring(0, line.length()-2);
		if (line.endsWith("0")) {
			nonRelevantDocs.add(doc);
		} else {
			relevantDocs.add(doc);
		}
	}
	
	public ArrayList<String> getRelevanceIntersection(ArrayList<String> docs, int topicNumber){
		ArrayList<String> relDocs = new ArrayList<String>(docs);
		relDocs.retainAll(relevantDocs);
		return relDocs;
	}
	
}
