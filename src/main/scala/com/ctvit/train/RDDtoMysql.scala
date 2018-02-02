package com.ctvit.train

import com.ctvit.db.MysqlConn
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.ResultSet
import org.apache.spark.rdd.JdbcRDD

import org.apache.spark.{ SparkContext, SparkConf }
import java.text.Normalizer.Form

object RDDtoMysql {

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("MyFirstSparkApplication") //设置应用程序的名称，在程序运行的监控界面可以看到名称
    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
    
    //公司集群地址： /tmp/zzl/userlog/device_id_hot50_dataSet.csv
    //本地地址 ： hdfs://localhost:9000/home/zhang/userlog/device_id_hot50_dataSet.csv
    
    val logRawRDD = sc.textFile("hdfs://localhost:9000/userlog/device_id_hot50_dataSet.csv")
      .map { line => val fields = line.split(","); (fields(0).replace("\"", ""), fields(2).replace("\"", "")) }
      .filter(tuple => tuple._2.contains("ARTI"))
      .groupByKey()
      .map(tup => (tup._2))
  }
  
 
} 