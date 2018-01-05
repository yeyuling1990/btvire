package com.ctvit.train

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.DriverManager
import org.apache.spark.rdd.JdbcRDD
import com.ctvit.config.Config
import java.sql.Connection
import java.sql.ResultSet
import com.ctvit.db.MysqlConn

object UserTrainResult {
  val conn = MysqlConn.connMySQL()
  def train() {
    val conf = new SparkConf()
    conf.setAppName("MyFirstSparkApplication") //设置应用程序的名称，在程序运行的监控界面可以看到名称
    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    var channelCount: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()
    var userCount: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()

    val sc = new SparkContext(conf)
    val topiccountdata = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid,tchannel,topicid,count from a_user_favorite where  id>? and id<?", 1, 2000000, 1, getUserFavorite)
    val userCountRdd = topiccountdata.map(tup => (tup._1, tup._4)).reduceByKey(_ + _, 3)
    userCountRdd.collect().foreach(tup => userCount.put(tup._1, tup._2))

    val userChannelRdd = topiccountdata.map(tup => (tup._1 + "$" + tup._2, tup._4))
    val userChannelCountRdd = userChannelRdd.reduceByKey(_ + _, 3)
    userChannelCountRdd.sortBy(tup => tup._2 * (-1)).collect().foreach(tup => println(tup._1 + ":" + tup._2))
    userChannelCountRdd.collect().foreach(tup => channelCount.put(tup._1, tup._2))
    userChannelCountRdd.sortBy(tup => tup._1).collect().foreach(tup => { var userid = tup._1.split('$')(0); var channel = tup._1.split('$')(1); insertChannel(userid, channel, tup._2, userCount.get(userid).getOrElse(0)) })
    topiccountdata.sortBy(tup => tup._1).collect().foreach(tup => { insertTopic(tup._1, tup._2, tup._3, tup._4, channelCount.get(tup._1 + "$" + tup._2).getOrElse(0)) })
    if (!conn.isClosed()) {
      conn.close()
    }
  }
  def insertChannel(userId: String, channel: String, count: Int, userCount: Int): Unit = {
    if (userCount != 0) {
      var dCount: Float = count
      var dUserCount: Float = userCount
      var dWeight: Double = dCount / dUserCount
      var sWeight: String = f"$dWeight%1.3f"
      val str = s"insert into a_user_favorite_channel(userid,channel,weights) values ('$userId','$channel','$sWeight') ;"
      println(str)
      conn.createStatement().execute(str)
    }
  }
  def insertTopic(userId: String, channel: String, topic: String, count: Int, channelCount: Int): Unit = {

    if (channelCount != 0) {
      var dCount: Float = count
      var dUserCount: Float = channelCount
      var dWeight: Double = dCount / dUserCount
      var sWeight: String = f"$dWeight%1.3f"
      val str = s"insert into a_user_favorite_topic(userid,channel,topicid,weights) values ('$userId','$channel','$topic','$sWeight') ;"
      println(str)
      conn.createStatement().execute(str)
    }
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