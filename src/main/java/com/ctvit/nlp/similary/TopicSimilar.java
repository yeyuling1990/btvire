package com.ctvit.nlp.similary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.ctvit.nlp.learn.wordlist2vec.WordList2Vec;
import com.google.word2vec.VectorModel;
import com.google.word2vec.VectorModel.WordScore;

import pitt.search.semanticvectors.vectors.Vector;

public class TopicSimilar {
	private static VectorModel vmTopic;
     static{
    	String path = Thread.currentThread().getContextClassLoader().getResource("db.properties").getPath();
    	Properties prop = new Properties();//属性集合对象    
        FileInputStream fis;
		try {
			fis = new FileInputStream(path);
			 prop.load(fis);//将属性文件流装载到Properties对象中   
			 fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//属性文件流    
		
       
    	vmTopic = VectorModel.loadFromFile(prop.getProperty("topicModelPath"));
		vmTopic.setTopNSize(2);
	}
	/**
	 * 计算与输入词列表最相似的topic
	 * @param lst
	 * @return
	 */
	public Set<WordScore> computeBestTopic(List<String> lst)
	{
		Vector vec = getVecOfWords(lst);
		float [] arrf = vec.getCoordinates();
		Set<WordScore> set = vmTopic.similar(arrf);
		return set;
	}
	/**
	 * 计算与输入向量最相似的topic
	 * @param lst
	 * @return
	 */
	public Set<WordScore> computeBestTopic(float[] center)
	{
		Set<WordScore> set = vmTopic.similar(center);
		return set;
	}
	/**
	 * 计算与输入词列表最相似的topic,只有topic的名称以filterStartStr开头时才有效
	 * @param filterStartStr 过滤字符串，判断topic名称是否以filterStartStr开头
	 * @param lst
	 * @return
	 */
	public Set<WordScore> computeBestTopic(String filterStartStr, List<String> lst)
	{
		Vector vec = getVecOfWords(lst);
		float [] arrf = vec.getCoordinates();
		Set<WordScore> set = vmTopic.similar(filterStartStr,arrf);
		return set;
	}
	/**
	 * 计算与输入向量最相似的topic,只有topic的名称以filterStartStr开头时才有效
	 * @param filterStartStr 过滤字符串，判断topic名称是否以filterStartStr开头
	 * @param center
	 * @return
	 */
	public Set<WordScore> computeBestTopic(String filterStartStr,float[] center)
	{
		Set<WordScore> set = vmTopic.similar(center);
		return set;
	}
	private  Vector getVecOfWords(List<String> lst)
	{
		WordList2Vec vmWords = new WordList2Vec();
		Vector vec = vmWords.trainDocVector(lst);
		return vec;
	}
}
