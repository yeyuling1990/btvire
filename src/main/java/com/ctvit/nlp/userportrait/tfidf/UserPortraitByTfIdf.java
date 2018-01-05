package com.ctvit.nlp.userportrait.tfidf;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *读取数据库中日志信息，得到用户的浏览记录
 *
 * @author zhilin zhang
 * @date   2017年10月27日下午5:36:54
 * @version 1.0
 */

public class UserPortraitByTfIdf {
	private static Logger logger = LoggerFactory.getLogger(UserPortraitByTfIdf.class);
	private Connection conn;
	private Statement ps;
	
	//全库文本处理后全部词的idf值
	private HashMap<String, Double> allWordIdf;
	
	public UserPortraitByTfIdf() throws SQLException {
		// TODO Auto-generated constructor stub
		conn=DbConnections.getContentDataConnection();
		ps = conn.createStatement();
		//计算出IDF值
		computeIDF();	
	}
	
	
	//根据用户的id得到相对应的labels
	public List<String> getUserPortrait(String id)
	{
		List<String> userPortrait = new ArrayList<String>();
		
		//读取用户行为日志,得到每个用户及其浏览记录
		String path = "E:\\中视广信\\06_用户画像\\search_result\\search_result\\device_id_hot50_dataSet.csv";
		HashMap<String,List<String>> id_contentList = getByUidByCsv(path);
		
//		for(String str:id_contentList.keySet())
//		{
//			System.out.println(str+":"+id_contentList.get(str));
//		}
		
		//针对某个用户的浏览记录，根据channel分类，得到不同channel类别下的article
		List<String> contentList = id_contentList.get(id);
		System.out.println(contentList);
		HashMap<String,List<Article>> channelArticle = getChannelArticle(contentList);
		
//		for(String string:channelArticle.keySet())
//		{
//			List<article> arList = channelArticle.get(string);
//			System.out.println(string);
//			for(article art:arList)
//			{
//				System.out.println(art.getContent()+"       ");
//			}
//		}	
		
//		//计算得到每个channel对应的一系列的label
		HashMap<String,List<String>> channelLabels= getChannelLabels(channelArticle);
		
		for(String str : channelLabels.keySet())
		{		
			userPortrait.addAll(channelLabels.get(str));
		}
		
		System.out.println("用户标签："+userPortrait);
		return userPortrait;
	}
	
		
	/*
	 * 计算IDF值，使用全库中的内容来计算。
	 * 将数据库中全部article使用HanLp分词，然后计算得到IDF值
	 * 
	 * */
	public void computeIDF()
	{
		allWordIdf= new HashMap<String, Double>();
		HashMap<String, Double> allWordIdfTemp= new HashMap<String, Double>();
		ResultSet rs =null;
		SegHnlp seg=new SegHnlp();
		
		try {
			String content_sql = "select ti,content from q_article;";
			rs=ps.executeQuery(content_sql);
			List<String> flagDF = null;
			double countNum=0;
			while(rs.next())
			{
				countNum++;
				flagDF =new ArrayList<String>();
				String content = rs.getString("content");
				if(content == null)
				{
					content="";
				}
				String ti = rs.getString("ti");
				if(ti == null)
				{
					ti="";
				}
				String cuTermsTerms=seg.seg(content+" "+ti);
				for(String t:cuTermsTerms.split(" "))
				{
					if(!flagDF.contains(t))
					{
						if(allWordIdfTemp.containsKey(t))
						{
							double value = allWordIdfTemp.get(t)+1;
							allWordIdfTemp.put(t, value);
						}else {
							allWordIdfTemp.put(t, 1.0);
						}
						flagDF.add(t);
					}

				}
			}
			
			for(Entry<String, Double> wordIdf:allWordIdfTemp.entrySet())
			{
//				if(wordIdf.getValue() > 5)
				{
					double idfValue=Math.log10(countNum/(wordIdf.getValue()));
					allWordIdf.put(wordIdf.getKey(),idfValue);
				}
			}
			logger.info("计算全库内容中的词的idf值");
			logger.info("库中有"+countNum+"条内容元数据");
			System.out.println("库中有"+countNum+"条内容元数据");
		} catch (Exception e) {
			// TODO: handle exception
		}		
	}
	
	
	//读取用户浏览记录的csv文件，返回每个用户的浏览记录，string代表用户，list代表浏览记录
	public static HashMap<String,List<String>> getByUidByCsv(String path)
	{	
		HashMap<String,List<String>> id_contentIdList= new HashMap<String, List<String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String s=null;
			try {
				while((s=br.readLine())!=null) {
					String[] arr= s.split(",");
					if(id_contentIdList.containsKey(arr[0]))
					{
						List<String> contentIdlist = id_contentIdList.get(arr[0]);
						contentIdlist.add(arr[2]);
						id_contentIdList.put(arr[0], contentIdlist);
					}else{
						List<String> contentIdlist=new ArrayList<String>();
						contentIdlist.add(arr[2]);
						id_contentIdList.put(arr[0],contentIdlist);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
		}
		return id_contentIdList;
	}
	
	
	/*
	 * 根据浏览记录，整理得到每个用户在不同channel的浏览内容的关键标签,暂时不用
	 * 
	 * 
	public HashMap<String, HashMap<String,List<article>>> sortContentByChannel(HashMap<String,List<String>> id_contentIdList)
	{	
		HashMap<String,List<article>> channel_articleList=new HashMap<String, List<article>>();
		
		for(String id:id_contentIdList.keySet())
		{	
			List<String> contentIdList=id_contentIdList.get(id);
			channel_articleList = getChannelArticle(contentIdList);
			
		}
		return null;
	}
	*/
	
	//根据不同的内容id，读取数据库，按照不同的channel得到不同的article集合
	public HashMap<String,List<Article>> getChannelArticle(List<String> contentIdList)
	{
		HashMap<String,List<Article>> channelArticle = new HashMap<String, List<Article>>();	
		String idStrSql=Utils.listToStringSql(contentIdList);		
		String sqlSelectArticle= "select * from q_test where mid in ("+idStrSql+");";
		System.out.println("sql查询语句："+sqlSelectArticle);
		ResultSet rs = null;		
		try {
			rs = ps.executeQuery(sqlSelectArticle);
			Article arti1=null;			
			while(rs.next())
			{
				String ti = rs.getString("ti");
				String mid = rs.getString("mid");
				String brief = rs.getString("brief");
				String tag = rs.getString("tag");
				String content = rs.getString("content");
				String column1 = rs.getString("column1");
				String channel = rs.getString("channel");
				
				if(content != null || channel != null || ti != null)
				{
					arti1=new Article();
					arti1.setMid(mid);
					arti1.setTi(ti);
					arti1.setBrief(brief);
					arti1.setTag(tag);
					arti1.setContent(content);
					arti1.setColumn1(column1);
					arti1.setChannel(channel);
					
					if (channelArticle.containsKey(channel)) {
						List<Article> articleList=channelArticle.get(channel);
						articleList.add(arti1);
						channelArticle.put(channel, articleList);
					}else {
						List<Article> articleList = new ArrayList<Article>();
						articleList.add(arti1);
						channelArticle.put(channel,articleList);
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return channelArticle;
	}
	
	//根据传入的频道string和文章内容列表list，得到每个频道相应个数的关键词
	public HashMap<String,List<String>> getChannelLabels(HashMap<String,List<Article>> channelArticle)
	{
		HashMap<String,List<String>> channelLabels = new HashMap<String, List<String>>();
		int countNum=0;      //共浏览了多少内容
		HashMap<String, Integer> channelCount = new HashMap<String, Integer>();
		for(String str : channelArticle.keySet()){
			List<Article> artList = channelArticle.get(str);
			int size = artList.size();
			channelCount.put(str, size);
			countNum += size;
		}
		
		for(String str : channelArticle.keySet())
		{
			List<Article> artList = channelArticle.get(str);
			List<String> channelWordLabel = getWordLabel(artList);
			
			int size = channelCount.get(str);
			int chooseNum= (int)(((double)size/countNum)*200);
			List<String> channelWordLabelSelect = new ArrayList<String>();
			if(channelWordLabel.size() > chooseNum)
			{
				channelWordLabelSelect = channelWordLabel.subList(0, chooseNum);

			}else {
				channelWordLabelSelect = channelWordLabel;
			}

			channelLabels.put(str, channelWordLabelSelect);
			System.out.println("每个频道对应的词个数及词："+str+":"+chooseNum+":"+channelWordLabelSelect);
		}
		
		return channelLabels;
	}
	
	//根据文章列表，得到指定数量的标签
	public List<String> getWordLabel(List<Article> articleList){
		
		List<String> resultLabel = new ArrayList<String>();
		SegHnlp seg = new SegHnlp();
		
		//计算各个单词的TFvalue
		HashMap<String, Double> tfIdfValue=new HashMap<String, Double>();
		HashMap<String, Double> tfValue = new HashMap<String, Double>();
		for(Article art:articleList)
		{
			String ti = art.getTi();
			if(ti == null)
			{
				ti="";
			}
			String cutTermsTerms=seg.seg(ti);	
			for(String t:cutTermsTerms.split(" "))
			{
				if(tfValue.containsKey(t))
				{
					Double wordNum=tfValue.get(t);
					tfValue.put(t, wordNum+1);
				}else {
					tfValue.put(t, 1.0);					
				}
			}			
		}
		
		//计算各个词的TfIdfvalue
		for(String word : tfValue.keySet())
		{
			if(allWordIdf.containsKey(word))
			{
				double wordOfTfIdf = (tfValue.get(word))*(allWordIdf.get(word));
				tfIdfValue.put(word, wordOfTfIdf);
			}
			else{
				tfIdfValue.put(word, 0.0);
			}

		}
		
		//根据tfidf value排序word标签
		List<Map.Entry<String, Double>> wordOfTfIdfOrder =new ArrayList<Map.Entry<String, Double>>(tfIdfValue.entrySet());
		Collections.sort(wordOfTfIdfOrder,new Comparator<Map.Entry<String,Double>>() {
            //降序排序
			public int compare(Entry<String, Double> o1,
	                    Entry<String, Double> o2) {
	                return o2.getValue().compareTo(o1.getValue());
	            }    
	        });
		
		//遍历得到200个label
		int num=0;
		for(Entry<String, Double> mapping:wordOfTfIdfOrder)
		{ 
             if(num < 200)
             {
            	 resultLabel.add(mapping.getKey());
            	 num++;
             }
             else {
				break;
			}
		}
		
		return resultLabel; 
  }
		

	public static void main(String[] args){
		
//		HashMap<String, String> id_content_source=new HashMap<String, String>();
//		try {
//			getUserRecording gur = new getUserRecording();
//			id_content_source=gur.getByUidBySql("q_article");
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for(String ss:id_content_source.keySet())
//		{
//			System.out.println(ss+":"+id_content_source.get(ss));
//		}
//		String path = "E:\\中视广信\\06_用户画像\\search_result\\search_result\\device_id_hot50_dataSet.csv";
//		HashMap<String,List<String>> id_content=getByUidByCsv(path);
//		int n=0;
//		for(Entry<String, List<String>> ss:id_content.entrySet())
//		{
//			List<String> content=new ArrayList<String>();
//			List<String> content_list=ss.getValue();
//			for(String id:content_list)
//			{
//				if(id_content_source.containsKey(id))
//				{
//					content.add(id_content_source.get(id));
//				}
//			}
//			
//			System.out.println(ss.getKey()+":"+ss.getValue());
//		}
//		System.out.println(n);
		
//		try {
//			(new getUserPortrait()).computeIDF();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
			System.setOut(new PrintStream(new FileOutputStream("output.txt")));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
		System.out.println("This is test output");
		try {
			UserPortraitByTfIdf userportrait= new UserPortraitByTfIdf();
			userportrait.getUserPortrait("\"38454FA1-6BF5-423E-BAC5-84D1763E127C\"");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
