package com.ctvit.nlp.userportrait.word2vec;

import java.sql.Date;

public class Article {
	private String mid;
	private String ti;
	private String brief;
	private String tag;
	private String content;
	private String column1;
	private String channel;
	private Date addTime;
	private int cmptTopic;
	
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getTi() {
		return ti;
	}
	public void setTi(String ti) {
		this.ti = ti;
	}
	public String getBrief() {
		return brief;
	}
	public void setBrief(String brief) {
		this.brief = brief;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getColumn1() {
		return column1;
	}
	public void setColumn1(String column1) {
		this.column1 = column1;
	}
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public int isCmptTopic() {
		return cmptTopic;
	}
	public void setCmptTopic(int cmptTopic) {
		this.cmptTopic = cmptTopic;
	}
}
