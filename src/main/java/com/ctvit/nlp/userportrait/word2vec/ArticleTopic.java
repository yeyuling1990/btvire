package com.ctvit.nlp.userportrait.word2vec;

import java.sql.Date;

public class ArticleTopic {
	private String article_id;
	private String channel;
	private String topic1;
	private Double sim_topic1;
	private String topic2;
	private Double sim_topic2;
	private int recommendOrNot;
	private Date addtime;
	
	public String getArticle_id() {
		return article_id;
	}
	public void setArticle_id(String article_id) {
		this.article_id = article_id;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getTopic1() {
		return topic1;
	}
	public void setTopic1(String topic1) {
		this.topic1 = topic1;
	}
	public Double getSim_topic1() {
		return sim_topic1;
	}
	public void setSim_topic1(Double sim_topic1) {
		this.sim_topic1 = sim_topic1;
	}
	public String getTopic2() {
		return topic2;
	}
	public void setTopic2(String topic2) {
		this.topic2 = topic2;
	}
	public Double getSim_topic2() {
		return sim_topic2;
	}
	public void setSim_topic2(Double sim_topic2) {
		this.sim_topic2 = sim_topic2;
	}
	public int getRecommendOrNot() {
		return recommendOrNot;
	}
	public void setRecommendOrNot(int RecommendOrNot) {
		this.recommendOrNot = RecommendOrNot;
	}
	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}
}
