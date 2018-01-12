package com.ctvit.train
import com.ctvit.db.MysqlConn
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.ResultSet
import org.apache.spark.rdd.JdbcRDD
import akka.dispatch.Filter


object NrentityTrain2 {
  val conn = MysqlConn.connMySQL()
  val conf = new SparkConf()
  conf.setAppName("entityTrain")
//  conf.setMaster("local")
//  conf.setMaster("spark://192.168.168.41:7077")
  conf.set("spark.executor.memory", "2g")
  val sc = new SparkContext(conf)
  println("读取用户浏览记录")

  def train(){
    
  val nrentityDataRDD = new JdbcRDD(sc,MysqlConn.connMySQL,"select artilce_id,nrEntity from a_article_topic where id > ? and id < ?",1,2000000,1,getArticleNrentity)
      .filter(line => line._2 != "")

    //公司集群地址： /tmp/zzl/userlog/device_id_hot50_dataSet.csv
    //本地地址 ： hdfs://localhost:9000/home/zhang/userlog/device_id_hot50_dataSet.csv
    val logRawRDD = sc.textFile("/tmp/zzl/userlog/device_id_hot50_dataSet.csv")
    .map { line => val fields = line.split(","); (fields(0).replace("\"", ""), fields(2).replace("\"",""))}
    .map(tuple => (tuple._2,tuple._1))
    .join(nrentityDataRDD)
    .map(tuple => (tuple._2._1,tuple._2._2)).foreach(tuple =>println(tuple._1+":"+tuple._2))
//    .filter(tuple => tuple._2.contains("ARTI"))
//    .groupByKey()
//    .filter(tuple => tuple._2.size > 0)
//    .collect()
//    .foreach(tuple => iterUserEntity(tuple._1, tuple._2))
  
//    logRawRDD.join(nrentityDataRDD)
//    logRawRDD.map{line => var userid = line._1;
//                          var articleList = line._2;
//                          var nrentity = }
//    nrentityDataRDD.filter(line => )
    println("读取用户浏览记录完成")
  }
  
  def iterUserEntity(userId: String, articleList: Iterable[String]){
    println("读取用户article的实体")
    var sSql = articleList.mkString(",").replace(",", "','")
    sSql = "'"+sSql+"'"
    println(sSql)
//    nrentityDataRDD.filter(line => articleList.contains(line._1))
//    .flatMap(line => line._2.split(","))
//    .map(word => (word, 1))
//    .reduceByKey(_+_)
//    .sortBy(_._2,false)
//    .take(20)
//    .foreach(tuple => insterEntityOfUsers(userId, tuple))  
  }

  def getArticleNrentity(r: ResultSet) = {
    (r.getString(1),r.getString(2))
  }
  
  def insterEntityOfUsers(userId: String, tuple: (String, Int) ){
   println("插入用户实体")
    var nrentity = tuple._1
    var count = tuple._2
    val str = s"insert into a_user_favorite_nrentity(userid,nrentity,count) values ('$userId','$nrentity','$count') ;"; 
   println(str) 
   conn.createStatement().execute(str)
   println("插入成功！")
  }
  
  def main(args: Array[String]): Unit = {
    train()
  }  
}