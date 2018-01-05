/**
   Copyright (c) 2008, Google Inc.

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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;

import com.ctvit.nlp.semantic.search.restrict.DocInfo;

import pitt.search.semanticvectors.vectors.IncompatibleVectorsException;
import pitt.search.semanticvectors.vectors.RealVector;
import pitt.search.semanticvectors.vectors.Vector;
import pitt.search.semanticvectors.vectors.VectorFactory;
import pitt.search.semanticvectors.vectors.VectorType;

/**
 * This class provides methods for reading a VectorStore into memory as an
 * optimization if batching many searches.
 * <p>
 * 
 * The serialization currently presumes that the object (in the ObjectVectors)
 * should be serialized as a String.
 * <p>
 * 
 * The class is constructed by creating a VectorStoreReader class, iterating
 * through vectors and reading them into memory.
 * 
 * @see VectorStoreReaderLucene
 * @see ObjectVector
 **/
public class VectorStoreRAM implements VectorStore {
	private static final Logger logger = Logger.getLogger(VectorStoreRAM.class
			.getCanonicalName());
	private FlagConfig flagConfig;
	private Hashtable<Object, ObjectVector> objectVectors;//0
	private Hashtable<Object, ObjectVector> objectVectors1;
	private Hashtable<Object, ObjectVector> objectVectors2;
	private Hashtable<Object, ObjectVector> objectVectors3;
	private Map<String, DocInfo> docInfos;
	private VectorType vectorType;
	private int dimension;
	/** Used for checking compatibility of new vectors. */
	private Vector zeroVector;
	// for restict docinfo
	private LuceneUtils luceneUtils = null;
	private Map<Integer, Hashtable<Object, ObjectVector>> vectorsMap;

