package com.ctvit.train

import com.ctvit.db.MysqlConn
import com.ctvit.nlp.userportrait.word2vec.Utils
import org.apache.spark.rdd.JdbcRDD
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import com.ctvit.nlp.similary.TopicSimilar
import com.hankcs.hanlp.HanLP
import com.google.word2vec.VectorModel.WordScore
import java.util.Date
import java.sql.ResultSet
import java.sql.DriverManager
import java.text.SimpleDateFormat
import com.ctvit.config.Config
import java.sql.Connection


object ArticleTrain2{
  //
  
  def train() {
    val conf = new SparkConf()
    conf.setAppName("MyFirstSparkApplication") //设置应用程序的名称，在程序运行的监控界面可以看到名称
//    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
    val filterlst: List[String] = List("cctv1", "cctv10", "cctv12", "cctv14", "cctv15", "cctv3", "cctv4", "cctv7","cctv2")
//    println(filterlst.contains("cctv10"));
    val non_series_data = new JdbcRDD(sc, MysqlConn.connMySQL, "select  mid,ti,tag,content,channel from q_test where  (id>? and id<?)", 1, 20000000, 1, getQtest)
      .filter(tup => tup._5 != null && tup._5 != "")
      .filter(tup => filterlst.contains(tup._5.toLowerCase()))
      //.foreach(tup=>println(tup._5))
      .map(tup => (tup._1, tup._2 + tup._3 + tup._4, tup._5.toLowerCase()))
//      println(non_series_data.count())
      .map(tup => computeTopic(tup._1, tup._2, tup._3))
//      .collect()
      .foreachPartition(myFun)
//      .foreach(tup =>insertMySQL(tup._1, tup._2, tup._3,tup._4, tup._5, tup._6,tup._7))

  }
  def computeTopic(mid: String, content: String, channel: String): scala.Tuple7[String, String, String, String, Float, Float,String] = {
//    println("++++++++")
    var topicSim: TopicSimilar = new TopicSimilar()
//    println("--------")
  //  val filter: List[String] = List("cctv1", "cctv10", "cctv12", "cctv14", "cctv15", "cctv3", "cctv4", "cctv7")
    var topic1Name: String = ""
    var topic2Name: String = ""
    var topic1Score: Float = 0f
    var topic2Score: Float = 0f
    var nrEntity: String = ""
  
    val keyword = HanLP.extractKeyword(content, 100);
//    println("keyword:"+keyword)
    var topicSet = topicSim.computeBestTopic(channel, keyword);
//    println("topicSet:"+topicSet)
    var iter = topicSet.iterator()
    var i = 0

      while (iter.hasNext()) {
        i = i + 1
        if (i == 1) {
          var t = iter.next()
          topic1Name = t.getName
          topic1Score = t.getScore
        } else if (i == 2) {
          var t = iter.next()
          topic2Name = t.getName
          topic2Score = t.getScore
        }
      }
      nrEntity = Utils.cmpNrEntity(content);
//      println("完成实体提取，返回插入数据库的数据")
    (mid, channel, topic1Name, topic2Name, topic1Score, topic2Score,nrEntity)
  }
  
  def myFun(iterator: Iterator[(String,String,String,String, Float,Float,String)]): Unit ={
    val conn = MysqlConn.connMySQL()
    iterator.foreach(data => insertMySQL(conn,data._1,data._2,data._3,data._4,data._5,data._6,data._7))
       if(!conn.isClosed())
      {
        println("关闭数据库！")
        conn.close();
      }
  }
   
    
  def insertMySQL(conn:Connection,mid:String, channel:String, topic1Name:String, topic2Name:String, topic1Score:Float, topic2Score:Float,nrEntity:String): Unit = {

    var now = getNowDate()
    val str = s"insert into a_article_topic (article_id,channel,topic1,sim_topic1,topic2,sim_topic2,nrEntity,recommendOrNot,addtime) values ('$mid','$channel','$topic1Name','$topic1Score','$topic2Name','$topic2Score','$nrEntity',0,'$now') ;"
    println("插入数据完成。")
    conn.createStatement().execute(str)
  }
  
  // access the private class field 'secret'
  def main(args: Array[String]): Unit = {
   train()
  }
  
  
  /**
   * q_test
   * mid,ti,tag,content,channel
   */
  def getQtest(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5))
  }
  def getNowDate(): String = {
    val now: Date = new Date()
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = dateFormat.format(now)
    return date
  }
}