package com.ctvit.utils;

public class Constant {
	public static enum LANG{
		EN,
		ZH
	}
	public static final String WORD_SPLIT=" ";
	private static final String ONE = "D:\\corpus\\oac\\OANC-GrAF\\data\\spoken\\face-to-face\\charlotte\\AdamsElissa.txt";
	private static final String TWO = "D:\\corpus\\oac\\OANC-GrAF\\data\\spoken\\face-to-face\\charlotte\\DeLuciaBrian.txt";
	private static final String THREE = "src\\test\\resources\\testdata\\John\\Chapter_1";
	private static final String FOUR = "src\\test\\resources\\testdata\\cn_fmsq_CN1242776C_ab";// cn_fmsq_CN1242776C
	private static final String FIVE = "CN1271538C";// cn_fmsq_CN1271538
	public static final String SOURCEDOCUMENT = FOUR;// THREE;//ONE;//FOUR
	public static final String TARGETDOCUMENT = FIVE;// TWO;
	
	private static String JOHN_CORPUS = "src/test/resources/testdata/John";
	private static String TEXT_INDEX = "luceneindexpath";
	private static String TORCH_CORPUS = "src/test/resources/testdata/TORCH09_UTF-8";

	private static String OANC_CORPUS = "D:/corpus/oac/OANC-GrAF";
	private static String OANC_INDEX = "D:/corpus/oac/OANC-GrAF/index";

	private static String ALL_CORPUS = "D:/corpus";
	private static String ALL_INDEX = "D:/corpus/index";


	// private static String
	// CN_FMSQ_MULTIFEILD_INDEX="D:\\workspace\\semanticvectors-5.5-trunk-server\\luceneindexpathfromdb_cn_fmsq_multifield";
	private static enum INDEX {

	}

	// private static String DB_INDEX=CN_FMSQ_MULTIFEILD_INDEX;//CN_FMSQ_INDEX;
	/************************************************/
	/************************************************/
	public static String CORPUS_PATH = TORCH_CORPUS;
	// public static String LUCENE_INDEX_PATH=DB_INDEX;
	public static String MODIFIED_CORPUS_PATH = "src/test/resources/testdata/modified";;
	/************************************************/
	public static String FIELDWEIGHT = "true";// "true";
	public static String DOCIDFIELD = "mid";// pn,path,title
	public static String CONTENTSFIELDS = "ti,brief,tag,content";// contents
	public static String LUCENE_INDEX = "luceneindex";
	public static int DOCVECTORSSPLITNUM = 50;

