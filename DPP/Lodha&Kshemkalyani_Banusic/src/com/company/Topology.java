package com.company;

import java.io.*;
import java.util.*;

public class Topology {
	
	public static void readNeighbors(int myId, int N, LinkedList<Integer> neighbors) {

		Util.println("Reading topology:");

		try {
	
			BufferedReader dIn = new BufferedReader(new FileReader("topology" + myId));
			StringTokenizer st = new StringTokenizer(dIn.readLine());

			while(st.hasMoreTokens()) {

				int neighbor = Integer.parseInt(st.nextToken());
				neighbors.add(neighbor);
			}
		}
		catch(FileNotFoundException e) {

			for(int i = 0; i < N; ++i)
				if(i != myId)
					neighbors.add(i);
		}
		catch(IOException e) {

			System.err.println(e);
		}

		Util.println(neighbors.toString());		
	}	
	
}
