/**
   Copyright (c) 2007, University of Pittsburgh

   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:

 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided
   with the distribution.

 * Neither the name of the University of Pittsburgh nor the names
   of its contributors may be used to endorse or promote products
   derived from this software without specific prior written
   permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package pitt.search.semanticvectors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.BaseCompositeReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import pitt.search.semanticvectors.utils.VerbatimLogger;

import com.ctvit.utils.Constant;
import com.google.word2vec.VectorModel;
import com.google.word2vec.util.Counter;


/**
 * Class to support reading extra information from Lucene indexes, including
 * term frequency, doc frequency.
 */
public class LuceneUtils {
	public static final Version LUCENE_VERSION = Version.LUCENE_4_10_2;

	private static final Logger logger = Logger.getLogger(LuceneUtils.class
			.getCanonicalName());
	private FlagConfig flagConfig;
	private BaseCompositeReader<AtomicReader> compositeReader;
	private AtomicReader atomicReader;
	private Directory directory;
	private Hashtable<Term, Float> termEntropy = new Hashtable<Term, Float>();
	private Hashtable<Term, Float> termIDF = new Hashtable<Term, Float>();
	private TreeSet<String> stopwords = null;
	private TreeSet<String> startwords = null;
	private IndexSearcher searcher;
	
	private VectorModel vm;


	// public static int count=0;
	// private Set<String> words=new HashSet<String>();
	/**
	 * Determines which term-weighting strategy to use in indexing, and in
	 * search if {@link FlagConfig#usetermweightsinsearch()} is set.
	 * 
	 * <p>
	 * Names may be passed as command-line arguments, so underscores are
	 * avoided.
	 */
	public enum TermWeight {
		/** No term weighting: all terms have weight 1. */
		NONE,
		/** Use inverse document frequency: see {@link LuceneUtils#getIDF}. */
		IDF,
		/** Use log entropy: see {@link LuceneUtils#getEntropy}. */
		LOGENTROPY,
	}

	/**
	 * @param flagConfig
	 *            Contains all information necessary for configuring
	 *            LuceneUtils. {@link FlagConfig#luceneindexpath()} must be
	 *            non-empty.
	 */
	public LuceneUtils(FlagConfig flagConfig) throws IOException {
		if (flagConfig.luceneindexpath().isEmpty()) {
			throw new IllegalArgumentException(
					"-luceneindexpath is a required argument for initializing LuceneUtils instance.");
		}
		directory = FSDirectory.open(new File(flagConfig
				.luceneindexpath()));
		this.compositeReader = DirectoryReader.open(directory);
		this.atomicReader = SlowCompositeReaderWrapper.wrap(compositeReader);
		// this.multiReader = new MultiReader(this.atomicReader);
		MultiFields.getFields(compositeReader);
		this.flagConfig = flagConfig;
		if (!flagConfig.stoplistfile().isEmpty())
			loadStopWords(flagConfig.stoplistfile());
	}

