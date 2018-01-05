package com.ctvit.train
import com.ctvit.db.MysqlConn
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.ResultSet
import org.apache.spark.rdd.JdbcRDD


object TrainNrtity {
  
    val conn = MysqlConn.connMySQL()
    println("连接数据库成功！")

    val conf = new SparkConf()
    conf.setAppName("entityTrain") //设置应用程序的名称，在程序运行的监控界面可以看到名称
    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
  
    
  /**
 	* 训练 
 	*/
  def train() {
      println("读取用户浏览记录")
    val logRawRDD = sc.textFile("hdfs://localhost:9000/userlog/device_id_hot50_dataSet.csv")
      .map { line => val fields = line.split(","); (fields(0).replace("\"", ""), fields(2).replace("\"","")) }
      .filter(tuple => tuple._1 == "353456789159784")
      .filter(tuple => tuple._2.contains("ARTI"))
      .groupByKey()
      .filter(tuple => tuple._2.size > 0)
//      .foreach(tuple => println(tuple._1+":"+tuple._2))
      .foreach(tuple => iterUserEntity(tuple._1, tuple._2))
      println("读取用户浏览记录完成")
  }
  
  /**
   * 根据每一个用户的浏览记录，得到排名靠前的实体，放到sql数据库中
 * @param userId 用户id	
 * @param articleList 用户浏览日志的文章号
 */
def iterUserEntity(userId: String, articleList: Iterable[String]){
    println("读取用户article的实体")  
//    var sSql = " "
    println("---------------------")
//    articleList.foreach(tuple =>println(tuple))
//    articleList.foreach(t => sSql = sSql + "'" + t + "',")
    var sSql = articleList.mkString(",").replace(",", "','")
    sSql = "'"+sSql+"'"
    
    println("+++++文章名")  
//    if (sSql.length() > 1)
//    {
//      sSql = sSql.substring(0, sSql.length() - 1)
//    }
    println("select nrEntity from a_article_topic where article_id in (" + sSql + ") and id>? and id<?")
    val topiccountdata = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid,tchannel,topicid,count from a_user_favorite1 where  id>? and id<?", 1, 2000000, 1, getUserFavorite)
        .foreach(tuple => println(tuple._1))
    //    val dataRDD = new JdbcRDD(sc, MysqlConn.connMySQL, "select nrEntity from a_article_topic where article_id in (" + sSql + ") and id>? and id<?", 1, 2000000, 1, getArticleNrentity).foreach(tuple =>println(tuple.toString()))
//         .filter(line => line != "")
//         .flatMap(line => line.split(","))
//         .map(word => (word, 1))
//         .reduceByKey(_+_)
//         .sortBy(_._2,false)
//         .take(20)
//         .foreach(tuple => insterEntityOfUsers(userId, tuple))
    //    println("&&&&&&&&&&&&&&&&&&&&&&&&&&&")
//    if(dataRDD == null)
//       {println("*******************")}
//    dataRDD.foreach(println)
    
    
    //     .filter(line => line != "").foreach(println)
//     .flatMap(line => line.split(","))
//     .map(word => (word, 1))
//     .reduceByKey(_+_)
//     .sortBy(_._2,false)
//     .take(20)
//     .foreach(tuple => insterEntityOfUsers(userId, tuple))
  }

  def insterEntityOfUsers(userId: String, tuple: (String, Int) ): Unit ={
   println("插入用户实体")
    var nrentity = tuple._1
    var count = tuple._2
    val str = s"insert into a_user_favorite_nrentity(userid,nrentity,count) values ('$userId','$nrentity','$count') ;"; 
    conn.createStatement().execute(str)
  }


  /**
   * jdbcRDD中使用，返回实体每个文章的entity
 * @param r
 * @return
 */
  def getArticleNrentity(r: ResultSet) = {
    (r.getString(1))
  }
  
    def getUserFavorite(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getInt(4))
  }
  
  def main(args: Array[String]): Unit = {
    train()
  }
  
}