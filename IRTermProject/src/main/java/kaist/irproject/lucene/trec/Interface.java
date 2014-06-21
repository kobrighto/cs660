package kaist.irproject.lucene.trec;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.indexer.IndexLDATopics;
import org.apache.lucene.indexer.IndexerTREC;
import org.apache.lucene.indexer.Searcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Interface {
	public static void main (String[] args){
		try{

			String command = "";
			String indexCommand = "-index [yes|no] -source [directory] -dest [directory] -analyzer "
					+ "[StandardAnalyzer|PorterStemAnalyzer]";
			String searchCommand = "-search [StandardAnalyzer] -type [query|evaluation]";
			searchCommand = "-search StandardAnalyzer -type query";

			String[] searchCommandSplit = searchCommand.split(" ");

			String analyzerChoice = "";
			String type = "";

			for (int i=0; i<searchCommandSplit.length; i++){
				if(searchCommandSplit[i].equalsIgnoreCase("-search")){
					analyzerChoice = searchCommandSplit[++i];
				}else if (searchCommandSplit[i].equalsIgnoreCase("-type")){
					type = searchCommandSplit[++i];
				}else {
					System.err.println("Search Format invalid!");
					return;
				}
			}
			System.out.println(analyzerChoice);
			System.out.println(type);

			if (type.equalsIgnoreCase("query")){
				String field = "contents";
				String index = "Index_TREC";
				Analyzer analyzer = null;

				if (analyzerChoice.equalsIgnoreCase("StandardAnalyzer")){
					analyzer = new StandardAnalyzer(Version.LUCENE_48, IndexerTREC.stopWordsSet);
				}
				QueryParser parser = new QueryParser(Version.LUCENE_48, field, analyzer);
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
				System.out.println("Enter query: ");
				String line = in.readLine();
				line = line.trim();
				Query query = parser.parse(line);
				
				ScoreDoc[] hits = searchTREC(query, field,index,analyzerChoice);
				
				int[] topics = IndexLDATopics.assignTopicsToDoc(hits.length);
				ScoreDoc[] hitsExpand = Searcher.expandQuery(query, hits, "1", topics);
				
				
				
			}

			/* Code use for indexing
		String command = "";
		String usage = "-index [yes|no] -source [directory] -dest [directory] -analyzer "
				+ "[StandardAnalyzer|PorterStemAnalyzer]";

		command = "-index yes -source WT10G -dest Index_TREC -analyzer StandardAnalyzer";
		String[] commandSplit = command.split(" ");
		if (commandSplit.length > 0){
			System.out.println(command);
		}else{
			System.err.println(usage);
			return;
		}

		String indexOrNot = "";
		String analyzer = "";
		String sourceDir = "";
		String destDir = "";

		for (int i=0; i<commandSplit.length; i++){
			if(commandSplit[i].equalsIgnoreCase("-index")){
				indexOrNot = commandSplit[++i];
			}else if (commandSplit[i].equalsIgnoreCase("-dest")){
				destDir = commandSplit[++i];
			}
			else if(commandSplit[i].equalsIgnoreCase("-source")){
				sourceDir = commandSplit[++i];
			}else if (commandSplit[i].equalsIgnoreCase("-analyzer")){
				analyzer = commandSplit[++i];
			}else {
				System.err.println("Command invalid!, please see format: " + usage);
				return;
			}
		}

		indexTREC(sourceDir, destDir, analyzer);*/
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private static ScoreDoc[] searchTREC(Query query, String field, String index, String analyzerChoice){
		try{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = null;

			if (analyzerChoice.equalsIgnoreCase("StandardAnalyzer")){
				analyzer = new StandardAnalyzer(Version.LUCENE_48, IndexerTREC.stopWordsSet);
			}
			QueryParser parser = new QueryParser(Version.LUCENE_48, field, analyzer);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
			System.out.println("Enter query: ");

			String line = in.readLine();

			line = line.trim();

			System.out.println("Searching for: " + query.toString(field));

			int n = 100;
			TopDocs results = searcher.search(query, n);
			ScoreDoc[] hits = results.scoreDocs;

			int numTotalHits = results.totalHits;
			System.out.println("Before Rocchio Query Expansion");
			System.out.println("-------------------------------------------------");
			System.out.println(numTotalHits + " total matching documents");
			int start = 0;
			int end = Math.min(numTotalHits, n);


			for (int i = start; i < end; i++) {

				Document doc = searcher.doc(hits[i].doc);
				String docnum= doc.get("docnumber");
				if (docnum != null) {
					System.out.println((i+1) + ". " + docnum);
				} else {
					System.out.println((i+1) + ". " + "No path for this document");
				}
			}
			System.out.println("------------------------------");
			return hits;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private static void indexTREC(String sourceDir, String dest, String analyzerChoice){
		try{
			//String INDEXPATH = "Index_TREC";
			Boolean CREATE = true;
			File DATA_DIRECTORY = new File(sourceDir);
			Analyzer analyzer = null;

			Directory dir = FSDirectory.open(new File(dest));
			if (analyzerChoice.equalsIgnoreCase("standardanalyzer")){
				analyzer = new StandardAnalyzer(Version.LUCENE_48,
						stopWordsSet);
			}else {
				System.err.println("Analyzer format invalid: " + analyzerChoice);
				return;
			}

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48,
					analyzer);

			if (CREATE) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			IndexWriter writer = new IndexWriter(dir, iwc);

			System.out.println("Added/updated documents: "
					+ IndexerTREC.indexDocs(writer, DATA_DIRECTORY));

			writer.close();
		}catch (Exception e){
			e.printStackTrace();
			return;
		}

	}

	static String[] STOP_WORDS = new String[] { "0", "1", "2", "3", "4", "5",
		"6", "7", "8", "9", "000", "$", "about", "after", "all", "also",
		"an", "and", "another", "any", "are", "as", "at", "be", "because",
		"been", "before", "being", "between", "both", "but", "by", "came",
		"can", "come", "could", "did", "do", "does", "each", "else", "for",
		"from", "get", "got", "has", "had", "he", "have", "her", "here",
		"him", "himself", "his", "how", "if", "in", "into", "is", "it",
		"its", "just", "like", "make", "many", "me", "might", "more",
		"most", "much", "must", "my", "never", "now", "of", "on", "only",
		"or", "other", "our", "out", "over", "re", "said", "same", "see",
		"should", "since", "so", "some", "still", "such", "take", "than",
		"that", "the", "their", "them", "then", "there", "these", "they",
		"this", "those", "through", "to", "too", "under", "up", "use",
		"very", "want", "was", "way", "we", "well", "were", "what", "when",
		"where", "which", "while", "who", "will", "with", "would", "you",
		"your", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
		"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
		"z", "http", "href", "li" , "br" , "page" };
	static CharArraySet stopWordsSet = new CharArraySet(Version.LUCENE_48,
			Arrays.asList(STOP_WORDS), true);
}
