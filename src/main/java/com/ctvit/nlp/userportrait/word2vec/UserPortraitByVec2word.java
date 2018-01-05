package com.ctvit.nlp.userportrait.word2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.attribute.HashAttributeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctvit.nlp.learn.wordlist2vec.WordList2Vec;
import com.ctvit.nlp.similary.TopicSimilar;
import com.ctvit.nlp.userportrait.tfidf.Article;
import com.ctvit.nlp.userportrait.tfidf.DbConnections;
import com.ctvit.nlp.userportrait.tfidf.Utils;
import com.google.word2vec.VectorModel.WordScore;
import com.hankcs.hanlp.HanLP;


public class UserPortraitByVec2word {
	private Logger logger = LoggerFactory.getLogger(UserPortraitByVec2word.class);
	private Connection conn;
	private Statement ps;
	
	public UserPortraitByVec2word() throws SQLException
	{
		conn=DbConnections.getContentDataConnection();
		ps = conn.createStatement();
	}
	
	/**
	 * 计算所有文章所属的频道及两个最相似的topic,并插入到article_topic表中。
	 */
	public void computeArticleTopic() {
		
		String topicPath = "data\\topic2Vec.model";
		String wordPath = "data\\word2vec.model";
		TopicSimilar topicSim = new TopicSimilar();
		
		String sql_article_table = "select * from q_test where channel !='';";
		
		List<String> filter = new ArrayList<>();
		filter.add("cctv1");filter.add("cctv10");filter.add("cctv12");filter.add("cctv14");
		filter.add("cctv15");filter.add("cctv3");filter.add("cctv4");
		filter.add("cctv7");
		ResultSet rs =null;
		try {
			rs = ps.executeQuery(sql_article_table);
			Article arti1=null;
			while (rs.next()) {
				
				String mid = rs.getString("mid");
				String content = rs.getString("content");
				if(content == null)
				{
					content = "";
				}
				String channel = rs.getString("channel");
				if(channel == null)
				{
					channel = "";
				}
				
				if(channel != "" && content != "")
				{
					channel =channel.toLowerCase();
					if(filter.contains(channel)){
						List<String> keyword = HanLP.extractKeyword(content, 100);
						Set<WordScore> topicSet = topicSim.computeBestTopic(channel,keyword);
						
						boolean flag = false;
						ArticleTopic artAndTopic = new ArticleTopic();
						
						artAndTopic.setArticle_id(mid);
						artAndTopic.setChannel(channel);
						for(WordScore ws:topicSet)
						{
							if(flag == false)
							{
								artAndTopic.setTopic1(ws.getName());
								artAndTopic.setSim_topic1(Double.valueOf(ws.getScore()));
								flag = true;
							}
							else {
								artAndTopic.setTopic2(ws.getName());
								artAndTopic.setSim_topic2(Double.valueOf(ws.getScore()));
							}
							
						}
						//将对象插入到文章表
						insertArticleTopicToMysql(artAndTopic);	
					}
					
				}		
									
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 将ArticleTopic对象插入到article_topic表中
	 * @param artTop
	 */
	public void insertArticleTopicToMysql(ArticleTopic artTop){
		
		String sql="insert into cctv_cms.article_topic "
				+ "(article_id,channel,topic1,sim_topic1,topic2,sim_topic2) "
				+ "values (?,?,?,?,?,?)";
		
		try {
			PreparedStatement preStmt=conn.prepareStatement(sql);
			preStmt.setString(1, artTop.getArticle_id());
			preStmt.setString(2, artTop.getChannel());
			preStmt.setString(3, artTop.getTopic1());
			preStmt.setDouble(4, artTop.getSim_topic1());
			preStmt.setString(5, artTop.getTopic2());
			preStmt.setDouble(6, artTop.getSim_topic2());
			
			preStmt.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	/**
	 * //根据LDA得到的topic文件，得到所有的topic vector，存放到topicAllVec文件中
	 */
	public static void getAllTopicVec() {
		WordList2Vec wl2vec = new WordList2Vec();
		Map<String,List<String>> _map = new HashMap<String,List<String>>();
		
		try {
			File dir=new File("data\\训练结果");
			File[] files=dir.listFiles();
			if (files !=null) {
				for(int fi=0;fi<files.length;fi++)
				{
					String fileName = files[fi].toString();
					String channelName=fileName.substring(10, fileName.length()-7);
					System.out.println(channelName);
					String strFileName = files[fi].getAbsolutePath();
					InputStreamReader inStream = new InputStreamReader(new FileInputStream(new File(strFileName)), "utf-8");
					BufferedReader br = new BufferedReader(inStream);// 构造一个BufferedReader类来读取文件
					String s = "";
					
					int m=0;
					while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
//						System.out.println(s);
						m++;
						List<String> topic = new ArrayList<String>();
						String[] str=s.split(":");
						String [] arrs = str[1].trim().split(" 	");
						for(int i=0;i<arrs.length;i++)
						{
							topic.add(arrs[i]);
						}
						if (channelName.contains("cctv5") ) {
							System.out.println(channelName+"t"+m+":"+topic);
							Thread.sleep(100);
						}
						_map.put(channelName+"t"+m, topic);
					}
					br.close();
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		wl2vec.train(_map);
		wl2vec.saveModel(new File("data\\topicAllVec"));
		System.out.println("fffhhh");
	}
	
	
	
	
	/**
	 * 读取用户浏览记录的csv文件，返回每个用户的浏览记录
	 * @param path 浏览记录文件存放的位置
	 * @return string代表用户，list代表浏览记录的
	 */
	public HashMap<String,List<String>> getByUidByCsv(String path)
	{	
		HashMap<String,List<String>> userId_contentIdList= new HashMap<String, List<String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String s=null;
			try {
				while((s=br.readLine())!=null) {
					String[] arr= s.split(",");
					if(userId_contentIdList.containsKey(arr[0]))
					{
						List<String> contentIdlist = userId_contentIdList.get(arr[0]);
						contentIdlist.add(arr[2]);
						userId_contentIdList.put(arr[0], contentIdlist);
					}else{
						List<String> contentIdlist=new ArrayList<String>();
						contentIdlist.add(arr[2]);
						userId_contentIdList.put(arr[0],contentIdlist);
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
		return userId_contentIdList;
	}
	
	
	
	/**
	 * 根据用户的浏览记录，从article_topic表中得到某条id所属的channel和话题，最后得到用户在每个频道的topic中的计数
	 * @param contentIdList   用户的浏览id集合
	 * @return HashMap<String, Map<String, Integer>>   String代表频道，Map<String, Integer>代表topic及其count值
	 */
	public HashMap<String, Map<String, Integer>> getUserChannelTopicCount(List<String> contentIdList)
	{
		HashMap<String, Map<String, Integer>> userChannelTopicCount = new HashMap<String, Map<String, Integer>>();
		
		String idStrSql=Utils.listToStringSql(contentIdList);
		String sqlSelectArticleTopic= "select * from cctv_cms.a_article_topic where article_id in ("+idStrSql+");";
//		System.out.println("sql查询语句："+sqlSelectArticleTopic);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ResultSet rs = null;
		try {
			rs = ps.executeQuery(sqlSelectArticleTopic);
			int count=0;
			while(rs.next())
			{
				count++;
				String article_id = rs.getString("article_id");
				String channel = rs.getString("channel");
				String topic1 = rs.getString("topic1");
				String sim_topic1 = rs.getString("sim_topic1");
				String topic2 = rs.getString("topic2");
				String sim_topic2 = rs.getString("sim_topic2");
				
				if (userChannelTopicCount.containsKey(channel)) {
					Map<String, Integer> topicCount = userChannelTopicCount.get(channel);
					if(topicCount.containsKey(topic1))
					{
						int val = topicCount.get(topic1)+1;
						topicCount.put(topic1, val);
					}else{
						topicCount.put(topic1, 1);
					}
					
					if(topicCount.containsKey(topic2))
					{
						int val = topicCount.get(topic2)+1;
						topicCount.put(topic2, val);
					}else{
						topicCount.put(topic2, 1);
					}
					
					userChannelTopicCount.put(channel, topicCount);
				}else{
					Map<String, Integer> topicCount = new HashMap<>();
					topicCount.put(topic1, 1);
					topicCount.put(topic2, 1);
					userChannelTopicCount.put(channel, topicCount);
				}
			}
			System.out.println("rs的结果数："+count);
		} catch (SQLException e) {
			// TODO: handle exception
		}
		return userChannelTopicCount;
	}
	
	
	
	/**
	 * 将某个用户的计算出来的topic插入到mysql中
	 * @param uid  用户ID
	 * @param ChannelTopicCount 某个用户的在不同channel的不同topic和计数
	 */
	public void insertChannelTopicToSql(String uid , HashMap<String, Map<String, Integer>> ChannelTopicCount){
		
		String sql = "insert into a_user_trait(userid,tchannel,topicid,count) values (?,?,?,?) ";
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			conn.setAutoCommit(false);
			
			for(String channel : ChannelTopicCount.keySet())
			{
				Map<String, Integer> topicCount = ChannelTopicCount.get(channel);
				for(String topic: topicCount.keySet())
				{
					stmt.setString(1, uid);
					stmt.setString(2, channel);
					stmt.setString(3, topic);
					stmt.setInt(4, topicCount.get(topic));
					stmt.addBatch();
				}
			}
			
			stmt.executeBatch();
			conn.commit();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * 将list转换成string
	 * @param list 用户浏览记录
	 * @return string 转换成可以嵌入得到sql中的string
	 */
	public String listToStringSql(List<String> list)
	{
		String strResult="";
		for(String str:list)
		{
			String tmp=str;
			strResult =strResult+tmp+",";
		}
		strResult =strResult.substring(0,strResult.length()-1);
		System.out.println(strResult);
		return strResult;
	}
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		UserPortraitByVec2word userPortrait = new UserPortraitByVec2word();
				
		//根据LDA得到的topic文件，得到所有的topic vector，存放到topicAllVec文件中
//		getAllTopicVec();
	
		//计算所有文章所属的频道及两个最相似的topic,并插入到article_topic表中。
		userPortrait.computeArticleTopic();	
		
		/**
		//测试某几个uid的userPortrait
		List<String> uidTest = new ArrayList<>();
		String device_id_hot100="E:\\中视广信\\06_用户画像\\search_result\\search_result\\device_id_hot100.csv";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(device_id_hot100)));
			String s=null;
			try {
				while((s=br.readLine())!=null) {
					String[] arr= s.split(",");
					uidTest.add(arr[0].replace("\"", ""));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String path = "E:\\中视广信\\06_用户画像\\search_result1\\search_result\\device_id_hot50_dataSet.csv";
		HashMap<String,List<String>> userId_contentIdList =userPortrait.getByUidByCsv(path);
		
//		System.out.println(userId_contentIdList.keySet());
		
		List<String> contentIdList = new ArrayList<>();
		for(String uid : uidTest)
		{
			contentIdList = userId_contentIdList.get("\""+uid+"\"");
			System.out.println("contentIdList:"+contentIdList.size());
			HashMap<String, Map<String, Integer>> userChannelTopicCount = userPortrait.getUserChannelTopicCount(contentIdList);
			userPortrait.insertChannelTopicToSql(uid,userChannelTopicCount);
		}
//		**/

	}
    
}
