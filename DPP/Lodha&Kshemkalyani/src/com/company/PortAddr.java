package com.company;

public class PortAddr {

	String hostname;
	int portnum;
	
	public PortAddr(String s, int i) {

		this.hostname = s;
		this.portnum = i;
	}

	public String getHostName() {
		return hostname;
	}

	public int getPort() {
		return portnum;
	}

}