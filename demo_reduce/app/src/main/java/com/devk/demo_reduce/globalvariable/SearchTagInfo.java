package com.devk.demo_reduce.globalvariable;

/**
 * @author Administrator
 * 这是标签的信息类，包括ＥＰＣ码，和读到的次数，以及ＲＳＳＩ值
 */
public class SearchTagInfo {
	private String EPC;
	private int count;
	private int RSSI;
	private int ANT;
	
	public SearchTagInfo(String ePC, int count, int rSSI, int rANT) {
		super();
		this.EPC = ePC;
		this.count = count;
		this.RSSI = rSSI;
		this.ANT = rANT;
	}
	public String getEPC() {
		return EPC;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getRSSI() {
		return RSSI;
	}

	public int getANT() {
		return ANT;
	}
	public void setANT(int rANT) {
		ANT = rANT;
	}
	public void setRSSI(int rSSI) {
		RSSI = rSSI;
	}
}
