package org.apache.lucene.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

/**
 * @author Anders
 * 
 */
public class IndexerTREC {
	public static void main(String args[]) throws IOException {
		String INDEXPATH = "Index_TREC";
		Boolean CREATE = true;
		File DATA_DIRECTORY = new File("wt10g");

		Directory dir = FSDirectory.open(new File(INDEXPATH));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48,
				stopWordsSet);
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
				+ indexDocs(writer, DATA_DIRECTORY));

		writer.close();

	}

	private static int indexDocs(IndexWriter writer, File file)
			throws IOException {
		int docCount = 0;
		if (file.getName().equals("info")) {
			return 0;
		}
		System.out.println(file.getPath());

		if (file.isDirectory()) {
			for (String path : file.list()) {
				docCount += indexDocs(writer, new File(file, path));
			}
		} else if (file.getName().endsWith(".gz")){
			docCount += indexDoc(writer, file);
		} else { 
			return 0;
		}

		return docCount;
	}

	private static int indexDoc(IndexWriter writer, File docfile)
			throws IOException {
		InputStream fileStream = new FileInputStream(docfile);
		InputStream gzipStream = new GZIPInputStream(fileStream);
		Reader decoder = new InputStreamReader(gzipStream,
				StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(decoder);
		String docFileName = "docs_txt.txt";
		File file = new File(docFileName);
		FileWriter filerWriterDocs = new FileWriter(file.getAbsoluteFile(),
				true);
		BufferedWriter bufferedWriterDocs = new BufferedWriter(filerWriterDocs);
		PrintWriter out = new PrintWriter(bufferedWriterDocs);

		String line;
		int docCount = 1;
		int intinvalidDocCount = 0;
		while ((line = in.readLine()) != null) {
			if (line.equals("<DOC>")) {
				Document doc = new Document();
				line = in.readLine();

				String docnum = line.substring(7, line.indexOf("</"));
				doc.add(new StringField("docnumber", docnum, Field.Store.YES));

				String content = "";
				while (!(line = in.readLine()).equals("</DOC>")) {
					content += line + " ";
				}

				content = content.substring(content.indexOf("</DOCHDR>"));
				content = Jsoup.parse(content).text();

				doc.add(new TextField("contents", content, Field.Store.YES)); // Content
																				// of
																				// the
																				// doc
				doc.add(new TextField("indexnumber",
						Integer.toString(docCount), Field.Store.YES)); // The
																		// number
																		// which
																		// corresponds
																		// to
																		// the
																		// LDA
																		// id

				if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
					System.out.println("adding " + docnum);
					// System.out.println(doc);
					writer.addDocument(doc);
					TokenStream tokenStream = writer.getAnalyzer().tokenStream(
							"contents", doc.get("contents"));
					CharTermAttribute charTermAttribute = tokenStream
							.addAttribute(CharTermAttribute.class);

					tokenStream.reset();
					if (docCount < 5000) { // Only write first 5000 docs
						int c = 0; // Count number of terms in a document. Used
									// for debugging purposes
						String docContent = "";
						while (tokenStream.incrementToken()) {
							String term = charTermAttribute.toString();
							docContent = docContent + " " + term;

							c++;
						}
						if (docContent.equals("")) {
							docContent = "invalid";
							intinvalidDocCount++;
						}
						bufferedWriterDocs.append(docContent.substring(1)
								+ "\n");
					}
					tokenStream.end();
					tokenStream.close();
					docCount++; // increases index number

				} else {
					System.out.println("updating " + docnum);
					writer.updateDocument(new Term("docnumber", docnum), doc);
				}

			}

		}
		bufferedWriterDocs.close();
		RandomAccessFile f = new RandomAccessFile(new File(docFileName), "rw");
		f.seek(0); // to the beginning
		String documentCount = Integer.toString(docCount - 1) + "\n";
		f.write(documentCount.getBytes());
		f.close();
		in.close();
		return docCount;
	}

	// Better stop words then the default. Used for the analyser.
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
