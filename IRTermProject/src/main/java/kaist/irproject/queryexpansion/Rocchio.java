package kaist.irproject.queryexpansion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * @author Emil Bunk
 * 
 * Query expansion class using Rocchio's Algorithm.
 * Utilizing Lucene boosted boolean query as a replacement of the vector space model.
 */
public class Rocchio {
	public static Query RocchioQueryExpander(Query query, ArrayList<ScoreDoc> relDocs, 
			ArrayList<ScoreDoc> nonRelDocs, float alpha, float beta, float gamma, Analyzer analyzer, 
			IndexSearcher searcher) throws IOException, ParseException {
		int Dr = relDocs.size(); // number of relevant docs
		int Dnr = 1; if(nonRelDocs != null) Dnr = nonRelDocs.size(); // number of NON relevant docs

		
		String[] queryTerms = query.toString().split(" ");
		
		HashMap<String, Float> terms = new HashMap<String, Float>();
		
		for(int i = 0; i < terms.size(); i++) {
			terms.put(queryTerms[i], new Float(alpha));
		}
		
		updateBoosts(relDocs, beta, analyzer, searcher, Dr, terms);
		updateBoosts(nonRelDocs, -gamma, analyzer, searcher, Dnr, terms);
		
		ValueComparator bvc =  new ValueComparator(terms);
        TreeMap<String,Float> sorted_map = new TreeMap<String,Float>(bvc);
        sorted_map.putAll(terms);
        
		BooleanQuery Query = new BooleanQuery();
		
		int count = 0;
		for(Map.Entry<String, Float> term : sorted_map.entrySet()){
			// System.out.println(term.getKey() + " " + term.getValue());
			TermQuery tq = new TermQuery(new Term("contents", term.getKey()));
			tq.setBoost(term.getValue());
			Query.add(tq, Occur.SHOULD);
			count++; if(count == 1023) break;
		}
		
		return Query;
	}

	private static void updateBoosts(ArrayList<ScoreDoc> docs, Float param,
			Analyzer analyzer, IndexSearcher searcher, int docCount,
			HashMap<String, Float> terms)
			throws IOException {
		if(docs == null) return;
		for(ScoreDoc hit:docs){
			Document doc = searcher.doc(hit.doc);
		    
		    TokenStream tokenStream = analyzer.tokenStream("contents", doc.get("contents"));
		    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	
		    tokenStream.reset();
		    while (tokenStream.incrementToken()) {
		        String term = charTermAttribute.toString();
		        if(terms.containsKey(term)){
		        	terms.put(term, terms.get(term) + param/docCount);
		        } else {
		        	terms.put(term, new Float(param/docCount));
		        }
		    }
		    tokenStream.end();
		    tokenStream.close();
		}
	}
}

class ValueComparator implements Comparator<String> {
    Map<String, Float> base;
    public ValueComparator(Map<String, Float> base) {
        this.base = base;
    }
    
    public int compare(String a, String b) {
        if (Math.abs(base.get(a)) >= Math.abs(base.get(b))) {
            return -1;
        } else {
            return 1;
        }
    }
}
