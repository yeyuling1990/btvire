package com.ctvit.config

import java.io.IOException
import java.io.InputStream
import java.util.Properties
import java.io.FileInputStream
import java.io.File

object Config {
  var mysql_url = ""
  var mysql_username = ""
  var mysql_password = ""
  var wordModelPath = ""
  var topicModePath = ""
  var recommendNum :Int = 0
  var nrentityobj = ""
  var properties = new Properties()
  println("开始读取配置文件！")
  //本地读取配置文件代码，需要将配置文件放入到target中
//  var path = Thread.currentThread().getContextClassLoader.getResource("db.properties").getPath //文件要放到resource文件夹下

  //集群读取配置文件代码
  var directory = new File("..")
  var filePath = directory.getAbsolutePath
  var path = filePath+"/home"+"/db.properties"
  println(path)

  properties.load(new FileInputStream(path))
  //properties.load(new FileInputStream("E:\\code\\eclipse\\scalaspark\\target\\classes\\com\\ctvit\\config\\db.properties"))
  mysql_url = properties.getProperty("mysql_url")
  mysql_username = properties.getProperty("mysql_username")
  mysql_password = properties.getProperty("mysql_password")
  wordModelPath = properties.getProperty("wordModelPath")
  topicModePath = properties.getProperty("topicModePath")
  recommendNum = properties.getProperty("recommendNum").toInt
  nrentityobj = properties.getProperty("nrentityobj")
  println("结束读取配置文件！")

}