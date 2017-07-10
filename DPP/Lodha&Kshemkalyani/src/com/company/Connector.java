package com.company;
// Klasa Connector sluzi za uspostavljanje uticnica izmedu procesa.

import java.io.*;
import java.util.*;
import java.net.*;

public class Connector {

	ServerSocket listener;
	Socket[] link;

	public void Connect(String baseName, int myId, int numProc,
			BufferedReader[] dataIn, PrintWriter[] dataOut) throws Exception {

		Name myNameClient = new Name();
		link = new Socket[numProc];
		int localPort = getLocalPort(myId);
		listener = new ServerSocket(localPort);

		// register in the name server
		myNameClient.insertName(baseName + myId, (InetAddress.getLocalHost()).getHostName(), localPort);

		// accept connections from all the smaller processes
		for(int i = 0; i < myId; ++i) {

			Socket s = listener.accept();

			BufferedReader dIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String getLine = dIn.readLine();
			StringTokenizer st = new StringTokenizer(getLine);
			int hisId = Integer.parseInt(st.nextToken());
			int destId = Integer.parseInt(st.nextToken());
			String tag = st.nextToken();

			if(tag.equals("hello")) {

				link[hisId] = s;
				dataIn[hisId] = dIn;
				dataOut[hisId] = new PrintWriter(s.getOutputStream());
			}
		}

		// contact all the bigger processes
		for(int i = myId; i < numProc; ++i) {

			PortAddr addr;

			do {
				addr = myNameClient.searchName(baseName + i);
				Thread.sleep(1000);
			}
			while(addr.getPort() == -1);

			link[i] = new Socket(addr.getHostName(), addr.getPort());
			dataOut[i] = new PrintWriter(link[i].getOutputStream());
			dataIn[i] = new BufferedReader(new InputStreamReader(link[i].getInputStream()));

			// send a "hello" message to P_i
			dataOut[i].println(myId + " " + i + " " + "hello" + " " + "null");
			dataOut[i].flush();
		}
	}

	int getLocalPort(int id) {
		return Symbols.ServerPort + 10 + id;
	}

	public void closeSockets() {

		try {
			listener.close();
			for(int i = 0; i < link.length; ++i)
				link[i].close();
		}
		catch(Exception e) {
			System.err.println(e);
		}
	}

}
