package com.ctvit.nlp.semantic.search.restrict;

public class DocInfo {

	private int ad;
	private int pd;
	private int[] ipc;
	private int status;
	private int prd;
	public int getAd() {
		return ad;
	}
	public void setAd(int ad) {
		this.ad = ad;
	}
	public int getPd() {
		return pd;
	}
	public void setPd(int pd) {
		this.pd = pd;
	}
	public int[] getIpc() {
		return ipc;
	}
	public void setIpc(int[] ipc) {
		this.ipc = ipc;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getPrd() {
		return this.prd;
	}
	public void setPrd(int prd) {
		this.prd=prd;
	}
}