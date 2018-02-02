package com.ctvit.train

import java.sql.DriverManager
import com.ctvit.config.Config
import java.sql.Connection
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.JdbcRDD
import java.sql.ResultSet
import com.ctvit.db.MysqlConn

object UserTrain2 {

  def train() {
    val conf = new SparkConf()
    conf.setAppName("UserTrain2") //设置应用程序的名称，在程序运行的监控界面可以看到名称
        conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)

    //获得历史的用户喜好，然后读取记录进行增量计算。
    val userTopicOldRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid,tchannel,topicid,count from a_user_favorite where  id>? and id<?", 1, 2000000, 1, getUserFavorite)
      .map(tup => (tup._1 + "$" + tup._2 + "$" + tup._3, tup._4))

    //增量计算
    //本地文件地址：hdfs://localhost:9000/userlog/device_id_hot50_dataSet.csv   deviceid_articleid_two_people_testData.csv
    //公司文件地址：/tmp/zzl/userlog/device_id_hot50_dataSet.csv     deviceid_articleid_two_people_testData.csv

    //该rdd读取用户的历史浏览记录，并过滤转换成(artcile_id,userid)的tuple。
    val userLogRdd = sc.textFile("hdfs://localhost:9000/userlog/device_id_hot50_dataSet.csv")
      .map { line => val fields = line.split(","); (fields(0).replace("\"", ""), fields(2).replace("\"", "")) }
      .filter(tuple => tuple._1 != "common_device_id")
      .filter(tuple => tuple._2.contains("ARTI"))
      .filter(tuple => tuple._2 != "")
      .map(tuple => (tuple._2, tuple._1))
    println("开始处理日志:" + userLogRdd.count())

    println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")

    val articleTopicRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select article_id,channel,topic1,topic2 from a_article_topic where id>? and id<?", 1, 2000000, 2, getArticleTopic)
      .map(tuple => (tuple._1, (tuple._2, tuple._3, tuple._4)))

    println("++++++++++++++++++++++++++++")
    val userArticleRdd = userLogRdd.leftOuterJoin(articleTopicRdd)
      .map(tuple => (tuple._2._1, tuple._2._2.getOrElse("111", "222", "33")._1, tuple._2._2.getOrElse("111", "222", "333")._2, tuple._2._2.getOrElse("111", "222", "33")._3))
      .filter(tuple => tuple._2 != "111")
      .filter(tuple => tuple._2 != "222")
      .filter(tuple => tuple._2 != "333")

    val userTopicNewRdd1 = userArticleRdd.map(tuple => (tuple._1 + "$" + tuple._2 + "$" + tuple._3, 1)).reduceByKey(_ + _)
    val userTopicNewRdd2 = userArticleRdd.map(tuple => (tuple._1 + "$" + tuple._2 + "$" + tuple._4, 1)).reduceByKey(_ + _)

    println("****************************")

    val userTopicNewAllRdd = userTopicNewRdd1.cogroup(userTopicNewRdd2)
      .map(tuple => (tuple._1, countTuple(tuple._2._1, tuple._2._2)))
    println("----------------------------")

    userTopicOldRdd.cogroup(userTopicNewAllRdd)
      .map(tup => (tup._1, countTuple(tup._2._1, tup._2._2)))
      .foreach(tup => insertMySQL(tup._1, tup._2))
    println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@")

  }

  def countTuple(a1: Iterable[Int], a2: Iterable[Int]): Int = {
    var countAll = 0
    if (a1.mkString("") != "") {
      //      println("+++++"+a1.head.toInt)
      countAll = countAll + a1.head.toInt
    }
    if (a2.mkString("") != "") {
      //      println("-----"+a2.head.toInt)
      countAll = countAll + a2.head.toInt
    }
    countAll
  }

  def insertMySQL(useridChannelTopic: String, count: Int) {
    val conn = MysqlConn.connMySQL()
    var arr = useridChannelTopic.split('$')
    var userid = arr(0)
    var tchannel = arr(1)
    var topicid = arr(2)
    val str = s"insert into a_user_favorite(userid,tchannel,topicid,count) values ('$userid','$tchannel','$topicid','$count') ;"
    //    println(str)
    conn.createStatement().execute(str)
    if (!conn.isClosed()) {
      println("insert finish!")
      conn.close()
    }
  }
  /**
   * a_article_topic
   * channel,topic1,topic2
   */
  def getArticleTopic(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getString(4))
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