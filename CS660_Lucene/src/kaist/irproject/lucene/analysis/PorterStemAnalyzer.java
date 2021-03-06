package kaist.irproject.lucene.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

/**
 * PorterStemAnalyzer processes input
 * text by stemming English words to their roots.
 * This Analyzer also converts the input to lower case
 * and removes stop words.  A small set of default stop
 * words is defined in the STOP_WORDS
 * array, but a caller can specify an alternative set
 * of stop words by calling non-default constructor.
 */
public class PorterStemAnalyzer extends Analyzer {
    private CharArraySet stopSet;

    /**
     * An array containing some common English words
     * that are usually not useful for searching.
     */
    public static final String[] STOP_WORDS =
    {
        "0", "1", "2", "3", "4", "5", "6", "7", "8",
        "9", "000", "$",
        "about", "after", "all", "also", "an", "and",
        "another", "any", "are", "as", "at", "be",
        "because", "been", "before", "being", "between",
        "both", "but", "by", "came", "can", "come",
        "could", "did", "do", "does", "each", "else",
        "for", "from", "get", "got", "has", "had",
        "he", "have", "her", "here", "him", "himself",
        "his", "how","if", "in", "into", "is", "it",
        "its", "just", "like", "make", "many", "me",
        "might", "more", "most", "much", "must", "my",
        "never", "now", "of", "on", "only", "or",
        "other", "our", "out", "over", "re", "said",
        "same", "see", "should", "since", "so", "some",
        "still", "such", "take", "than", "that", "the",
        "their", "them", "then", "there", "these",
        "they", "this", "those", "through", "to", "too",
        "under", "up", "use", "very", "want", "was",
        "way", "we", "well", "were", "what", "when",
        "where", "which", "while", "who", "will",
        "with", "would", "you", "your",
        "a", "b", "c", "d", "e", "f", "g", "h", "i",
        "j", "k", "l", "m", "n", "o", "p", "q", "r",
        "s", "t", "u", "v", "w", "x", "y", "z"
    };

    /**
     * Builds an analyzer.
     */
    public PorterStemAnalyzer() {
        this(STOP_WORDS);
    }

    /**
     * Builds an analyzer with the given stop words.
     *
     * @param stopWords a String array of stop words
     */
    public PorterStemAnalyzer(String[] stopWords) {
    	stopSet = StopFilter.makeStopSet(Version.LUCENE_48,stopWords);
    }
    
    /**
     * Builds an analyzer with the stop words from the given file.
     * 
     * @see WordlistLoader#getWordSet(File)
     */
    public PorterStemAnalyzer(CharArraySet stopwords, Reader reader) throws IOException {
    	stopSet = WordlistLoader.getWordSet(reader, stopwords);
    }

    /** Constructs a {@link StandardTokenizer} filtered by a {@link
    	StandardFilter}, a {@link LowerCaseFilter}, a {@link StopFilter} 
    	and a {@link PorterStemFilter}. */
	public TokenStream tokenStream(String fieldName, Reader reader, FieldType type) {
	    StandardTokenizer tokenStream = new StandardTokenizer(Version.LUCENE_48,reader);
	    tokenStream.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
	    TokenStream result = new StandardFilter(Version.LUCENE_48,tokenStream);
	    result = new LowerCaseFilter(Version.LUCENE_48, result);
	    result = new StopFilter(Version.LUCENE_48, result, stopSet);
	    result = new PorterStemFilter(result);
	    return result;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer source = new LowerCaseTokenizer(Version.LUCENE_48, reader);
		return new TokenStreamComponents(source ,new PorterStemFilter(source));
	}
}