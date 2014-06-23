package com.limelight.emulator.av;

import java.nio.ByteBuffer;

public class RtpHeader {
	private byte[] payload;
	
	public static final int HEADER_SIZE = 12;
	
	static short sequenceNumber = 0;
	
	public RtpHeader(byte[] payload) {
		this.payload = payload;
	}
	
	public byte[] toBytes() {
		byte[] full = new byte[payload.length+HEADER_SIZE];
		
		ByteBuffer bb = ByteBuffer.wrap(full);
		bb.put((byte)0x80); // Version
		bb.put((byte) 96); // Payload type
		bb.putShort(sequenceNumber++); // Seq number
		
		System.arraycopy(payload, 0, full, HEADER_SIZE, payload.length);
		return full;
	}
}
