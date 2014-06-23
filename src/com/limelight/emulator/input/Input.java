package com.limelight.emulator.input;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Input {
	private ServerSocket serverSocket;
	
	public static final int PORT = 35043;
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Input connection listening on "+PORT);
		
		new Thread() {
			@Override
			public void run() {
				for (;;) {
					try {
						Socket s = serverSocket.accept();
						
						System.out.println("Input connected from "+s.getRemoteSocketAddress());
						try {
							// Wait for the client to close this connection
							InputStream sin = s.getInputStream();
							while (sin.read() != -1);
						} catch (IOException e) {
							// Client died; continue
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}
