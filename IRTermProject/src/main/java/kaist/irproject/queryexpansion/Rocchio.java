package kaist.irproject.queryexpansion;

import java.util.ArrayList;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;

public class Rocchio {
	public BooleanQuery RocchioQuery expander(ArrayList<String> query, ScoreDoc relDocs, ScoreDoc nonRelDoc, double alpha, double beta, double gamma) {
		ArrayList<String> terms = new ArrayList<String>(query);
		
		ArrayList<Double> boosts = new ArrayList<Double>();
		for(int i = 0; i < terms.size(); i++) {
			boosts.add(1.0);
		}
	}
}
