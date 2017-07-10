package com.company;

import java.util.*;

public class Msg {

	int srcId, destId;
	String tag;
	String msgBuf;

	public Msg(int s, int t, String msgType, String buf) {

		this.srcId = s;
		this.destId = t;
		this.tag = msgType;
		this.msgBuf = buf;
	}

	public int getSrcId() {
		return srcId;
	}

	public int getDestId() {
		return destId;
	}

	public String getTag() {
		return tag;
	}

	public String getMessage() {
		return msgBuf;
	}

	public int getMessageInt() {
		
		StringTokenizer st = new StringTokenizer(msgBuf); // Allows application to break string into tokens.
		return Integer.parseInt(st.nextToken()); // nextToken() returns a String
	}

	public static Msg parseMsg(StringTokenizer st) {

		int srcId = Integer.parseInt(st.nextToken());
		int destId = Integer.parseInt(st.nextToken());
		String tag = st.nextToken();
		String buf = st.nextToken("#");

		return new Msg(srcId, destId, tag, buf);
	}

	public String toString() {

		String s = String.valueOf(srcId) + " " + String.valueOf(destId) +
				" " + tag + " " + msgBuf + "#";

		return s;
	}	
}
