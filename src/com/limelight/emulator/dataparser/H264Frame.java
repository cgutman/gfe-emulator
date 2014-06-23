package com.limelight.emulator.dataparser;

import java.util.ArrayList;

public class H264Frame {
	private ArrayList<H264NAL> NALs;
	private int length;
	
	public H264Frame(ArrayList<H264NAL> NALs) {
		this.NALs = NALs;
		
		for (H264NAL nal : NALs) {
			length += nal.nalData.length;
		}
	}
	
	public int getLength() {
		return length;
	}
	
	public byte[] toBytes() {
		int offset = 0;
		byte[] buffer = new byte[length];
		for (H264NAL nal : NALs) {
			byte[] nalData = nal.nalData;
			System.arraycopy(nalData, 0, buffer, offset, nalData.length);
			offset += nalData.length;
		}
		return buffer;
	}
}
