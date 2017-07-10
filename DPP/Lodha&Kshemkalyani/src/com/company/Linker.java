package com.company;

import java.io.*;
import java.util.*;

public class Linker {

	PrintWriter[] dataOut;
	BufferedReader[] dataIn;
	int myId, N;
	Connector connector;
	public LinkedList<Integer> neighbors = new LinkedList<Integer>();

	public Linker(String basename, int id, int numproc) throws Exception {

		myId = id;
		N = numproc;
		dataIn = new BufferedReader[numproc];
		dataOut = new PrintWriter[numproc];

		Topology.readNeighbors(myId, N, neighbors);

		connector = new Connector();
		connector.Connect(basename, myId, numproc, dataIn, dataOut);
	}

	public void sendMsg(int destId, String tag, String msg) {

		dataOut[destId].println(myId + " " + destId + " " + tag + " " + msg + " " + "#");
		dataOut[destId].flush();		
	}

	public void sendMsg(int destId, String tag) {

		sendMsg(destId, tag, " 0 ");
	}

	public void multicast(LinkedList<Integer> destIds, String tag, String msg) {
	
		for(int i = 0; i < destIds.size(); ++i)
			sendMsg(destIds.get(i), tag, msg);
	}

	public Msg receiveMsg(int fromId) throws IOException {
		
		String getline = dataIn[fromId].readLine();
		Util.println(" Received message: " + getline);

		StringTokenizer st = new StringTokenizer(getline);
		int srcId = Integer.parseInt(st.nextToken());
		int destId = Integer.parseInt(st.nextToken());
		String tag = st.nextToken();
		String msg = st.nextToken("#");

		return new Msg(srcId, destId, tag, msg);
	}

	public int getMyId() {
		return myId;
	}

	public int getNumProc() {
		return N;
	}
	
	public void close() {

		connector.closeSockets();
	}
}
