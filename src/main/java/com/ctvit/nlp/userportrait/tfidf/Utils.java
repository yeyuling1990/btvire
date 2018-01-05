package com.ctvit.nlp.userportrait.tfidf;

import java.util.ArrayList;
import java.util.List;

public class Utils{
	
	public static String listToStringSql(List<String> list)
	{
		String strResult="";
		for(String str:list)
		{
			String tmp=str;
			strResult =strResult+tmp+",";
		}
		strResult =strResult.substring(0,strResult.length()-1);
//		System.out.println(strResult);
		return strResult;
	}
	
	public static void main(String[] args)
	{
		List<String> test= new ArrayList<String>();
		test.add("a");
		test.add("b");
		test.add("c");
		test.add("d");
		test.add("g");
		
		String result = listToStringSql(test);
		System.out.println(result);
		
	}

}
