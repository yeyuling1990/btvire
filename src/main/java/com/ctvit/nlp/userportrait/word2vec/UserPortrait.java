package com.ctvit.nlp.userportrait.word2vec;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class UserPortrait {

	/**
	 计算喜好的频道权重，并写入数据表
	 * */
	
	public void cmptFavoriteChannel()
	{
		Connection conn = null;
		Statement stmt = null;
		String sqlTabTruncate = "TRUNCATE TABLE cctv_cms.a_user_favorite_channel;";
		String sqlInsertTest = "insert into a_user_favorite_channel(userid,channel,weights)value(?,?,?);";
		
		String altUserFavChannel = "insert into cctv_cms.a_user_favorite_channel "
				+ "select A.userid,B.tchannel,B.channelNum/A.countAll*1.00 from "
				+ "(select userid,sum(count) as countAll from cctv_cms.a_user_favorite group by userid) as A , "
				+ "(select userid,tchannel,sum(count) as channelNum from cctv_cms.a_user_favorite group by userid,tchannel) as B "
				+ "where A.userid = B.userid;";
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			stmt.execute(sqlTabTruncate);
			stmt.execute(altUserFavChannel);
			
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	/**
	 计算每个频道中喜好的topic，并写入数据表
	 * */
	public void cmptFavoriteTopic()
	{
		Connection conn = null;
		Statement stmt = null;
		String sqlTabTruncate = "TRUNCATE TABLE cctv_cms.a_user_favorite_topic;";
		String altUserFavTop ="insert into cctv_cms.a_user_favorite_topic "
				+ "select A.userid,A.tchannel,A.topicid,A.count/B.countAll*1.00 as weights "
				+ "from cctv_cms.a_user_favorite as A , "
				+ "(select userid,sum(count) as countAll from cctv_cms.a_user_favorite group by userid) as B "
				+ "where A.userid = B.userid;";
		
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			
			stmt.execute(sqlTabTruncate);
			stmt.execute(altUserFavTop);
			
			stmt.close();
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	public Map<String,Double> getFavoriteChannel(String userid)
	{
		Map<String,Double>  _map = new HashMap<String,Double>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sqlFavChannelByUid = "select * from cctv_cms.a_user_favorite_channel where userid = \""+userid+"\"";
		
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlFavChannelByUid);
			
			while(rs.next())
			{
				String channle = rs.getString("channel");
				if(channle == null)
				{
					channle ="";
				}
				Double weight = rs.getDouble("weights");
				if(weight == null)
				{
					weight = 0.0;
				}
				_map.put(channle, weight);
			}
			
			rs.close();
			stmt.close();
			conn.close();
			
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return _map;
	}
	
	public Map<String,Double> getFavoriteTopic(String userid,String channel)
	{
		Map<String,Double>  _map = new HashMap<String,Double>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sqlFavChannelByUid = "select * from cctv_cms.a_user_favorite_topic where userid = \""+userid+"\"";
		
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlFavChannelByUid);
			
			while(rs.next())
			{
				String topicid = rs.getString("topicid");
				if(topicid == null)
				{
					topicid ="";
				}
				Double weight = rs.getDouble("weights");
				if(weight == null)
				{
					weight = 0.0;
				}
				_map.put(topicid, weight);
			}
			
			rs.close();
			stmt.close();
			conn.close();
			
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return _map;
	}
	
	
	public static void main(String[] args){
		UserPortrait userPortrait = new UserPortrait();
		userPortrait.cmptFavoriteChannel();
		userPortrait.cmptFavoriteTopic();
	} 
}
