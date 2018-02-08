package com.ctvit.train

import java.util.Date
import java.text.SimpleDateFormat
import java.text.Format

object RDDtoMysql {

  def main(args: Array[String]) {
    val fd:String = "2018-02-05 08:11:22"
    val toNow: Date = new Date()
    val dateFormat:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val now = toNow.getTime
    val fast = dateFormat.parse(fd).getTime
    val hourDiff = (now - fast)/3600000
    println(hourDiff)
  }
  
 
} 