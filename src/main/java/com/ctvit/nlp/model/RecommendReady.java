package com.ctvit.nlp.model;

import java.sql.Date;

public class RecommendReady {
	
   private String articleId;	//文章id
   private String userId;//用户id
   private String articleChannel;//文章所属频道
   private String articleTopic;//文章topic
   private double recMearure;//推荐度量
   private 	int recFlag;//是否推荐
   private Date addDate;	//文章添加时间
public String getArticleId() {
	return articleId;
}
public void setArticleId(String articleId) {
	this.articleId = articleId;
}
public String getUserId() {
	return userId;
}
public void setUserId(String userId) {
	this.userId = userId;
}
public String getArticleChannel() {
	return articleChannel;
}
public void setArticleChannel(String articleChannel) {
	this.articleChannel = articleChannel;
}
public String getArticleTopic() {
	return articleTopic;
}
public void setArticleTopic(String articleTopic) {
	this.articleTopic = articleTopic;
}
public double getRecMearure() {
	return recMearure;
}
public void setRecMearure(double recMearure) {
	this.recMearure = recMearure;
}
public int getRecFlag() {
	return recFlag;
}
public void setRecFlag(int recFlag) {
	this.recFlag = recFlag;
}
public Date getAddDate() {
	return addDate;
}
public void setAddDate(Date addDate) {
	this.addDate = addDate;
}

}
