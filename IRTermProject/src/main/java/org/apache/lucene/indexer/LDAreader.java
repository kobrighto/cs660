package org.apache.lucene.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LDAreader {
	public LDAreader() {
	
	}
	
	
	public static List<Integer> getTopics(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		List<Integer> topics = new ArrayList<Integer>();
		String line;
		while ((line = in.readLine()) != null){
			String[] prob = line.split(" ");
			int idx = 0;
			double idx_prob = Double.parseDouble(prob[idx]);
			for(int i = 1; i < prob.length; i++) {
				double newVal = Double.parseDouble(prob[i]);
				if(newVal > idx_prob) {
					idx_prob = newVal;
					idx = i;
				}
			}
			topics.add(idx);
		}
		in.close();
		return topics;
	}
}
