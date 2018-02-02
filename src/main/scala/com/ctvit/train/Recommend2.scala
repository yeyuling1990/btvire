package com.ctvit.train

import java.sql.DriverManager
import com.ctvit.config.Config
import java.sql.Connection
import org.apache.spark.rdd.JdbcRDD
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.sql.ResultSet
import scala.collection.mutable.ListBuffer
import com.ctvit.db.MysqlConn
import scala.collection.SortedMap.Default
import java.io.PrintWriter
import java.io.File

object Recommend2{
  def train() = {

    val conf = new SparkConf()
    conf.setAppName("recommend2") //设置应用程序的名称，在程序运行的监控界面可以看到名称
//    conf.setMaster("local")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
    val userFChannelRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select userid,channel,weights from a_user_favorite_channel where  id>? and id<?", 1, 2000000, 1, getFChannel)
      .map(tup => (tup._1, (tup._2, tup._3)))
      .groupByKey()

    val userFTopicRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "select  userid,channel,topicid,weights from a_user_favorite_topic where  id>? and id<?", 1, 2000000, 1, getFTopic)
      .map(tup => (tup._1, (tup._2, tup._3, tup._4)))
      .groupByKey()

    val userFEntityRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "SELECT userid,userEntity from a_userinfo where id > ? and id < ?", 1, 200000, 1, getEntity)

    val userFavoritRdd = userFEntityRdd.cogroup(userFChannelRdd, userFTopicRdd)
    println(userFavoritRdd.count())

    val articleRdd = new JdbcRDD(sc, MysqlConn.connMySQL, "SELECT article_id,channel,topic1,sim_topic1,topic2,sim_topic2,nrEntity FROM a_article_topic where  id>? and id<? and recommendOrNot = '1'", 1, 2000000, 1, getArticle)
    println(articleRdd.count())

    val cartesianRdd = userFavoritRdd.cartesian(articleRdd)
      .groupByKey()
      .foreach(tup => computeRecommed(tup))
