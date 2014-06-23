package com.limelight.emulator.dataparser;

public class H264NAL {
	public byte[] nalData;
	
	public H264NAL(byte[] nalData) {
		this.nalData = nalData;
	}
	
	public boolean isType(byte type) {
		return (nalData[0] == 0 && nalData[1] == 0 &&
			nalData[2] == 1 && nalData[3] == type) ||
			(nalData[0] == 0 && nalData[1] == 0 &&
			nalData[2] == 0 && nalData[3] == 1 &&
			nalData[4] == type);
	}
	
	public boolean isSps() {
		return isType((byte)0x67);
	}
	
	public boolean isPps() {
		return isType((byte)0x68);
	}
}
