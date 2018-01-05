package com.ctvit.nlp.similary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.google.word2vec.VectorModel;
import com.google.word2vec.VectorModel.WordScore;

public class WordSimilar
{
	private static VectorModel vmZH;

	static {
		String path = Thread.currentThread().getContextClassLoader().getResource("db.properties").getPath();
		Properties prop = new Properties();// 属性集合对象
		FileInputStream fis;
		try {
			fis = new FileInputStream(path);
			prop.load(fis);// 将属性文件流装载到Properties对象中
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 属性文件流
		vmZH = VectorModel.loadFromFile(prop.getProperty("wordModelPath"));
	}
	public void query(String query)
	{
		Set<WordScore> _setResult = this.vmZH.similar(query);
		Iterator<WordScore> it = _setResult.iterator();
		while (it.hasNext())
		{
			WordScore ws = it.next();
			System.out.println(ws.name + ":" + ws.score);
		}

	}

	public static void main(String[] args)
	{
		WordSimilar _ws = new WordSimilar();
	
		while (true)
		{
			try
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("请输入检索词:");
				String word = br.readLine();
				if ("quit".equals(word))
				{
					System.out.println("daming，欢迎您进入本系统。");
					break;
				}
				else
				{
					_ws.query(word);
				}
			}
			catch (Exception e)
			{
			}
		}
	}
}