	public VectorStoreRAM(FlagConfig flagConfig) {
		this.objectVectors = new Hashtable<Object, ObjectVector>();
		this.flagConfig = flagConfig;
		this.vectorType = flagConfig.vectortype();
		this.dimension = flagConfig.dimension();
		zeroVector = VectorFactory.createZeroVector(vectorType, dimension);
		//
		if (flagConfig.loadDocInfo()) {
			this.docInfos = new HashMap<String, DocInfo>();
			try {
				this.luceneUtils = new LuceneUtils(this.flagConfig);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// added by yl
	public Map<String, float[]> getVectorMap() {
		Map<String, float[]> vectorMap = new HashMap<String, float[]>();
		Enumeration<ObjectVector> vecEnum = getAllVectors();
		// int i = 0;
		while (vecEnum.hasMoreElements()) {
			try {
				ObjectVector o = vecEnum.nextElement();
				/*
				 * System.out.println(o.getObject().toString());
				 * System.out.println(o.getVector().toString()); if ((i % 100 ==
				 * 0) || (i < 100 && i % 10 == 0)) {
				 * VerbatimLogger.info("computed " + i + "...\n"); }
				 */
				vectorMap.put(o.getObject().toString(),
						((RealVector) o.getVector()).getCoordinates());
				// i++;
				// VerbatimLogger.info("computed "+ i++ +"...\n");
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}
		}
		// System.out.println(i);
		return vectorMap;
	}

	/**
	 * Returns a new vector store, initialized from disk with the given
	 * vectorFile.
	 * 
	 * Dimension and vector type from store on disk may overwrite any previous
	 * values in flagConfig.
	 **/
	public static VectorStoreRAM readFromFile(FlagConfig flagConfig,
			String vectorFile) throws IOException {
		VectorStoreRAM store = new VectorStoreRAM(flagConfig);
		store.initFromFile(vectorFile);
		return store;
	}

	/** Initializes a vector store from disk. */
	public void initFromFile(String vectorFile) throws IOException {
		CloseableVectorStore vectorReaderDisk = VectorStoreReader
				.openVectorStore(vectorFile, flagConfig);
		Enumeration<ObjectVector> vectorEnumeration = vectorReaderDisk
				.getAllVectors();
		//

		logger.fine("Reading vectors from store on disk into memory cache  ...");
		if (flagConfig.loadDocInfo()) {
			this.objectVectors1 = new Hashtable<Object, ObjectVector>();
			this.objectVectors2 = new Hashtable<Object, ObjectVector>();
			this.objectVectors3 = new Hashtable<Object, ObjectVector>();
			this.vectorsMap=new HashMap<Integer, Hashtable<Object, ObjectVector>>();
			while (vectorEnumeration.hasMoreElements()) {
				ObjectVector objectVector = vectorEnumeration.nextElement();
				String key = objectVector.getObject().toString();
				try {
					DocInfo docInfo = this.getDocInfoFromLucene(key);
					int status=docInfo.getStatus();
					this.docInfos.put(key, docInfo);
					if(status==0){
						this.objectVectors.put(key, objectVector);
					}else if(status==1){
						this.objectVectors1.put(key, objectVector);
					}else if(status==2){
						this.objectVectors2.put(key, objectVector);
					}else if(status==3){
						this.objectVectors3.put(key, objectVector);
					}
				} catch (Exception e) {
					//e.printStackTrace();
					//System.out.println("initFromFile exception");
				}
			}
			//
			this.luceneUtils.close();
			//
			this.vectorsMap.put(0, this.objectVectors);
			this.vectorsMap.put(1, this.objectVectors1);
			this.vectorsMap.put(2, this.objectVectors2);
			this.vectorsMap.put(3, this.objectVectors3);
			
		} else {
			while (vectorEnumeration.hasMoreElements()) {
				ObjectVector objectVector = vectorEnumeration.nextElement();
				String key = objectVector.getObject().toString();
				this.objectVectors.put(key, objectVector);
			}
		}

		vectorReaderDisk.close();
		logger.log(Level.FINE, "Cached {0} vectors.", objectVectors.size());
	}

	private DocInfo getDocInfoFromLucene(String pn) {
		DocInfo docInfo = new DocInfo();
		int docId = this.getDocIdByPn(pn);
		try {
			//System.out.println("############:"+flagConfig.luceneindexpath()+","+pn+","+docId);
			Document document = this.luceneUtils.getDoc(docId);
			String ad = document.get("ad");
			ad = ad != null ? ad : "0";
			String pd = document.get("pd");
			pd = pd != null ? pd : "0";
			//
			String prd = document.get("prd");
			prd = prd != null ? prd : "0";
			//
			String status = document.get("status");
			status = status != null ? status : "0";
			String ipc = document.get("ipc");
			ipc = ipc != null ? ipc : "0";
			String[] arr = ipc.split(";");
			int[] arrIpc = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				arrIpc[i] = Integer.parseInt(arr[i]);
			}
			docInfo.setAd(Integer.parseInt(ad));
			docInfo.setPd(Integer.parseInt(pd));
			docInfo.setPrd(Integer.parseInt(prd));
			docInfo.setStatus(Integer.parseInt(status));
			docInfo.setIpc(arrIpc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("getDocInfo exception");
		}
		return docInfo;
	}

	public int getDocIdByPn(String pn) {	
	   Term term = new Term("pn", pn);
		int docId = 0;
		try {
			DocsEnum docsEnum = this.luceneUtils.getDocsForTerm(term);
			int tmp=0;
			while ((tmp = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
				docId=tmp;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("############:"+flagConfig.luceneindexpath()+","+pn+","+docId);
			//System.out.println("getDocIdByPn exception");
		}
		//System.out.println(docId);
		return docId;
	}

	/**
	 * Adds a single vector with the given key and value. Overwrites any
	 * existing vector with this key.
	 */
	public void putVector(Object key, Vector vector) {
		IncompatibleVectorsException.checkVectorsCompatible(zeroVector, vector);
		ObjectVector objectVector = new ObjectVector(key, vector);
		this.objectVectors.put(key, objectVector);
	}


	public Enumeration<ObjectVector> getAllVectors() {
		return this.objectVectors.elements();
	}

	public Enumeration<ObjectVector> getAllVectors(List<String> pns) {
		Hashtable<Object, ObjectVector> ovs = new Hashtable<Object, ObjectVector>();
		for (String pn : pns) {
			ovs.put(pn, this.objectVectors.get(pn));
		}
		return ovs.elements();
	}


	public int getNumVectors() {
		return this.objectVectors.size();
	}

	/**
	 * Given an object, get its corresponding vector.
	 * 
	 * <p>
	 * This implementation only works for string objects so far.
	 * 
	 * @param desiredObject
	 *            - the string you're searching for
	 * @return vector from the VectorStore, or null if not found.
	 */
	public Vector getVector(Object desiredObject) {
		ObjectVector objectVector = this.objectVectors.get(desiredObject);
		if (objectVector != null) {
			return objectVector.getVector();
		} else {
			return null;
		}
	}

	/**
	 * Given an object, return its corresponding vector and remove it from the
	 * VectorStore. Does nothing and returns null if the object was not found.
	 * <p>
	 * This implementation only works for string objects so far.
	 * 
	 * @param desiredObject
	 *            - the string you're searching for
	 * @return vector from the VectorStore, or null if not found.
	 */
	public Vector removeVector(Object desiredObject) {
		ObjectVector objectVector = this.objectVectors.get(desiredObject);
		if (objectVector != null) {
			return objectVectors.remove(desiredObject).getVector();
		} else {
			return null;
		}
	}


	public boolean containsVector(Object object) {
		return objectVectors.containsKey(object);
	}

	// added by yl
	public void superpose(VectorStoreRAM vectors) throws Exception {
		if (this.dimension != vectors.dimension)
			throw new Exception("dimensions not equals");
		if (this.getNumVectors() != vectors.getNumVectors())
			throw new Exception("number of vectors not equals");
		Enumeration<ObjectVector> enums = this.getAllVectors();
		while (enums.hasMoreElements()) {
			ObjectVector vectorObject = enums.nextElement();
			Object key = vectorObject.getObject();
			Vector vector = vectorObject.getVector();
			vector.superpose(vectors.getVector(key), 1, null);
		}
	}

	public Map<Integer,Hashtable<Object, ObjectVector>> getAllVectorsWithStatus() {
		return this.vectorsMap;
	}

	public DocInfo getDocInfo(String key) {
		return this.docInfos.get(key);
	}
}