	public static String[] COUNTRY=new String[]{
		//en-ar##1
		"ar",
		//en-at##2
		"at-0","at-1",
		//en-au##3
		"au-0","au-1","au-2",
		//en-br##1
		"br",
		//en-ca##6
		"ca-0", "ca-1", "ca-2","ca-3", "ca-4", "ca-5",
		//en-ch##2
		"ch-0","ch-1",
		//en-dd##1
		"dd",
		//en-de##12
		"de-0","de-1","de-2","de-3","de-4","de-5","de-6",
		"de-7","de-8","de-9","de-10","de-11",
		//en-docdb##14
		//28
		//en-ep##10
		"ep-0","ep-1","ep-2","ep-3","ep-4","ep-5","ep-6",
		"ep-7","ep-8","ep-9",
		//en-es##3
		"es-0","es-1","es-2",
		//en-fr##6
		"fr-0","fr-1","fr-2","fr-3","fr-4","fr-5",
		//en-gb##6
		"gb-0","gb-1","gb-2","gb-3","gb-4","gb-5",
		//en-in##1
		"in",
		//en-it##2
		"it-0","it-1",
		//28
		/*//en-jp1##18
		"jp1-0","jp1-1","jp1-2","jp1-3","jp1-4","jp1-5",
		"jp1-6","jp1-7","jp1-8","jp1-9","jp1-10","jp1-11",
		"jp1-12","jp1-13","jp1-14","jp1-15","jp1-16","jp1-17",
		//en-jp2##16
		"jp2-0","jp2-1","jp2-2","jp2-3","jp2-4","jp2-5",
		"jp2-6","jp2-7","jp2-8","jp2-9","jp2-10","jp2-11",
		//30
		"jp2-12","jp2-13","jp2-14","jp2-15",
		//en-jp3##16
		"jp3-0","jp3-1","jp3-2","jp3-3","jp3-4","jp3-5",
		"jp3-6","jp3-7","jp3-8","jp3-9","jp3-10","jp3-11",
		"jp3-12","jp3-13","jp3-14","jp3-15",*/
		//en-kr##10
		"kr-0","kr-1","kr-2","kr-3","kr-4","kr-5",
		"kr-6","kr-7","kr-8","kr-9",
		//en-mx##1
		"mx",
		//en-ru##2
		"ru-0","ru-1",
		//en-su##3
		"su-0","su-1","su-2",
		//en-us##26
		//en-us1##5
		"us1-0","us1-1","us1-2","us1-3","us1-4",
		//en-us2##6
		"us2-0","us2-1","us2-2","us2-3","us2-4","us2-5",
		//en-us3##15
		"us3-0","us3-1","us3-2","us3-3","us3-4","us3-5",
		"us3-6","us3-7","us3-8","us3-9","us3-10","us3-11",
		"us3-12","us3-13","us3-14",
		//en-wo##6
		"wo-0","wo-1","wo-2","wo-3","wo-4","wo-5",
		////
		//cn-syxx##8
		"syxx-0","syxx-1","syxx-2","syxx-3","syxx-4","syxx-5",
		"syxx-6","syxx-7",
		//cn-fmsq##3
		"fmsq-0", "fmsq-1","fmsq-2",
		//cn-fmzl##8
		"fmzl-0","fmzl-1","fmzl-2","fmzl-3","fmzl-4","fmzl-5",
		"fmzl-6","fmzl-7",
		//cn-tw##4
		"tw-0","tw-1","tw-2","tw-3",
		//cn-hk##1
		"hk",
		//cn-mo##1
		"mo"
	};
	
	// syxx fmsq fmzl hk mo tw
	private static String[] CN = new String[] { "fmsq-0", "fmsq-1","fmsq-2"/*, "syxx-0", "syxx-1", "syxx-2",
			"syxx-3", "syxx-4", "syxx-5", "syxx-6", "syxx-7", "fmzl-0", "fmzl-1", "fmzl-2",
			"fmzl-3", "fmzl-4","fmzl-5","fmzl-6","fmzl-7","hk", "mo", "tw-0", "tw-1"*/ };
	// ar at au br ca ch "dd" de docdb ep es fr gb "in" it jp1 jp2 jp3 kr mx ru
	// "su" us wo
	private static String[] EN = new String[] { "ar", "au-0", "au-1",
			"br"/*, "ca-0", "ca-1", "ca-2", "at-0","at-1","ch", "dd", "es", "in", "it", "mx",
			"ru", "su", "wo-0", "wo-1", "wo-2", "de-0", "de-1", "de-2", "de-3",
			"de-4", "de-5", "ep-0", "ep-1", "ep-2", "ep-3", "ep-4", "fr-0",
			"fr-1", "fr-2", "gb-0", "gb-1", "gb-2", "kr-0", "kr-1", "kr-2",
			"kr-3", "kr-4", "jp1-0", "jp1-1", "jp1-2", "jp1-3", "jp1-4",//5
			"jp1-5", "jp1-6", "jp1-7", "jp1-8", "jp2-0", "jp2-1", "jp2-2",//7
			"jp2-3", "jp2-4", "jp2-5", "jp2-6", "jp2-7", "jp3-0", "jp3-1",//7
			"jp3-2", "jp3-3", "jp3-4", "jp3-5", "jp3-6", "jp3-7", "docdb-0",//7
			"docdb-1", "docdb-2", "docdb-3", "docdb-4", "docdb-5", "docdb-6",//6
			"us-0", "us-1", "us-2", "us-3", "us-4", "us-5", "us-6", "us-7",//8
			"us-8", "us-9", "us-10", "us-11", "us-12"*/ };//5

}
