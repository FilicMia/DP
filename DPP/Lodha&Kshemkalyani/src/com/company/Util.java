package com.company;

import java.util.*;

public class Util {

	public static int max(int a, int b) {

		if(a > b) return a;
		else return b;
	}

	public static void mySleep(int time) {

		try {
			Thread.sleep(time);
		}
		catch(InterruptedException e) { }
	}

	public static void myWait(Object obj) {

		println("Waiting!");
		
		try {
			obj.wait();
		}
		catch(InterruptedException e) { }

	}

	public static boolean lessThan(int A[], int B[]) {

		for(int i = 0; i < A.length; ++i)
			if(A[i] > B[i]) return false;

		for(int i = 0; i < A.length; ++i)
			if(A[i] < B[i]) return true;

		return false;
	} 

	public static int maxArray(int[] A) {

		int v = A[0];

		for(int i = 0; i < A.length; ++i)
			if(A[i] > v) v = A[i];

		return v;
	}

	public static String writeArray(int[] A) {

		StringBuffer s = new StringBuffer();
		
		for(int i = 0; i < A.length; ++i)
			s.append(String.valueOf(A[i]) + " ");

		return new String(s.toString());
	}

	public static void readArray(String s, int[] A) {

		StringTokenizer st = new StringTokenizer(s);
	
		for(int i = 0; i < A.length; ++i)
			A[i] = Integer.parseInt(st.nextToken());		
	}

	public static int searchArray(int[] A, int x) {

		for(int i = 0; i < A.length; ++i)
			if(A[i] == x) return i;

		return -1;
	}

	public static void println(String s) {

		if(Symbols.debugFlag) {

			System.out.println(s);
			System.out.flush();
		}
	}

}
