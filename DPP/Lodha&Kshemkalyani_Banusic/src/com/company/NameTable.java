package com.company;

import java.util.*;

public class NameTable {

	final int maxSize = 100;
	String[] names = new String[maxSize];
	String[] hosts = new String[maxSize];
	int[] ports = new int[maxSize];
	int dirsize = 0;

	public int search(String s) {
		
		for(int i = 0; i < dirsize; ++i)
			if(names[i].equals(s))
				return i;

		return -1;
	}

	public int insert(String s, String hostName, int portNumber) {
		
		int oldIndex = search(s); // is it already there
		
		if(oldIndex == -1) {

			names[dirsize] = s;
			hosts[dirsize] = hostName;
			ports[dirsize++] = portNumber;

			return 1;
		}
		else return 0;
	}

	public int getPort(int index) {

		return ports[index];
	}				
	
	public String getHostName(int index) {
		
		return hosts[index];
	}

}
