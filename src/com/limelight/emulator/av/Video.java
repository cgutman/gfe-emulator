package com.limelight.emulator.av;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Video {
	private VideoPacketizer packetizer;
	
	private ServerSocket serverSocket;
	private DatagramSocket rtpSocket;
	
	private InetSocketAddress videoTarget;
	
	public static final int FIRST_FRAME_PORT = 47996;
	public static final int VIDEO_PORT = 47998;
	
	public Video(VideoPacketizer packetizer) {
		this.packetizer = packetizer;
	}
	
	public void start() throws IOException {
		serverSocket = new ServerSocket(FIRST_FRAME_PORT);
		System.out.println("First frame connection listening on "+FIRST_FRAME_PORT);
		
		rtpSocket = new DatagramSocket(VIDEO_PORT);
		System.out.println("RTP socket bound to "+VIDEO_PORT);
		
		// First frame server port
		new Thread() {
			@Override
			public void run() {
				for (;;) {
					try {
						Socket s = serverSocket.accept();
						
						System.out.println("Accepted first frame connection from "+s.getRemoteSocketAddress());
						try {
							// Send the first frame data and close the connection
							OutputStream sout = s.getOutputStream();
							sout.write(packetizer.getFirstFrameData());
							s.close();
						} catch (IOException e) {
							// Client died; continue
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		// Video receive thread
		new Thread() {
			@Override
			public void run() {
				DatagramPacket packet = new DatagramPacket(new byte[10], 0, 10);
				for (;;) {
					try {
						rtpSocket.receive(packet);
						
						// If we get a ping from an unknown target, restart the video sending
						if (videoTarget == null || !packet.getSocketAddress().equals(videoTarget)) {
							videoTarget = (InetSocketAddress) packet.getSocketAddress();
							packetizer.reset();
							System.out.println("Starting video sending to "+videoTarget.getHostString());
							
							// Video sending thread
							new Thread() {
								@Override
								public void run() {
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e1) {
										return;
									}
									
									for (;;) {
										byte[] packetData = packetizer.getNextPacket();
										if (packetData == null) {
											System.out.println("Done sending to "+videoTarget.getHostString());
											videoTarget = null;
											return;
										}
										
										try {
											DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
											packet.setSocketAddress(videoTarget);
											rtpSocket.send(packet);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							}.start();
						}
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		}.start();
	}
}
