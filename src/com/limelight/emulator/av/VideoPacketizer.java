package com.limelight.emulator.av;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import com.limelight.emulator.dataparser.H264Frame;
import com.limelight.emulator.dataparser.H264Parser;
import com.limelight.emulator.utils.Utils;

public class VideoPacketizer {
	private H264Parser parser;
	private boolean needsIdr;
	
	private int streamPacketIndex;
	private int frameIndex;
	private int packetIndex;
	
	private LinkedList<RtpHeader> packets = new LinkedList<RtpHeader>();
	private Iterator<RtpHeader> packetIterator = packets.iterator();
	
	public VideoPacketizer(H264Parser parser) {
		this.parser = parser;
	}
	
	public void notifyNeedsIdr() {
		synchronized (this) {
			needsIdr = true;
		}
	}
	
	public void reset() {
		parser.reset();
		
		notifyNeedsIdr();
	}
	
	private void parseNextFrame() {
		H264Frame frame = parser.nextFrame();
		if (frame == null) {
			return;
		}
		
		byte[] frameData = frame.toBytes();
		
		int totalPackets = frameData.length / (VideoHeader.PACKET_SIZE - VideoHeader.HEADER_SIZE);
		totalPackets += (frameData.length % (VideoHeader.PACKET_SIZE - VideoHeader.HEADER_SIZE) != 0) ? 1 : 0;
				
		frameIndex++;
		packetIndex = 0;
		
		packets.clear();
		int offset = 0;
		do {
			byte[] videoPayload = Arrays.copyOfRange(frameData, offset, Math.min(frameData.length, offset+VideoHeader.PACKET_SIZE - VideoHeader.HEADER_SIZE));
			offset += VideoHeader.PACKET_SIZE - VideoHeader.HEADER_SIZE;
			
			int flags = 0;
			
			if (packets.isEmpty()) {
				flags |= VideoHeader.FLAG_SOF;
			}
			if (offset >= frameData.length) {
				flags |= VideoHeader.FLAG_EOF;
			}
			
			packets.add(new RtpHeader(new VideoHeader(frameIndex, packetIndex++, totalPackets, flags, streamPacketIndex++, videoPayload).toBytes()));
		} while (offset < frameData.length);
		
		packetIterator = packets.iterator();
	}
	
	public byte[] getNextPacket() {
		if (!packetIterator.hasNext()) {
			parseNextFrame();
		}
		
		if (!packetIterator.hasNext()) {
			// We're really done now
			return null;
		}
		
		return packetIterator.next().toBytes();
	}
	
	public byte[] getFirstFrameData() {
		return new VideoHeader(Utils.concatBytes(parser.getSps(), Utils.concatBytes(parser.getPps(0), parser.getPps(1)))).toBytes();
	}
}