//    println(cartesianRdd.count())
    //    println(cartesianRdd.count())
    //    userFEntityRdd.foreach{
    //      case (userid, userNrentity) =>
    //        {
    //          var recommendList: ListBuffer[(String, String, Double)] = ListBuffer[(String, String, Double)]()
    //          articleRdd.foreach{
    //            case (article_id, channel, topic1, sim_topic1, topic2, sim_topic2, nrEntity) =>
    //              {
    //                println("******************************")
    //                var fields = (userNrentity.split(',').toList) intersect (nrEntity.split(',').toList)
    //                if (fields.length > 0 && recommendList.length < 100) {
    //                  recommendList.+=((article_id, "", 1.0))
    //                }
    //              }
    //          }
    //         recommendList.foreach(tup =>println(userid+":"+tup))
    //        }
    //    }
    sc.stop()
  }

  //
  def computeRecommed(userAndArticle: ((String, (Iterable[String], Iterable[Iterable[(String, Double)]], Iterable[Iterable[(String, String, Double)]])), Iterable[(String, String, String, Double, String, Double, String)])) =
    {

      val artcicleList = userAndArticle._2

      println("articleList:" + artcicleList.size)

      val userId = userAndArticle._1._1

      println("userId:" + userId)

      val userFav = userAndArticle._1._2
      
      println("++++++++++++++++++++++++++++++")
      
      //用户喜欢的实体
      var userFavEntity:String = ""
      if (userFav._1.size > 0)
      {
        userFavEntity = userFav._1.head
        println("用户喜欢的实体:" + userFavEntity)
      }
      //用户喜好的channel，map（key：channel,值：权重）
      var userFChannelMap: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map[String, Double]()

      if (userFav._2.size > 0) {
        //用户喜欢的频道及其权重
        val userFavChannelWeight = userFav._2.head
        println("用户喜欢的频道及其权重:" + userFavChannelWeight.size)
        userFavChannelWeight.foreach(tup => userFChannelMap.put(tup._1, tup._2))
      }

      println("-----------------------")
      
      //用户喜好的topic，key:userid,值：map（key：channel,值：权重）
      var userFTopicMap: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map[String, Double]()

      //用户喜欢的topic及其权重
      if (userFav._3.size > 0) {
        val userFavTopicWeight = userFav._3
        println("用户喜欢的topic及其权重:" + userFavTopicWeight.size)
        userFavTopicWeight.head.foreach(tup => userFTopicMap.put(tup._2, tup._3))
      }

      println("@@@@@@@@@@@@@@@@@")
      
//      var recommendList1: ListBuffer[(String, String, Double)] = ListBuffer[(String, String, Double)]()
//      //计算实体推荐
//      if(userFavEntity != null)
//      {
//        recommendList1 = iterArtEntityRecom(artcicleList, userFavEntity)   
//      }
//
//      println("******************")
//      
//      //计算频道、topic推荐
//      val artcicleListNew = artcicleList.filter(tuple => !recommendList1.contains(tuple._1))
//      
//      println("%%%%%%%%%%%%%%%%%%")
//      
//      var recommendList2:ListBuffer[(String, String, Double)] =ListBuffer[(String, String, Double)]()
//      
//      if(userFChannelMap != null && userFTopicMap != null)
//      {
//       println("$$$$$$$")
//       recommendList2 = iterChannelTopicRec(artcicleListNew,userFChannelMap,userFTopicMap)        
//      }
//      
//      println("&&&&&&&&&&&&&&&&&&&&")
//      
//      val recommedList = recommendList1 ++ recommendList2
//      recommedList.foreach(println)
    }

  //实体推荐程序函数
  def iterArtEntityRecom(
    articleList:Iterable[(String, String, String, Double, String, Double, String)] ,
    userFavEntity: String): ListBuffer[(String, String, Double)] = 
    {
    val recommendList: ListBuffer[(String, String, Double)] = ListBuffer[(String, String, Double)]()

    val entityList = userFavEntity.split(',')
    articleList.foreach { tup =>
      val num = tup._7.split(',') intersect entityList
      if (num.size > 0)
        recommendList.+=((tup._1, "", 1.0))
    }

    recommendList
  }

  
  
  //根据channel、topic进行推荐
  def iterChannelTopicRec(
    artcicleList:    Iterable[(String, String, String, Double, String, Double, String)],
    userFChannelMap: scala.collection.mutable.Map[String, Double],
    userFTopicMap:   scala.collection.mutable.Map[String, Double]):ListBuffer[(String, String, Double)] =
    {
      val recommendList: ListBuffer[(String, String, Double)] = ListBuffer[(String, String, Double)]()
      println("channel个数大小：" + userFChannelMap.size)
      val articleMessureList = iterArticle(artcicleList,userFTopicMap)
      if (userFChannelMap.size > 0) {
        var dFChannelTotal = 0.0

        userFChannelMap.toSeq.sortBy { case (channel, weight) => weight * (-1) }.take(5).foreach { case (channel, weight) => { dFChannelTotal = dFChannelTotal + weight } }
        println("****************************")
        //  println(dFChannelTotal)
        //选取5个频道，按照每个频道的权重选择推荐数据
        userFChannelMap.toSeq.sortBy { case (channel, weight) => weight * (-1) }.take(5).foreach {
          case ((channel, messure)) =>
            {
              var numSelectChannel: Int = (Config.recommendNum * messure / dFChannelTotal).intValue()
              //println(numSelectChannel)
              // println("###############################")
              
              articleMessureList.filter(tup => tup._2.equalsIgnoreCase(channel))
                .sortBy(tup => tup._4 * (-1)).take(numSelectChannel).foreach{
                                      case (articleId, articleCchannel, articleTopic, articleMess) => {
                                        if (recommendList.size < Config.recommendNum) {
                                          recommendList.+=((articleId, articleCchannel, articleMess))
                                          articleMessureList.-=((articleId, articleCchannel, articleTopic, articleMess))
                                        }
                                      }
                }
            }
        }
      }
              println("Config.recommendNum:" + Config.recommendNum)
              //推荐数量小于400，补充
              if (recommendList.size < Config.recommendNum) {
                var remain: Int = Config.recommendNum - recommendList.size;
                articleMessureList.sortBy(tup => tup._4 * (-1)).take(remain).foreach {
                  case (articleId, articleCchannel, articleTopic, articleMess) => {
                    if (recommendList.size < Config.recommendNum) {
                      recommendList.+=((articleId, articleCchannel, articleMess))
                      articleMessureList.-=((articleId, articleCchannel, articleTopic, articleMess))
                    }
                  }
                }
              }
      //        println("****************************")
      //        println(userid)
      //        recommendList.foreach(tuple => println(tuple))
      //        println("*********************************")
    recommendList
    }

  //
  def iterArticle(articleList: Iterable[(String, String, String, Double, String, Double, String)], curruserFTopicMap: scala.collection.mutable.Map[String, Double]): ListBuffer[(String, String, String, Double)] =
    {
      val articleMessureList: ListBuffer[(String, String, String, Double)] = ListBuffer[(String, String, String, Double)]()
      //println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
      articleList.foreach { tup =>
        val topic1 = tup._3
        val simTopic1 = tup._4
        val topic2 = tup._5
        val simTopic2 = tup._6
        var sTopic: String = ""
        var dMeasure: Double = 0.0
        if (curruserFTopicMap != null) {
          dMeasure = curruserFTopicMap.get(topic1).getOrElse(0.0) * simTopic1
          sTopic = topic1
          var dTemp = curruserFTopicMap.get(topic2).getOrElse(0.0) * simTopic2
          if (dTemp > dMeasure) {
            dMeasure = dTemp
            sTopic = topic2
          }
        }
        articleMessureList.+=((tup._1, tup._2, sTopic, dMeasure))
      }

      //println(articleId + "--" + sTopic + ":" + dMeasure)
      articleMessureList
    }

  def getFChannel(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getDouble(3))
  }
  /**
   * a_user_favorite_topic
   * userid,channel,topicid,weights
   */
  def getFTopic(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getDouble(4))
  }

  /**
   * a_article_topic
   * article_id,channel,topic1,sim_topic1,topic2,,sim_topic2
   */
  def getArticle(r: ResultSet) = {
    (r.getString(1), r.getString(2), r.getString(3), r.getDouble(4), r.getString(5), r.getDouble(6), r.getString(7))
  }
  def getEntity(r: ResultSet) = {
    (r.getString(1), r.getString(2))
  }

  def main(args: Array[String]): Unit = {
    train()
  }

}