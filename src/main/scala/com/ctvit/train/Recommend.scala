package com.ctvit.train

import java.sql.DriverManager
import com.ctvit.config.Config
import java.sql.Connection
import org.apache.spark.rdd.JdbcRDD
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.ResultSet
import scala.collection.mutable.ListBuffer
import com.ctvit.db.MysqlConn
import scala.collection.SortedMap.Default

object Recommend {
  def train() = {
    val conf = new SparkConf()
    conf.setAppName("MyFirstSparkApplication") //设置应用程序的名称，在程序运行的监控界面可以看到名称
    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
    var userFChannelRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid,channel,weights from a_user_favorite_channel where  id>? and id<?", 1, 2000000, 5, getFChannel)
    var userFTopicRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid,channel,topicid,weights from a_user_favorite_topic where  id>? and id<?", 1, 2000000, 5, getFTopic)
    var userRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid from a_userinfo where  id>? and id<?", 1, 2000000, 5, getUser)
    var articleRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "SELECT article_id,channel,topic1,sim_topic1,topic2,sim_topic2,nrentity FROM a_article_topic where  id>? and id<?", 1, 2000000, 5, getArticle)
    var userFEntityRdd = new JdbcRDD(sc,MysqlConn.connMySQL,"SELECT userid,nrentity from a_user_favorite_nrentity where id > ? and id < ?",1,200000,5,getEntity).groupByKey()
    //文章list
    var articleList: ListBuffer[(String, String, String, Double, String, Double,String)] = ListBuffer[(String, String, String, Double, String, Double,String)]()
    articleRdd.collect().foreach { case( articleId, channel, topic1, simtopic1, topic2, simtopic2, nrentity ) => { articleList.+=((articleId, channel, topic1, simtopic1, topic2, simtopic2,nrentity)) } }
    //用户喜好的channel，key:userid,值：map（key：channel,值：权重）
    var userFChannelMap: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]] = scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]]()
    userFChannelRdd.collect().foreach { case (userid, channel, weights) => putFMap(userFChannelMap, userid, channel, weights) }
    //用户喜好的topic，key:userid,值：map（key：channel,值：权重）
    var userFTopicMap: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]] = scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]]()
    userFTopicRdd.collect().foreach { case (userid, channel, topicid, weights) => putFMap(userFTopicMap, userid, topicid, weights) }
    //用户喜好的实体，entity。key:userid。值：List<string> 实体list。
    var userFEntityMap: scala.collection.mutable.Map[String,List[String]] = scala.collection.mutable.Map[String,List[String]]()
    userFEntityRdd.collect().foreach{case (userid,entitys) => userFEntityMap.put(userid, entitys.toList)}
        
    userRdd.collect().foreach(userid => iterUser(userid, articleList, userFChannelMap, userFTopicMap,userFEntityMap))
  }
  
  //根据用户id，待推荐的文章，用户喜欢的频道数据，用户喜欢的topic数据，用户喜欢的实体数据，先进行实体推荐，然后根据频道和话题数据进行推荐。
  def iterUser(userid: String, articleList: ListBuffer[(String, String, String, Double, String, Double,String)],
    userFChannelMap: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]],
    userFTopicMap: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]],
    userFEntityMap: scala.collection.mutable.Map[String,List[(String)]]) =
    {
      println("推荐前推荐列表长度："+articleList.length)
      //推荐列表，包括articleid，channel，measure
      var recommendList: ListBuffer[(String, String, Double)] = ListBuffer[(String, String, Double)]()
      //实体推荐
      iterArtEntityRecom(articleList,userid,userFEntityMap,recommendList)
       
      //频道、话题推荐
      //article_id,channel,topic,weight
      var articleMessureList: ListBuffer[(String, String, String, Double)] = ListBuffer[(String, String, String, Double)]()
      //  println("******************")
      //println(articleList.size)
      articleList.filter(tuple => !recommendList.contains(tuple._1)).foreach { case (articleId, channel, topic1, simtopic1, topic2, simtopic2, nrentity) => iterArticle(userid, articleId, channel, topic1, simtopic1, topic2, simtopic2, nrentity,userFTopicMap, articleMessureList) }
      // println("###################")
      var currUserFChannel: scala.collection.mutable.Map[String, Double] = userFChannelMap.get(userid).getOrElse(null)

      if (currUserFChannel != null) {
        var dFChannelTotal: Double = 0.0
        // println("###############################")
        // currUserFChannel.toSeq.sortBy(tup => tup._2*(-1)).take(5).foreach(println)
        // println("###############################")
        currUserFChannel.toSeq.sortBy { case (channel, weight) => weight * (-1) }.take(5).foreach { case (channel, weight) => { dFChannelTotal = dFChannelTotal + weight } }
        // println("****************************")
        //  println(dFChannelTotal)
        //选取5个频道，按照每个频道的权重选择推荐数据
        currUserFChannel.toSeq.sortBy { case (channel, weight) => weight * (-1) }.take(5).foreach {
          case ((channel, messure)) =>
            {
              var numSelectChannel: Int = (Config.recommendNum * messure / dFChannelTotal).intValue()
              //println(numSelectChannel)
              articleMessureList.filter(tup => tup._2.equalsIgnoreCase(channel))
                .sortBy(tup => tup._4 * (-1)).take(numSelectChannel).foreach {
                  case (articleId, articleCchannel, articleTopic, articleMess) => {
                    if (recommendList.size < Config.recommendNum) {
                      recommendList.+=((articleId, articleCchannel, articleMess))
                      articleMessureList.-=((articleId, articleCchannel, articleTopic, articleMess))
                    }
                  }
                }
            }
        }
      }
      println(Config.recommendNum)
      //推荐数量小于400，补充
      if (recommendList.size < Config.recommendNum) {
        var remain: Int = Config.recommendNum - recommendList.size;
        articleMessureList.sortBy(tup => tup._4 * (-1)).take(remain).foreach {
          case (articleId, articleCchannel, articleTopic, articleMess) => {
            if (recommendList.size < Config.recommendNum) {
              recommendList.+=((articleId, articleCchannel, articleMess))
              articleMessureList.-=((articleId, articleCchannel, articleTopic, articleMess))
            }
          }
        }
      }
      println("****************************")
      println(userid)
      recommendList.filter(tuple => tuple._3 > 0.5).foreach(println)
      println("*********************************")
    }
  def putFMap(map: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]], userid: String, channel: String, weights: Double) =
    {
      var userMap = map.get(userid).getOrElse(null)
      if (userMap != null && userMap.size != 0) {
        userMap.put(channel, weights)
        map.put(userid, userMap)
      } else {
        var user2Map: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map[String, Double]()
        user2Map.put(channel, weights)
        map.put(userid, user2Map)
      }
    }
  
  def iterArticle(userid: String, articleId: String, channel: String, topic1: String, simTopic1: Double, topic2: String, simTopic2: Double, nrentity:String, userFTopicMap: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String, Double]], articleMessureList: ListBuffer[(String, String, String, Double)]) =
    {
      //println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
      var curruserFTopicMap: scala.collection.mutable.Map[String, Double] = userFTopicMap.get(userid).getOrElse(null)
      var sTopic: String = ""
      var dMeasure: Double = 0.0
      if (curruserFTopicMap != null) {
        dMeasure = curruserFTopicMap.get(topic1).getOrElse(0.0) * simTopic1
        sTopic = topic1
        var dTemp = curruserFTopicMap.get(topic2).getOrElse(0.0) * simTopic2
        if (dTemp > dMeasure) {
          dMeasure = dTemp
          sTopic = topic2
        }
      }
      //println(articleId + "--" + sTopic + ":" + dMeasure)
      articleMessureList.+=((articleId, channel, sTopic, dMeasure))
    }
  
  
  def iterArtEntityRecom(articleList:ListBuffer[(String, String, String, Double, String, Double,String)],
                         userid:String,
                         userFEntityMap: scala.collection.mutable.Map[String,List[(String)]],
                         recommendList: ListBuffer[(String, String, Double)]) ={
    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    println("articleList的length:"+articleList.length)
    
    var artcileEntityMap:scala.collection.mutable.Map[String, List[String]] = scala.collection.mutable.Map[String, List[String]]()
    if(userFEntityMap.contains(userid))
    {
          var entityList:List[String] = userFEntityMap(userid)
           articleList.foreach{Tuple => var fields = Tuple._7.split(",").toList intersect entityList 
                                 if(fields.length > 0 && recommendList.length < 100)
                                 {
//                                   println(Tuple._7)
//                                   println(entityList)
                                   recommendList .+= ((Tuple._1,"",1.0))
//                                   articleList.-=(Tuple)  
                                 }
                       }
    }

    println("recommendList的length:"+recommendList.length)
    recommendList                       
  }
  
  /**
  def connMySQL(): Connection = {
    println("connect db")
    val MYSQL_DRIVER = "com.mysql.jdbc.Driver"
    Class.forName(MYSQL_DRIVER)
    DriverManager.getConnection(Config.mysql_url, Config.mysql_username, Config.mysql_password)
  }**/
  /**
   * a_user_favorite_channel
   * userid,channel,weights
   */
  def getFChannel(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getDouble(3))
  }
  /**
   * a_user_favorite_topic
   * userid,channel,topicid,weights
   */
  def getFTopic(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getDouble(4))
  }
  /**
   * a_userifno
   * userid
   */
  def getUser(r: ResultSet) = {
    (r.getString(1))
  }
  /**
   * a_article_topic
   * article_id,channel,topic1,sim_topic1,topic2,,sim_topic2
   */
  def getArticle(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getDouble(4), r.getString(5), r.getDouble(6), r.getString(7))
  }
  def getEntity(r:ResultSet) = {
    (r.getString(1),r.getString(2))
  }
  
  def main(args: Array[String]): Unit = {
    train()
  }


}