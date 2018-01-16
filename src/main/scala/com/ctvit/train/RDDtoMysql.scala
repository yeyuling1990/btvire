package com.ctvit.train
 
import java.sql.PreparedStatement
import org.apache.spark.{SparkContext, SparkConf}
import com.ctvit.db.MysqlConn

object RDDtoMysql{
 
  case class Blog(name: String, count: Int)
 
  def myFun(iterator: Iterator[(String, Int)]): Unit = {
    println("++++++++++")
    var conn = MysqlConn.connMySQL()
    println("----------")
//    var ps = conn.prepareStatement(sql)
    println("*************")
    iterator.foreach(data => {
        val sql = "insert into blog(name, count) values (?, ?)"
        conn.createStatement().execute(sql)
        }
    )
      
//      if (ps != null) {
//        ps.close()
//      }
      if (conn != null) {
        conn.close()
      }
    }

 
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("RDDToMysql")
    val sc = new SparkContext(conf)
    val data = sc.parallelize(List(("www", 10), ("iteblog", 20), ("com", 30)))
    data.foreach(tuple=>println(tuple._1+":"+tuple._2))
    data.foreachPartition(myFun)
  }
} 