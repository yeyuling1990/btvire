package com.ctvit.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

import com.ctvit.utils.Constant.LANG;



public class CommonUtils {
	private static Set<String> enCountries;
	private static Set<String> zhCountries;
	static{
		zhCountries=new HashSet<String>();
		enCountries=new HashSet<String>();
		String zhs="fmsq,fmzl,syxx";//,mo,hk,tw
		String[] zhsArr=zhs.split(",");
		for(String c:zhsArr){
			zhCountries.add(c);
		}
		String ens="ep,wo,us,jp,gb,fr,ru,kr,de,ch,it,ca,at,es,au,dd,in,br,ar,mx,su,docdb,oth,mo,hk,tw";
		String[] ensArr=ens.split(",");
		for(String c:ensArr){
			enCountries.add(c);
		}
	}
	public static void main(String[] args) {
		List<Integer> docs = new ArrayList<Integer>();
		for (int i = 0; i < 134; i++) {
			docs.add(i);
		}
		splitList(docs,20);
	}
	public static <T> List<List<T>> splitList(List<T> docs,int NUM) {
		List<List<T>> parts = new ArrayList<List<T>>();
		int N = docs.size();
		int L = N / NUM;
		int rem = N % NUM;
		System.out.println("N:" + N);
		System.out.println("L:" + L);
		System.out.println("rem:" + rem);

		for (int i = 0; i <= N - L - rem; i += L) {
			//System.out.println("i:" + i);
			if (i == N - L - rem)
				parts.add(docs.subList(i, N));
			else
				parts.add(docs.subList(i, i + L));
		}
		/*for (int j = 0; j < parts.size(); j++) {
			List<T> l = parts.get(j);
			System.out.println(j + "," + l);
		}*/
		return parts;
	}
	
	public static double[] convertFloatsToDoubles(float[] input) {
		if (input == null) {
			return null; // Or throw an exception - your choice input.length
		}
		double[] output = new double[input.length];// 150
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i];
		}
		return output;
	}

	public static float[] convertDoublesToFloats(double[] input) {
		if (input == null) {
			return null; // Or throw an exception - your choice
		}
		float[] output = new float[input.length];
		for (int i = 0; i < input.length; i++) {
			output[i] = (float) input[i];
		}
		return output;
	}
	
	public static boolean isPnChinese(String pn) {
		boolean bChinese = false;
		if (pn.startsWith("CN") || pn.startsWith("HK") || pn.startsWith("MO")
				|| pn.startsWith("TW"))
			bChinese = true;
		return bChinese;
	}
	
	public static boolean isZhCountry(String country){
		return zhCountries.contains(country);
	}
	
	private static boolean isEnCountry(String country){
		return enCountries.contains(country);
	}
	
	public static LANG getLangByCountry(String country){
		if(isZhCountry(country))
			return LANG.ZH;
		else
			return LANG.EN;
	}
	
	public static String processCountry(String name) {
		String str=name.split("-")[0];
		if(str.equals("jp1")|| str.equals("jp2")|| str.equals("jp3"))
			str="jp";
		if(str.equals("us1")|| str.equals("us2")|| str.equals("us3"))
			str="us";
		return str;
	}
}
