package com.ctvit.train

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.DriverManager
import org.apache.spark.rdd.JdbcRDD
import com.ctvit.config.Config
import java.sql.Connection
import java.sql.ResultSet
import com.ctvit.db.MysqlConn
import java.lang.Double

object UserTrainResult2 {

  def train() {

    val conf = new SparkConf()
    conf.setAppName("UserTrainResult") //设置应用程序的名称，在程序运行的监控界面可以看到名称
    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
    println("----------------------")

    //得到每个用户的favorite表
    val useridTopicCountRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select userid,tchannel,topicid,count from a_user_favorite where id>? and id<?", 1, 2000000, 1, getUserFavorite)

    //得到每个用户看的总频道数，将被用来做除数,结果是(userid,全部的浏览个数)
    val userAllChannelCountRdd = useridTopicCountRdd.map(tup => (tup._1, tup._4))
      .reduceByKey(_ + _, 3)
    //.foreach(println)
    
    //得到每个用户每个频道的观看数(userid,(tchannel,count))
    val userEachChannelCountRdd = useridTopicCountRdd.map(tup => (tup._1 + "$" + tup._2, tup._4))
      .reduceByKey(_ + _, 3)
      .map(tup => (tup._1.split('$')(0), tup._1.split('$')(1), tup._2))
      .map(tuple => (tuple._1, (tuple._2, tuple._3)))
    //.foreach(println)

   //得到每个用户的频道所占的比率，结果为插入sql中，(userid,tchannel,weights)   
    val userEachChannelRateRdd = userAllChannelCountRdd.cogroup(userEachChannelCountRdd)
      .map(tup => (tup._1, computRate(tup._2)))
      .foreach(tuple => iterSqlUserChannelRate(tuple._1, tuple._2))

    println("**********************")

    //得到每个用户每个频道的观看数(userid$tchannel,count)
    val countOfUserAndChannelRdd =useridTopicCountRdd.map(tup => (tup._1 + "$" + tup._2, tup._4))
      .reduceByKey(_ + _, 3)
//      .foreach(println)
    
    
      //得到每个用户每个频道中的topic个数，rdd结果为(userid$tchannel,(topic,count))
    val countOfUserAndChannelTopicRdd = useridTopicCountRdd.map(tup => (tup._1 + "$" + tup._2,(tup._3,tup._4)))
//        .foreach(println)                                                   
     
    
    val topicWeightOfEachChannelRdd = countOfUserAndChannelRdd.cogroup(countOfUserAndChannelTopicRdd)
                                                              .map(tup => (tup._1,computRate(tup._2)))
                                                              .foreach(tup => iterSqlUserChannelTopicRate(tup._1,tup._2))
  }

  def computRate(data: (Iterable[Int], Iterable[(String, Int)])): Iterable[(String, Double)] = {
    var allCount = Double.valueOf(data._1.mkString(""))
    //    println(allCount)
    val channelRate = data._2.map(tup => (tup._1, Double.valueOf(tup._2 / allCount)))
    channelRate
  }

  def iterSqlUserChannelRate(userid: String, channelRate: Iterable[(String, Double)]) = {
    val conn = MysqlConn.connMySQL()
    channelRate.foreach(tuple => insertUserFavChannel(conn, userid, tuple._1, tuple._2))
    if (!conn.isClosed()) {
      println("insert finish!")
      conn.close()
    }
  }
  def iterSqlUserChannelTopicRate(userid: String, channelRate: Iterable[(String, Double)]) = {
    val conn = MysqlConn.connMySQL()
    channelRate.foreach(tuple => insertUserFavChannelTopic(conn, userid, tuple._1, tuple._2))
    if (!conn.isClosed()) {
      println("insert finish!")
      conn.close()
    }
  }
  
  def insertUserFavChannel(conn: Connection, userId: String, channel: String, weight: Double): Unit = {
    val str = s"insert into a_user_favorite_channel(userid,channel,weights) values ('$userId','$channel','$weight') ;"
    println(str)
    conn.createStatement().execute(str)
  }

  def insertUserFavChannelTopic(conn: Connection, userIdChannel: String, topicid: String, weight: Double): Unit = {
   var userId = userIdChannel.split('$')(0)
   var channel = userIdChannel.split('$')(1)
    val str = s"insert into a_user_favorite_topic(userid,channel,topicid,weights) values ('$userId','$channel','$topicid','$weight') ;"
    println(str)
    conn.createStatement().execute(str)
  }
  
  /**
   * a_user_favorite
   * userid,tchannel,topicid,count
   */
  def getUserFavorite(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getInt(4))
  }
  def main(args: Array[String]): Unit = {
    train()
  }
}