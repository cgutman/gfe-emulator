package com.limelight.emulator.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Control {
	private ServerSocket serverSocket;
	
	public static final int PORT = 47995;
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Control connection listening on "+PORT);
		new Thread() {
			@Override
			public void run() {
				for (;;) {
					try {
						Socket s = serverSocket.accept();
						
						System.out.println("Control connected from "+s.getRemoteSocketAddress());
						try {
							// Wait for the client to close this connection
							InputStream sin = s.getInputStream();
							OutputStream sout = s.getOutputStream();
							while (sin.read() != -1) {
								// Send some garbage so handshake can finish
								sout.write(0);
							}
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
