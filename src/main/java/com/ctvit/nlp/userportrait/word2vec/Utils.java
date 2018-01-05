package com.ctvit.nlp.userportrait.word2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

public class Utils {
	
//	public static BloomFilter<String> fileter = new BloomFilter(0.00000001,10000000);

	/*
	 * static中代码通过使用bloomFilter来加载人名字典，快速过滤。
	 * */
	static{			
////		try {
////			BufferedReader br = new BufferedReader(new FileReader(new File("E:\\中视广信\\onlyword2.dic")));
////			String line = "";
////			while ((line = br.readLine()) != null) {
////				fileter.add(line);
////			}
////			// System.out.println("count:"+count);
////
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////
////		fileter.saveFilterToFile("C:\\11.obj");
		 
//		fileter = fileter.readFilterFromFile("hdfs://localhost:9000/model/11.obj");  
//		System.out.println("加载nrentity完成");
	}
	
	
	/**
	 * 该方法用来提取sentence中涉及到的人名命名实体
	 * @param sentence
	 * @return
	 */
	public static String cmpNrEntity(String sentence1){		
		List<String> entityList = new ArrayList<>();
		
		Segment segment = HanLP.newSegment().enableAllNamedEntityRecognize(true);
		
		Pattern p1 = Pattern.compile("本文编辑|本文记者");
		Matcher m1 = p1.matcher(sentence1);
		if(m1.find())
		{
			int index1 = sentence1.indexOf("本文编辑");			
			if(index1 != -1)
			{
				String subsentence1 = sentence1.substring(0, index1);
				String subsentence2 = sentence1.substring(index1,sentence1.length());
				if(subsentence2.length() > 10)
				{
					subsentence2 = subsentence2.substring(10,subsentence2.length());
					sentence1 =subsentence1+subsentence2;
				}
				else {
					sentence1 = subsentence1;
				}
			}
			int index2 = sentence1.indexOf("本文记者");
			if(index2 != -1)
			{
				String subsentence1 = sentence1.substring(0, index2);
				String subsentence2 = sentence1.substring(index2,sentence1.length());
				if(subsentence2.length() > 10)
				{
					subsentence2 = subsentence2.substring(10,subsentence2.length());
					sentence1 =subsentence1+subsentence2;
				}
				else {
					sentence1 = subsentence1;
				}				
			}
		}
				
		List<Term> segList = segment.seg(sentence1);		
		
		for(Term term : segList)
		{
			if(String.valueOf(term.nature).contains("nr"))
			{
				if(!term.word.contains("先生") && !term.word.contains("女士") 
						&& !term.word.contains("教授") && !term.word.contains("小姐")
						&& !term.word.contains("医生") && term.word.length() >1)
				{
					String nrtity = term.word.trim();
//					if(fileter.contains(nrtity))
					{
						if(!entityList.contains(nrtity))
						{
							entityList.add(nrtity);
						}
					}
				}	
			}			
		}
				
		String entityListString = listToStringSql(entityList);
		return entityListString;
	}
		
	/**
	 * 将List转换成string
	 * @param list
	 * @return
	 */
	public static String listToStringSql(List<String> list)
	{
		String strResult="";
		for(String str:list)
		{
			String tmp=str;
			strResult =strResult+tmp+",";
		}
		if (strResult.length()>1) {
			strResult =strResult.substring(0,strResult.length()-1);
		}
		return strResult;
	}
	
	public static void main(String[] args)
	{
		String senttence1 = "你好,世界";
		Segment segment = HanLP.newSegment();
		List<Term> ss = segment.seg(senttence1);
		for(Term t : ss)
		{
			System.out.println(t.word+":"+t.word.length());
		}
	}
}
