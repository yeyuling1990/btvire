package com.ctvit.train
import com.ctvit.db.MysqlConn
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.ResultSet
import org.apache.spark.rdd.JdbcRDD
import akka.dispatch.Filter
import java.sql.Connection
import java.text.Normalizer.Form

object NrentityTrain2 {
  def train() {
    val conf = new SparkConf()
    conf.setAppName("entityTrain2")
    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)

    println("read user logs")

    val nrentityDataRDD = new JdbcRDD(sc, MysqlConn.connMySQL, "select article_id,nrEntity from a_article_topic where id > ? and id < ?", 1, 2000000, 1, getArticleNrentity)
      .filter(line => line._2 != "")
    //      .take(100).foreach(tuple =>print(tuple._1+":"+tuple._2))

    println("article and entity finsh")

    //公司集群地址： /tmp/zzl/userlog/device_id_hot50_dataSet.csv
    //本地地址 ： hdfs://localhost:9000/userlog/device_id_hot50_dataSet.csv
    val logRawRDD = sc.textFile("hdfs://localhost:9000/userlog/device_id_hot50_dataSet.csv")
      .map { line => val fields = line.split(","); (fields(0).replace("\"", ""), fields(2).replace("\"", "")) }
      .filter(tuple => tuple._2.contains("ARTI"))
      .map(tuple => (tuple._2, tuple._1))

    logRawRDD.leftOuterJoin(nrentityDataRDD).filter(tup => tup._2._2 != None)
//            .take(100).foreach(println)
      .map(tuple => (tuple._2._1, tuple._2._2))
      .groupByKey()
      .map(tup => (tup._1, tup._2.flatMap(_.toList)))
//      .foreach(println)
      .map(tuple => (tuple._1, getUserNrentity(tuple._2)))
//      .foreach(println)
      .foreach(tuple => insterEntityOfUsers(tuple._1, tuple._2))

    println("read user logs end")
    sc.stop()
  }

  def getUserNrentity(Iterator: Iterable[String]): String = {
    val listNr = Iterator
    .flatMap(line => line.split(","))
    .map((_, 1))
    .groupBy(_._1)
    .mapValues(_.map((_._2))
    .reduce(_ + _)).toSeq.sortWith(_._2 > _._2)
    .take(10)
    .map(tup => tup._1)
    .mkString(",")
    listNr
  }

  def getArticleNrentity(r: ResultSet) = {
    (r.getString(1), r.getString(2))
  }

  def insterEntityOfUsers(userId: String, nrEntityString:String) {
    //    println("插入用户实体")
    val conn = MysqlConn.connMySQL()
    val str = s"update a_userinfo set userEntity ='$nrEntityString' where userid ='$userId';";
    conn.createStatement().executeUpdate(str)
    if (!conn.isClosed()) {
      println("close connection!")
      conn.close()
    }
  }

  def main(args: Array[String]): Unit = {
    train()
  }
}