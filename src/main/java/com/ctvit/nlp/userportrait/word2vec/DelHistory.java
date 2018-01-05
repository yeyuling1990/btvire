package com.ctvit.nlp.userportrait.word2vec;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 删除过期数据，根据系统定义的过期时间跨度，删除过期数据
 * @author qsy
 *
 */
public class DelHistory {

	/**
	 * 删除article_topic表中过期数据
	 * @param date
	 */
	public void delArticleTopic(String dateString)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
		Date date = new Date();
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 删除推荐准备表中的过期数据
	 * @param date
	 */
	public void delRecReady(String date)
	{
		
	}
}
