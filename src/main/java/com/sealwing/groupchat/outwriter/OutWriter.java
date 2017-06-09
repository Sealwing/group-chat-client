package com.sealwing.groupchat.outwriter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class OutWriter {

	private OutputStream out;

	public OutWriter(Socket socket) {
		try {
			out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeOut(int command, String message) {
		try {
			out.write(command);
			byte[] buffer = message.getBytes("UTF-8");
			out.write(buffer.length);
			out.write(buffer);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
