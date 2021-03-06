package com.ctvit.nlp.similary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
       	vmTopic = VectorModel.loadFromFile("hdfs://localhost:9000/model/topic2vec");   	
//    	vmTopic = VectorModel.loadFromFile("/tmp/zzl/model/topicAllVec");
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
