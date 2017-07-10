package com.company;

import java.io.*;

public interface MsgHandler {

	public void handleMsg(Msg M, int srcId, String tag);
	public Msg receiveMsg(int fromId) throws IOException;
}