	public void close(){
		try {
			this.atomicReader.close();
			this.compositeReader.close();
			this.directory.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Loads the stopword file into the {@link #stopwords} data structure.
	 * 
	 * @param stoppath
	 *            Path to stopword file.
	 * @throws IOException
	 *             If stopword file cannot be read.
	 */
	public void loadStopWords(String stoppath) throws IOException {
		logger.info("Using stopword file: " + stoppath);
		stopwords = new TreeSet<String>();
		try {
			BufferedReader readIn = new BufferedReader(new FileReader(stoppath));
			String in = readIn.readLine();
			while (in != null) {
				stopwords.add(in);
				in = readIn.readLine();
			}
			readIn.close();
		} catch (IOException e) {
			throw new IOException("Couldn't open file " + stoppath);
		}
	}

	/**
	 * Loads the startword file into the {@link #startwords} data structure.
	 * 
	 * @param startpath
	 *            Path to startword file
	 * @throws IOException
	 *             If startword file cannot be read.
	 */
	public void loadStartWords(String startpath) throws IOException {
		System.err.println("Using startword file: " + startpath);
		startwords = new TreeSet<String>();
		try {
			BufferedReader readIn = new BufferedReader(
					new FileReader(startpath));
			String in = readIn.readLine();
			while (in != null) {
				startwords.add(in);
				in = readIn.readLine();
			}
			readIn.close();
		} catch (IOException e) {
			throw new IOException("Couldn't open file " + startpath);
		}
	}

	/**
	 * Returns true if term is in stoplist, false otherwise.
	 */
	public boolean stoplistContains(String x) {
		if (stopwords == null)
			return false;
		return stopwords.contains(x);
	}

	public Document getDoc(int docID) throws IOException {
		return this.atomicReader.document(docID);
	}

	public Terms getTermsForField(String field) throws IOException {
		return atomicReader.terms(field);
	}

	// added by yl
	public int readTokens(Counter<String> counter) {
		int count = 0;
		TermsEnum termsEnum = null;
		for (String field : Constant.CONTENTSFIELDS.split(",")) {
			try {
				termsEnum = this.getTermsForField(field).iterator(termsEnum);
				BytesRef ref;
				while ((ref = termsEnum.next()) != null) {
					String word = ref.utf8ToString();
					Term term = new Term(field, word);
					int freq = this.getGlobalTermFreq(term);
					counter.add(word, freq);
					count += freq;
					// System.out.println("term:"+term+",freq:"+freq);
					if ((count % 10000 == 0)
							|| (count < 10000 && count % 1000 == 0)) {
						VerbatimLogger.info("Processed " + count
								+ " terms ... \n");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("count:" + count);
		return count;
	}

	public DocsEnum getDocsForTerm(Term term) throws IOException {
		return this.atomicReader.termDocsEnum(term);
	}

	// added by yl
	public List<Integer> getAllDocs() throws IOException {
		if (this.searcher == null)
			this.searcher = new IndexSearcher(this.atomicReader);
		TopDocs hits = null;
		Query q = new MatchAllDocsQuery();
		// test id, bounded on both ends 201405230623
		hits = this.searcher.search(q, this.getNumDocs());
		List<Integer> docs = new ArrayList<Integer>();
		for (int i = 0; i < hits.totalHits; i++) {
			int docID = hits.scoreDocs[i].doc;
			docs.add(docID);
		}
		return docs;
	}

	public int getTermFreq(int docId, Term term) throws IOException {
		int freq = 0;
		/*
		 * DocsEnum docsEnum = this.atomicReader.termDocsEnum(term); while
		 * (docsEnum.nextDoc() != DocsEnum.NO_MORE_DOCS) { //
		 * System.out.println(docsEnum.docID()); if (docId == docsEnum.docID())
		 * { freq = docsEnum.freq(); break; } }
		 */
		Terms terms = this.atomicReader.getTermVector(docId, term.field());
		TermsEnum itr = terms.iterator(null);
		BytesRef t = null;
		while ((t = itr.next()) != null) {
			String termText = t.utf8ToString();
			if (termText.equals(term.text()))
				freq = (int) itr.totalTermFreq();
		}
		return freq;
	}

	public Terms getTermVector(int docID, String field) throws IOException {
		return this.atomicReader.getTermVector(docID, field);
	}

	public FieldInfos getFieldInfos() {
		return this.atomicReader.getFieldInfos();
	}

	/**
	 * Gets the global term frequency of a term, i.e. how may times it occurs in
	 * the whole corpus
	 * 
	 * @param term
	 *            whose frequency you want
	 * @return Global term frequency of term, or 1 if unavailable.
	 */
	public int getGlobalTermFreq(Term term) {
		int tf = 0;

		try {
			tf = (int) compositeReader.totalTermFreq(term);
		} catch (IOException e) {
			logger.info("Couldn't get term frequency for term " + term.text());
			return 1;
		}
		if (tf == -1) {
			logger.warning("Lucene StandardDirectoryReader returned -1 for term: '"
					+ term.text()
					+ "' in field: '"
					+ term.field()
					+ "'. Changing to 0."
					+ "\nThis may be due to a version-mismatch and might be solved by rebuilding your Lucene index.");
			tf = 0;
		}
		return tf;
	}

	/**
	 * Gets a term weight for a string, adding frequency over occurrences in all
	 * contents fields.
	 */
	public float getGlobalTermWeightFromString(String termString) {
		float freq = 0;
		for (String field : flagConfig.contentsfields())
			freq += getGlobalTermWeight(new Term(field, termString));
		return freq;
	}

	/**
	 * Gets a global term weight for a term, depending on the setting for
	 * {@link FlagConfig#termweight()}.
	 * 
	 * Used in indexing. Used in query weighting if
	 * {@link FlagConfig#usetermweightsinsearch} is true.
	 * 
	 * @param term
	 *            whose frequency you want
	 * @return Global term weight, or 1 if unavailable.
	 */
	public float getGlobalTermWeight(Term term) {
		switch (flagConfig.termweight()) {
		case NONE:
			return 1;
		case IDF:
			return getIDF(term);
		case LOGENTROPY:
			return getEntropy(term);
		}
		VerbatimLogger.severe("Unrecognized termweight option: "
				+ flagConfig.termweight() + ". Returning 1.");
		return 1;
	}

	/**
	 * Gets a local term weight for a term based on its document frequency,
	 * depending on the setting for {@link FlagConfig#termweight()}.
	 * 
	 * Used in indexing.
	 * 
	 * @param docfreq
	 *            the frequency of the term concerned in the document of
	 *            interest
	 * @return Local term weight
	 */
	public float getLocalTermWeight(int docfreq) {
		switch (flagConfig.termweight()) {
		case NONE:
			return 1;
		case IDF:
			return docfreq;
		case LOGENTROPY:
			return (float) Math.log10(1 + docfreq);
		}
		VerbatimLogger.severe("Unrecognized termweight option: "
				+ flagConfig.termweight() + ". Returning 1.");
		return 1;
	}

	/**
	 * Returns the number of documents in the Lucene index.
	 */
	public int getNumDocs() {
		return compositeReader.numDocs();
	}

	/**
	 * added by yangliang
	 * 
	 * @return
	 */
	public int getNumTerms() {
		TermsEnum termsEnum = null;
		int tc = 0;
		for (String fieldName : flagConfig.contentsfields()) {
			TermsEnum terms;
			try {
				System.out.println(fieldName);
				terms = getTermsForField(fieldName).iterator(termsEnum);
				while (terms.next() != null) {
					tc++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tc;
	}

	// added by yl
	public int getNumWords() {
		TermsEnum termsEnum = null;
		Set<String> words = new HashSet<String>();
		for (String fieldName : flagConfig.contentsfields()) {
			TermsEnum terms;
			try {
				terms = getTermsForField(fieldName).iterator(termsEnum);
				BytesRef word;
				while ((word = terms.next()) != null) {
					words.add(word.utf8ToString());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return words.size();
	}


	// added by yl
	public Map<String, Integer> getWordCounter(List<Integer> docsId,
			VectorModel vm) {
		Map<String, Integer> counter = new HashMap<String, Integer>();
		for (int docId : docsId) {
			for (String f : Constant.CONTENTSFIELDS.split(",")) {
				Terms terms;
				try {
					terms = getTermVector(docId, f);
					if (terms == null)
						continue;
					TermsEnum enums = terms.iterator(null);
					BytesRef ref;
					while ((ref = enums.next()) != null) {
						String text = ref.utf8ToString();
						if (!vm.containsWord(text))
							continue;
						Term term = new Term(f, text);
						// this.getTermFreq(docId, term);
						int freq = (int) enums.totalTermFreq();
						if (counter.containsKey(text)) {
							counter.put(text, counter.get(text) + freq);
						} else {
							counter.put(text, freq);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return counter;
	}

	

	private double getIDF(int numDocs, int docFreq) {
		double idf = 0;
		if (docFreq == 0) {
			idf = 0;
		} else {
			idf = (float) Math.log10(numDocs / docFreq);
		}
		return idf;
	}


	public List<Integer> getDocsByIdentifiedField(String field,
			List<String> keys) {
		List<Integer> docsId = new ArrayList<Integer>();
		for (String key : keys) {
			Term term = new Term(field, key);
			try {
				DocsEnum docsEnum = this.getDocsForTerm(term);
				int docId = 0;
				while ((docId = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
					docsId.add(docId);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return docsId;
	}

	// added by yl
	public Set<String> getWordsByIdentifiedField(String field, Set<String> keys) {
		Set<String> words = new HashSet<String>();
		Set<Integer> docsId = new HashSet<Integer>();
		for (String key : keys) {
			Term term = new Term(field, key);
			try {
				DocsEnum docsEnum = this.getDocsForTerm(term);
				int docId = 0;
				while ((docId = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
					docsId.add(docId);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int docId : docsId) {
			for (String f : Constant.CONTENTSFIELDS.split(",")) {
				Terms terms;
				try {
					terms = getTermVector(docId, f);
					if (terms == null)
						continue;
					TermsEnum enums = terms.iterator(null);
					BytesRef ref;
					while ((ref = enums.next()) != null) {
						String text = ref.utf8ToString();
						words.add(text);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return words;
	}

	// added by yl
	public String getTokenStream(int docId) {
		StringBuffer sb = new StringBuffer();
		for (String field : Constant.CONTENTSFIELDS.split(",")) {
			Map<Integer, String> fieldContent = this
					.getTokenStreamIdentifiedByPosition(docId, field);
			StringBuffer fieldSb = new StringBuffer();
			for (int pos : fieldContent.keySet()) {
				fieldSb.append(fieldContent.get(pos));
				fieldSb.append(Constant.WORD_SPLIT);//\t
			}
			sb.append(fieldSb);
		}
		return sb.toString();
	}

	// added by yl
	public Map<Integer, String> getTokenStreamIdentifiedByPosition(int docId,
			String field) {
		Map<Integer, String> map = new TreeMap<Integer, String>();
		try {
			// System.out.println("docId:"+docId+",field:"+field);
			Terms terms = getTermVector(docId, field);
			/*
			 * System.out.println("hasFreqs:" + terms.hasFreqs());
			 * System.out.println("hasOffsets:" + terms.hasOffsets());
			 * System.out.println("hasPayloads:" + terms.hasPayloads());
			 * System.out.println("hasPositions:" + terms.hasPositions());
			 */
			// System.out.println(terms);
			if (terms == null)
				return map;
			TermsEnum enums = terms.iterator(null);
			BytesRef ref;
			while ((ref = enums.next()) != null) {
				String text = ref.utf8ToString();
				DocsAndPositionsEnum docPosEnum = enums.docsAndPositions(null,
						null, DocsAndPositionsEnum.FLAG_NONE);
				docPosEnum.nextDoc();
				// Retrieve the term frequency in the current document
				int freq = docPosEnum.freq();
				// System.out.println(text + ",freq:" + freq);
				// words.add(text);
				for (int i = 0; i < freq; i++) {
					int position = docPosEnum.nextPosition();
					// int start=docPosEnum.startOffset();
					// int end=docPosEnum.endOffset();
					// System.out.println(position);
					map.put(position, text);
					// Store start, end and position in a list
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * for (int pos : map.keySet()) { System.out.println("pos:" + pos + ","
		 * + map.get(pos)); }
		 */
		// System.out.println(map.size()+","+words.size());
		// count+=map.size();
		return map;
	}

	/**
	 * Gets the IDF (i.e. log10(numdocs/doc frequency)) of a term
	 * 
	 * @param term
	 *            the term whose IDF you would like
	 */
	private float getIDF(Term term) {
		if (termIDF.containsKey(term)) {
			return termIDF.get(term);
		} else {
			try {
				int freq = compositeReader.docFreq(term);
				if (freq == 0) {
					return 0;
				}
				float idf = (float) Math
						.log10(compositeReader.numDocs() / freq);
				termIDF.put(term, idf);
				return idf;
			} catch (IOException e) {
				// Catches IOException from looking up doc frequency, never seen
				// yet in practice.
				e.printStackTrace();
				return 1;
			}
		}
	}

	/**
	 * Gets the 1 - entropy (i.e. 1+ plogp) of a term, a function that favors
	 * terms that are focally distributed We use the definition of log-entropy
	 * weighting provided in Martin and Berry (2007): Entropy = 1 + sum ((Pij
	 * log2(Pij)) / log2(n)) where Pij = frequency of term i in doc j / global
	 * frequency of term i n = number of documents in collection
	 * 
	 * @param term
	 *            whose entropy you want Thanks to Vidya Vasuki for adding the
	 *            hash table to eliminate redundant calculation
	 */
	private float getEntropy(Term term) {
		if (termEntropy.containsKey(term))
			return termEntropy.get(term);
		int gf = getGlobalTermFreq(term);
		double entropy = 0;
		try {
			DocsEnum docsEnum = this.getDocsForTerm(term);
			while ((docsEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
				double p = docsEnum.freq(); // frequency in this document
				p = p / gf; // frequency across all documents
				entropy += p * (Math.log(p) / Math.log(2)); // sum of Plog(P)
			}
			int n = this.getNumDocs();
			double log2n = Math.log(n) / Math.log(2);
			entropy = entropy / log2n;
		} catch (IOException e) {
			logger.info("Couldn't get term entropy for term " + term.text());
		}
		termEntropy.put(term, 1 + (float) entropy);
		return (float) (1 + entropy);
	}

	/**
	 * Public version of {@link #termFilter} that gets all its inputs from the
	 * {@link #flagConfig} and the provided term.
	 * 
	 * External callers should normally use this method, so that new filters are
	 * available through different codepaths provided they pass a
	 * {@code FlagConfig}.
	 * 
	 * @param term
	 *            Term to be filtered in or out, depending on Lucene index and
	 *            flag configs.
	 */
	public boolean termFilter(Term term) {
		return termFilter(term, flagConfig.contentsfields(),
				flagConfig.minfrequency(), flagConfig.maxfrequency(),
				flagConfig.maxnonalphabetchars(), flagConfig.filteroutnumbers());
	}

	/**
	 * Filters out non-alphabetic terms and those of low frequency.
	 * 
	 * Thanks to Vidya Vasuki for refactoring and bug repair
	 * 
	 * @param term
	 *            Term to be filtered.
	 * @param desiredFields
	 *            Terms in only these fields are filtered in
	 * @param minFreq
	 *            minimum term frequency accepted
	 * @param maxFreq
	 *            maximum term frequency accepted
	 * @param maxNonAlphabet
	 *            reject terms with more than this number of non-alphabetic
	 *            characters
	 */
	protected boolean termFilter(Term term, String[] desiredFields,
			int minFreq, int maxFreq, int maxNonAlphabet) {
		// Field filter.
		boolean isDesiredField = false;
		for (int i = 0; i < desiredFields.length; ++i) {
			if (term.field().compareToIgnoreCase(desiredFields[i]) == 0) {
				isDesiredField = true;
			}
		}

		// Stoplist (if active)
		if (stoplistContains(term.text()))
			return false;

		if (!isDesiredField) {
			return false;
		}

		// Character filter.
		if (maxNonAlphabet != -1) {
			int nonLetter = 0;
			String termText = term.text();
			for (int i = 0; i < termText.length(); ++i) {
				if (!Character.isLetter(termText.charAt(i)))
					nonLetter++;
				if (nonLetter > maxNonAlphabet)
					return false;
			}
		}

		// Frequency filter.
		int termfreq = getGlobalTermFreq(term);
		if (termfreq < minFreq | termfreq > maxFreq) {
			return false;
		}

		// If we've passed each filter, return true.
		return true;
	}

	/**
	 * Applies termFilter and additionally (if requested) filters out digit-only
	 * words.
	 * 
	 * @param term
	 *            Term to be filtered.
	 * @param desiredFields
	 *            Terms in only these fields are filtered in
	 * @param minFreq
	 *            minimum term frequency accepted
	 * @param maxFreq
	 *            maximum term frequency accepted
	 * @param maxNonAlphabet
	 *            reject terms with more than this number of non-alphabetic
	 *            characters
	 * @param filterNumbers
	 *            if true, filters out tokens that represent a number
	 */
	private boolean termFilter(Term term, String[] desiredFields, int minFreq,
			int maxFreq, int maxNonAlphabet, boolean filterNumbers) {
		// number filter
		if (filterNumbers) {
			try {
				// if the token can be parsed as a floating point number, no
				// exception is thrown and false is returned
				// if not, an exception is thrown and we continue with the other
				// termFilter method.
				// remark: this does not filter out e.g. Java or C++ formatted
				// numbers like "1f" or "1.0d"
				Double.parseDouble(term.text());
				return false;
			} catch (Exception e) {
			}
		}
		return termFilter(term, desiredFields, minFreq, maxFreq, maxNonAlphabet);
	}

	/**
	 * TODO(dwiddows): Check that no longer needed in Lucene 4.x and above.
	 * 
	 * Static method for compressing an index.
	 * 
	 * This small preprocessing step makes sure that the Lucene index is
	 * optimized to use contiguous integers as identifiers. Otherwise exceptions
	 * can occur if document id's are greater than indexReader.numDocs().
	 */

	static void compressIndex(String indexDir) {
		return;
	}

	/*
	 * try { IndexWriter compressor = new IndexWriter(FSDirectory.open(new
	 * File(indexDir)), new StandardAnalyzer(Version.LUCENE_30), false,
	 * MaxFieldLength.UNLIMITED); compressor.optimize(); compressor.close(); }
	 * catch (CorruptIndexException e) { e.printStackTrace(); } catch
	 * (IOException e) { e.printStackTrace(); } }
	 */

	public IndexSearcher getSearcher() {
		return new IndexSearcher(this.atomicReader);
	}

}
