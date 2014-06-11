package kaist.irproject.queryexpansion;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;

public class Rocchio {
	public BooleanQuery RocchioQueryExpander(ArrayList<String> query, ScoreDoc[] relDocs, 
			ScoreDoc[] nonRelDocs, float alpha, float beta, float gamma, Analyzer analyzer, 
			IndexSearcher searcher) throws IOException {
		int Dr = relDocs.length; // number of relevant docs
		int Dnr = nonRelDocs.length; // number of NON relevant docs
		
		ArrayList<String> terms = new ArrayList<String>(query);
		
		ArrayList<Float> boosts = new ArrayList<Float>();
		for(int i = 0; i < terms.size(); i++) {
			boosts.add(alpha);
		}
		
		updateBoosts(relDocs, beta, analyzer, searcher, Dr, terms, boosts);
		updateBoosts(nonRelDocs, -gamma, analyzer, searcher, Dnr, terms, boosts);
		
		BooleanQuery Query = new BooleanQuery();
		
		for(int idx = 0; idx < terms.size(); idx++){
			TermQuery tq = new TermQuery(new Term("text", terms.get(idx)));
			tq.setBoost(boosts.get(idx));
			Query.add(tq, Occur.SHOULD);
		}
		
		return Query;
	}

	private void updateBoosts(ScoreDoc[] Docs, Float param,
			Analyzer analyzer, IndexSearcher searcher, int docCount,
			ArrayList<String> terms, ArrayList<Float> boosts)
			throws IOException {
		for(ScoreDoc hit:Docs){
			Document doc = searcher.doc(hit.doc);
		    
		    TokenStream tokenStream = analyzer.tokenStream("contents", doc.get("contents"));
		    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	
		    tokenStream.reset();
		    while (tokenStream.incrementToken()) {
		        String term = charTermAttribute.toString();
		        int idx = terms.indexOf(term);
		        if(idx > -1){
		        	boosts.set(idx, boosts.get(idx)+ param/docCount);
		        } else {
		        	terms.add(term);
		        	boosts.add(param/docCount);
		        }
		    }
		}
	}
}
