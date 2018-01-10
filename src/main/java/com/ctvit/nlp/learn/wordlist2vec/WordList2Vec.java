package com.ctvit.nlp.learn.wordlist2vec;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.word2vec.VectorModel;

import pitt.search.semanticvectors.vectors.RealVector;
import pitt.search.semanticvectors.vectors.Vector;
import pitt.search.semanticvectors.vectors.VectorFactory;
import pitt.search.semanticvectors.vectors.VectorType;

public class WordList2Vec {

	private static VectorModel vmZH;
	private Map<String, float[]> wordMap = new HashMap<String, float[]>();
	private int vectorSize = 200; // 特征数

	static {
//		String path = Thread.currentThread().getContextClassLoader().getResource("db.properties").getPath();
//		Properties prop = new Properties();// 属性集合对象
//		FileInputStream fis;
//		try {
//			fis = new FileInputStream(path);
//			prop.load(fis);// 将属性文件流装载到Properties对象中
//			fis.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} // 属性文件流
//		vmZH = VectorModel.loadFromFile(prop.getProperty("wordModelPath"));
		vmZH = VectorModel.loadFromFile("/tmp/zzl/model/word2vec.model");
	}

	public Vector trainDocVector(List<String> wordLst) {
		Vector docVector = VectorFactory.createZeroVector(VectorType.REAL, 200);
		for (int i = 0; i < wordLst.size(); i++) {
			String sWord = wordLst.get(i);

			float[] _arrf = vmZH.getVectorOfStr(sWord);
			if (_arrf == null) {
				continue;
			}
			RealVector _termVector = new RealVector(_arrf);
			docVector.superpose(_termVector, 1.0, null);
		}
		docVector.normalize();

		return docVector;
	}

	public void printDocVec() {

	}

	/**
	 * 训练词列表为一个向量
	 * 
	 * @param map
	 *            key ：topic名称 value ：topic包含的词列表
	 */
	public void train(Map<String, List<String>> map) {

		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			String sKey = entry.getKey();
			List<String> _lst = entry.getValue();
			Vector vec = trainDocVector(_lst);
			float[] arrf = vec.getCoordinates();
			wordMap.put(sKey, arrf);
		}
	}

	public void saveModel(File file) {

		DataOutputStream dataOutputStream = null;
		try {
			dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			dataOutputStream.writeInt(wordMap.size());
			// System.out.println(wordMap.size());
			dataOutputStream.writeInt(vectorSize);
			// System.out.println(vectorSize);
			for (Map.Entry<String, float[]> element : wordMap.entrySet()) {
				dataOutputStream.writeUTF(element.getKey());
				// System.out.println(element.getKey());
				for (float d : element.getValue()) {
					dataOutputStream.writeFloat(d);
					// System.out.print(d+",");
				}
				// System.out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (dataOutputStream != null) {
					dataOutputStream.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		WordList2Vec wl2vec = new WordList2Vec();
		Map<String, List<String>> _map = new HashMap<String, List<String>>();

		try {
			InputStreamReader inStream = new InputStreamReader(
					new FileInputStream(new File("D:\\vectorindex\\cctv14.twords")), "utf-8");
			BufferedReader br = new BufferedReader(inStream);// 构造一个BufferedReader类来读取文件
			String s = "";
			int m = 0;
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				m++;
				String[] arrs = s.split(";");

				List<String> topic = new ArrayList<String>();

				for (int i = 0; i < arrs.length; i++) {
					topic.add(arrs[i]);
				}
				_map.put("cctv14t" + m, topic);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		wl2vec.train(_map);
		wl2vec.saveModel(new File("D:\\vectorindex\\topic14"));
		System.out.println("fffhhh");
	}

}
