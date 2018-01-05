package com.ctvit.nlp.userportrait.word2vec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ctvit.nlp.model.RecommendReady;

public class UserRecommend {

	// 用户喜好的topic，key用户名称，value喜好map（key：topic，value:喜好值）
	private static Map<String, Map<String, Double>> mapUserLikeTopic;
	// 用户喜好的channel，key用户名称，value喜好map（key：topic，value:喜好值）
	private static Map<String, Map<String, Double>> mapUserLikeChannel;
	static {
		mapUserLikeTopic = new HashMap<String, Map<String, Double>>();
		mapUserLikeTopic = loadTabFavTopToMap("a_user_favorite_topic");
		// 从数据库中读取填充map
		mapUserLikeChannel = new HashMap<String, Map<String, Double>>();
		// 从数据库中读取填充map
		mapUserLikeChannel = loadTabFavChanToMap("a_user_favorite_channel");
	}
	
	/**
	 * 从数据库中读取用户喜欢的topic和权重
	 * @param tableName a_user_favorite_topic
	 * @return
	 */
	public static HashMap<String, Map<String, Double>> loadTabFavTopToMap(String tableName)
	{
		HashMap<String, Map<String, Double>> resultMap = new HashMap<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			String sqlQuery = "select * from cctv_cms."+tableName+" ;";
			rs = stmt.executeQuery(sqlQuery);
			while(rs.next())
			{
				String userid = rs.getString(1);
				String topicid = rs.getString(3);
				Double weights = rs.getDouble(4);
				
				if(resultMap.containsKey(userid))
				{
					Map<String, Double> topicWei = resultMap.get(userid);
					topicWei.put(topicid,weights);
					resultMap.put(userid, topicWei);
				}else {
					Map<String, Double> topicWei = new HashMap<>();
					topicWei.put(topicid,weights);
					resultMap.put(userid, topicWei);
				}	
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}
	
	/**
	 * 从数据库中读取用户喜欢的频道和权重
	 * @param tableName a_user_favorite_channel
	 * @return
	 */
	public static HashMap<String, Map<String, Double>> loadTabFavChanToMap(String tableName)
	{
		HashMap<String, Map<String, Double>> resultMap = new HashMap<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			String sqlQuery = "select * from cctv_cms."+tableName+" order by weights desc;";
			rs = stmt.executeQuery(sqlQuery);
			while(rs.next())
			{
				String userid = rs.getString(1);
				String channelid = rs.getString(2);
				Double weights = rs.getDouble(3);
				
				if(resultMap.containsKey(userid))
				{
					Map<String, Double> topicWei = resultMap.get(userid);
					topicWei.put(channelid,weights);
					resultMap.put(userid, topicWei);
				}else {
					Map<String, Double> topicWei = new HashMap<>();
					topicWei.put(channelid,weights);
					resultMap.put(userid, topicWei);
				}	
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}

	
	/**
	 * 得到全部用户的userid
	 * @param userTable userinfo表
	 * @return
	 */
	public List<String> getLstUser(String userTable)
	{
		List<String>  _lstUser =  new ArrayList<String>();	
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			String sqlQuery = "select * from "+userTable+" ;";
			rs = stmt.executeQuery(sqlQuery);
			while(rs.next())
			{
				String uid = rs.getString(1);
				_lstUser.add(uid);
			}
			
			rs.close();
			stmt.close();
			conn.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return _lstUser;
	}
	
	
	/**
	 * 从article_topic表中查找参与推荐的article
	 * @param artTopTab  article_topic表
	 * @return
	 */
	public List<ArticleTopic> getLstAT(String artTopTab){
		List<ArticleTopic> _lstAT = new ArrayList<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sqlQuery = "select * from cctv_cms.a_article_topic where recommendOrNot = 1 ;";
		try {
			conn = DbConnections.getContentDataConnection();
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sqlQuery);
			while(rs.next())
			{
				ArticleTopic artTop = new ArticleTopic();
				artTop.setArticle_id(rs.getString("article_id"));
				artTop.setChannel(rs.getString("channel"));
				artTop.setTopic1(rs.getString("topic1"));
				artTop.setSim_topic1(rs.getDouble("sim_topic1"));
				artTop.setTopic2(rs.getString("topic2"));
				artTop.setSim_topic2(rs.getDouble("sim_topic2"));
				artTop.setAddtime(rs.getDate("addtime"));
				artTop.setRecommendOrNot(rs.getShort("recommendOrNot"));
				_lstAT.add(artTop);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return _lstAT;
	}
	
	/**
	 * 对于新更新的文章计算用户对文章的喜好度
	 */
	public void cmptUserLikeArticle() {
		
		//得到全部用户
		List<String> _lstUser = new ArrayList<String>();
		_lstUser = getLstUser("a_userinfo");
		
		//得到全部参与推荐的文章
		List<ArticleTopic> _lstAT = new ArrayList<ArticleTopic>();
		_lstAT=getLstAT("a_article_topic");
//		System.out.println("参与推荐的文章个数："+_lstAT.size());
		
		//
		for (int i = 0; i < _lstUser.size(); i++)// 对每个用户
		{
			List<RecommendReady> _lstRecReady = new ArrayList<RecommendReady>();
			String sUserId = _lstUser.get(i);
			for (int j = 0; j < _lstAT.size(); j++)// 对每个更新新闻
			{
				double dRecMeasure = 0.0;
				String sRecTopic = "";
				ArticleTopic _at = _lstAT.get(j);
				String sChannel = _at.getChannel();
				String sTopic1 = _at.getTopic1();
				String sTopic2 = _at.getTopic2();
				double fSimtopic1 = _at.getSim_topic1();
				double fSimtopic2 = _at.getSim_topic2();
				
				Map<String, Double> _mapUserTopic = mapUserLikeTopic.get(sUserId);
//				System.out.println(mapUserLikeTopic.size());
//				System.out.println("sssss"+_mapUserTopic.keySet());

				if(_mapUserTopic == null)
				{
					continue;
				}
				if (_mapUserTopic.containsKey(sTopic1)) {
					sRecTopic = sTopic1;
					dRecMeasure = fSimtopic1 * _mapUserTopic.get(sTopic1);
				}
				
				if (_mapUserTopic.containsKey(sTopic2)) {
					double dTemp = fSimtopic2 * _mapUserTopic.get(sTopic2);
					if (dTemp > dRecMeasure) {
						sRecTopic = sTopic2;
						dRecMeasure = dTemp;
					}
				}
				
				if (!sRecTopic.equals("")) {
					RecommendReady _recReady = new RecommendReady();
					_recReady.setArticleId(_at.getArticle_id());
					_recReady.setArticleChannel(_at.getChannel());
					_recReady.setArticleTopic(sRecTopic);
					_recReady.setRecFlag(1);
					_recReady.setRecMearure(dRecMeasure);
					_recReady.setUserId(sUserId);
					_lstRecReady.add(_recReady);
				}
			}
			if (_lstRecReady.size() > 0) {
				// 批量插入数据库
				insRecommenPreparTab(_lstRecReady);
			}
		}
	}

	/**
	 * 将得到的推荐准备数据插入到推荐准备表中
	 * @param _lstRecReady  推荐准备数据
	 */
	public void insRecommenPreparTab(List<RecommendReady> _lstRecReady){
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		String insSql = "insert into a_recommend_ready(articleId,userId,articleChannel,recMearure,recFlag,addDate) values(?,?,?,?,?,?);";
		try {
			conn = DbConnections.getContentDataConnection();
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(insSql);
			
			for(RecommendReady recReady:_lstRecReady)
			{
				pstmt.setString(1, recReady.getArticleId());
				pstmt.setString(2, recReady.getUserId());
				pstmt.setString(3, recReady.getArticleChannel());
				pstmt.setDouble(4, recReady.getRecMearure());
				pstmt.setInt(5, recReady.getRecFlag());
				pstmt.setDate(6, recReady.getAddDate());				
				pstmt.addBatch();
			}
			
			pstmt.executeBatch();
			conn.commit();
			
			pstmt.close();
			conn.close();
						
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void cmptRecommend(String userid) {

		LinkedList<RecommendReady> _lstRec = new LinkedList<RecommendReady>();
		Map<String, List<RecommendReady>> _mapChannelRec = new HashMap<String, List<RecommendReady>>();
		// _mapChannelRec 从两个地方获取，已经推荐过的数据；新加入的文档未计算推荐的数据
		Map<String, Double> _mapChannelLike = mapUserLikeChannel.get(userid);
		
        //这里将map.entrySet()转换成list
        List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(_mapChannelLike.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
            //降序排序
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }            
        });
        
		for (Map.Entry<String, Double> entry : list) {
			// 得到所属channel所有待推荐数据
			List<RecommendReady> _lstRecReady = new ArrayList<RecommendReady>();
			String articleChannel = entry.getKey();
			_lstRecReady = loadRecomReadyList(userid,articleChannel);
			
			// 排序
			MyComparator mc = new MyComparator();
			Collections.sort(_lstRecReady, mc);
		
			_mapChannelRec.put(articleChannel, _lstRecReady);
		}

		for (int i = 0; i < 10; i++) {
			
			int countOnePaper = 0;
			//推荐10页，每页10条
			 //按照channel喜好取10条数据，喜好值大的多取，需要处理量不够的问题，即channel下待取数据
			  //小于需要提取的数据量
			double dTotallike = 0.0;
			for (Map.Entry<String, Double> entry : list) {
				dTotallike = dTotallike + entry.getValue();
                
			}
			if(dTotallike<0.00001)
			{
				break;
			}
			int iRecNextNum = 0;
			for (Map.Entry<String, Double> entry : list) {
				String sChannel = entry.getKey();
				double dChannelLike = entry.getValue();
				double dRatio = dChannelLike/dTotallike;
				int iRecNum = (int)Math.round(10*dRatio);
				if(iRecNextNum>0)
				{
					iRecNum = iRecNum + iRecNextNum;
				}
				List<RecommendReady> _lstRecReady = _mapChannelRec.get(sChannel);
				
				if(_lstRecReady.size()>iRecNum)
				{
					for(int j=0;j<iRecNum;j++)
					{
						_lstRec.add(_lstRecReady.get(j));
					}
					countOnePaper += iRecNum;
					_mapChannelRec.put(sChannel, _lstRecReady.subList(iRecNum, _lstRecReady.size()));
				}
				else if(_lstRecReady.size( ) == iRecNum)
				{
					_lstRec.addAll(_lstRecReady);
					_mapChannelLike.remove(sChannel);
					countOnePaper += iRecNum;
				}
				else
				{
					_lstRec.addAll(_lstRecReady);
					_mapChannelLike.remove(sChannel);
					iRecNextNum = iRecNum - _lstRecReady.size();
					countOnePaper += _lstRecReady.size();
				}
			}
			if(countOnePaper <10)
			{
				int subNum = 10-countOnePaper;
				List<RecommendReady> subNumRecRea =  new LinkedList<RecommendReady>(); 
				subNumRecRea = _lstRec.subList(0, subNum);
				_lstRec.addAll(subNumRecRea);
			}
			if(countOnePaper > 10)
			{
				int subNum = countOnePaper -10;
				for(int k = 0;k < subNum; k++)
				{
					_lstRec.removeLast();
				}
			}
			
		}	
		insRecomToTable(userid,_lstRec);
	}

	
	public void insRecomToTable(String userId,List<RecommendReady> _lstRec){
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		String sqlIns = "insert into cctv_cms.a_user_recommend(userId,recomArticleId,channelOfArticle,recMeasure) values(?,?,?,?)";
		
		try {
			conn =DbConnections.getContentDataConnection();
			pstmt = conn.prepareStatement(sqlIns);
			for(RecommendReady  recomArt : _lstRec)
			{
//				System.out.println(recomArt.getArticleChannel()+":"+recomArt.getRecMearure());
				pstmt.setString(1, userId);
				pstmt.setString(2, recomArt.getArticleId());
				pstmt.setString(3, recomArt.getArticleChannel());
				pstmt.setDouble(4, recomArt.getRecMearure());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			
			pstmt.close();
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 
	 * @param userid
	 * @param articleChannel
	 * @return
	 */
	public List<RecommendReady> loadRecomReadyList(String userid, String articleChannel){
		
		List<RecommendReady> _lstRecReady = new ArrayList<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sqlQuery ="select * from a_recommend_ready "
				+ "where userId = "+userid+" "
						+ "and articleChannel = '"+articleChannel+"' ;";
		try {
			conn = DbConnections.getContentDataConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);
			while(rs.next()){
				RecommendReady _recReady = new RecommendReady();
				_recReady.setArticleId(rs.getString("articleId"));
				_recReady.setArticleChannel(rs.getString("articleChannel"));
				_recReady.setRecFlag(rs.getInt("recFlag"));
				_recReady.setRecMearure(rs.getDouble("recMearure"));
				_recReady.setUserId(rs.getString("userId"));
				
				_lstRecReady.add(_recReady);
							
			}
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _lstRecReady;
	}
	
	
	class MyComparator implements Comparator {
	
		public int compare(Object o1, Object o2) {
			RecommendReady e1 = (RecommendReady) o1;
			RecommendReady e2 = (RecommendReady) o2;
			if (e1.getRecMearure()  <= e2.getRecMearure())
				return 1;
			else
				return -1;
		}
	}
	
	public static void main(String[] args){
		
		String userID = "863387032559210";
		UserRecommend userRec = new UserRecommend();
//		userRec.cmptUserLikeArticle();
		userRec.cmptRecommend(userID);
	}
}
