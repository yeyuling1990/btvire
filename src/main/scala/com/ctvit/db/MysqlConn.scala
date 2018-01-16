package com.ctvit.db

import java.sql.DriverManager

import com.ctvit.config.Config
import java.sql.Connection
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.Date

object MysqlConn {
 
  def connMySQL(): Connection = {
    println("连接数据库")
     val MYSQL_DRIVER = "com.mysql.jdbc.Driver"
    Class.forName(MYSQL_DRIVER)
//    DriverManager.getConnection(Config.mysql_url, Config.mysql_username, Config.mysql_password)
    DriverManager.getConnection(Config.mysql_url, Config.mysql_username, Config.mysql_password)

    
  }
}