package com.ctvit.train

import java.sql.DriverManager
import com.ctvit.config.Config
import java.sql.Connection
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.JdbcRDD
import java.sql.ResultSet
import com.ctvit.db.MysqlConn

object UserTrain {

  val conn = MysqlConn.connMySQL()
  val conf = new SparkConf()
  conf.setAppName("MyFirstSparkApplication") //设置应用程序的名称，在程序运行的监控界面可以看到名称
  conf.setMaster("local")
  conf.set("spark.executor.memory", "2g")
  val sc = new SparkContext(conf)
  var topicCount: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()
  //topicCount.foreach(tup=>println(tup._1+ ":" + tup._2))

  var topicCountInner: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()

  def train() {

    //获得历史的用户喜好，然后读取记录进行增量计算。
    val topiccountdata = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid,tchannel,topicid,count from a_user_favorite where  id>? and id<?", 1, 2000000, 1, getUserFavorite)
      .collect().foreach(tup => (topicCount.put(tup._1 + "$" + tup._2 + "$" + tup._3, tup._4)))
    //    topicCount.foreach(tup => println(tup._1 + ":" + tup._2))
    println("获得历史的用户喜好")
    //增量计算
    //本地文件地址：hdfs://localhost:9000/userlog/device_id_hot50_dataSet.csv
    //公司文件地址：/tmp/zzl/userlog/device_id_hot50_dataSet.csv
    val logRawRDD = sc.textFile("hdfs://localhost:9000/userlog/deviceid_articleid_two_people_testData.csv")
      .map { line => val fields = line.split(","); (fields(0).replace("\"", ""), fields(2).replace("\"", "")) }
      .filter(tuple => tuple._1 != "common_device_id")
      .filter(tuple => tuple._2.contains("ARTI"))
    println("开始处理日志:" + logRawRDD.count())
    val userLogRdd = logRawRDD.groupByKey().collect().take(100).foreach(tuple => iterUser(tuple._1, tuple._2))
    //    .foreach(tuple => println(tuple._1+":"+tuple._2.size))

    if (!conn.isClosed()) {
      println("完成处理")
      conn.close()
    }
  }

  def iterUser(userId: String, articleList: Iterable[String]) = {
    //    println(userId + ":")
    //    println(articleList)
    var sSql = articleList.mkString(",").replace(",", "','")
    sSql = "'" + sSql + "'"
    //println("select  channel,topic1,topic2 from a_article_topic where article_id in (" + sSql  + ") and  id>1 and id<200000")
    println(sSql)
    val non_series_data = new JdbcRDD(sc, MysqlConn.connMySQL, "select channel,topic1,topic2 from a_article_topic where article_id in (" + sSql + ") and  id>? and id<?", 1, 2000000, 2, getArticleTopic)
      .map(tup => (tup._1, (tup._2, tup._3)))
      .groupByKey()
      .foreach(tup => computeTopic(tup._1, tup._2, userId))
    //println(sSql)
  }

  def computeTopic(channel: String, topics: Iterable[(String, String)], userId: String) = {
    println("开始计算用户喜欢的topic")

    for (elem <- topics) {

      var temp = topicCount.get(userId + "$" + channel + "$" + elem._1).getOrElse(0)
      if (temp != 0) {
        topicCountInner.put(elem._1, temp)
      }
      temp = topicCount.get(userId + "$" + channel + "$" + elem._2).getOrElse(0)
      if (temp != 0) {
        topicCountInner.put(elem._2, temp)
      }

    }
    for (elem <- topics) {

      var temp = topicCountInner.get(elem._1).getOrElse(0)
      temp = temp + 1
      topicCountInner.put(elem._1, temp)
      temp = topicCountInner.get(elem._2).getOrElse(0)
      temp = temp + 1
      topicCountInner.put(elem._2, temp)

    }
    topicCountInner.foreach(tup => insertMySQL(userId, channel, tup._1, tup._2))
    println("***********************14")

  }

  def insertMySQL(userid: String, tchannel: String, topicid: String, count: Int): Unit = {
    val str = s"insert into a_user_favorite(userid,tchannel,topicid,count) values ('$userid','$tchannel','$topicid','$count') ;"
    println(str)
    conn.createStatement().execute(str)
  }
  /**
   * a_article_topic
   * channel,topic1,topic2
   */
  def getArticleTopic(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3))
  }
  /**
   * a_user_favorite
   * userid,tchannel,topicid,count
   */
  def getUserFavorite(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getInt(4))
  }

  // access the private class field 'secret'
  def main(args: Array[String]): Unit = {
    train()
    //UserTrainResult.train()
  }
}