package com.limelight.emulator.dataparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class H264Parser {
	H264NAL spsNal;
	H264NAL ppsNal;
	LinkedList<H264Frame> frames;
	Iterator<H264Frame> frameIterator;
	
	public H264Parser() {
		spsNal = null;
		ppsNal = null;
		frames = new LinkedList<H264Frame>();
	}
	
	private int findFrameStart(byte[] fileData, int startOffset) {
		if (startOffset + 4 >= fileData.length) {
			return -1;
		}
		
		// Find the next frame start
		while (fileData[startOffset] != 0 || fileData[startOffset+1] != 0 ||
				fileData[startOffset+2] != 0 || fileData[startOffset+3] != 1) {
			startOffset++;
			
			if (startOffset + 4 >= fileData.length) {
				return -1;
			}
		}
		
		return startOffset;
	}
	
	private int findNalStart(byte[] fileData, int startOffset, int length) {
		if (startOffset + 3 >= length) {
			return -1;
		}
		
		// Find the next frame start
		while (fileData[startOffset] != 0 || fileData[startOffset+1] != 0 || fileData[startOffset+2] != 1) {
			startOffset++;
			
			if (startOffset + 3 >= length) {
				return -1;
			}
		}
		
		return startOffset;
	}
	
	public void loadFile(File f) throws IOException {
		FileInputStream fin = new FileInputStream(f);
		byte[] fileData = new byte[(int)f.length()];
		fin.read(fileData);
		fin.close();
		System.out.println("Read "+fileData.length+" bytes from file");
		
		// Start at the beginning of the first frame
		int frameStart;
		int frameEnd = 0;
		int frameIndex = 0;
		for (;;) {
			// Search for the next frame from the end of the last
			frameStart = findFrameStart(fileData, frameEnd);
			if (frameStart == -1) {
				break;
			}
			
			// Search for the end from the next byte
			frameEnd = findFrameStart(fileData, frameStart+1);
			if (frameEnd == -1) {
				frameEnd = fileData.length;
			}
			
			//System.out.println("Frame "+frameIndex+" is from "+frameStart+" to "+frameEnd);
			ArrayList<H264NAL> nalList = new ArrayList<H264NAL>();
			
			int nalStart = frameStart;
			int nalEnd = frameStart;
			for (;;) {
				nalEnd = findNalStart(fileData, nalStart+2, frameEnd);
				if (nalEnd == -1) {
					nalEnd = frameEnd;
				}
				
				nalList.add(new H264NAL(Arrays.copyOfRange(fileData, nalStart, nalEnd)));
				
				if (nalEnd == frameEnd) {
					break;
				}
				
				nalStart = findNalStart(fileData, nalEnd, frameEnd);
				if (nalStart == -1) {
					break;
				}
			}
						
			// Find any SPS or PPS data
			for (H264NAL nal : nalList) {
				if (nal.isSps()) {
					System.out.println("Found SPS NAL");
					spsNal = nal;
				}
				else if (nal.isPps()) {
					System.out.println("Found PPS NAL");
					ppsNal = nal;
				}
			}
			
			if (nalList.isEmpty()) {
				break;
			}
			
			//System.out.println("Frame "+frameIndex+" is finished with "+nalList.size()+" NALs");
			frames.add(new H264Frame(nalList));
			frameIndex++;
			
			if (frameEnd == fileData.length) {
				break;
			}
		}
		
		reset();
	}
	
	public void reset() {
		frameIterator = frames.iterator();
	}
	
	public H264Frame nextFrame() {
		// Wait 33 ms (30 FPS)
		try {
			Thread.sleep(33);
		} catch (InterruptedException e) {
			return null;
		}
		
		if (frameIterator.hasNext()) {
			return frameIterator.next();
		}
		
		return null;
	}
	
	public byte[] getSps() {
		return spsNal.nalData;
	}
	
	public byte[] getPps(int index) {
		// FIXME: We're lying and not using the index
		return ppsNal.nalData;
	}
	
	
}
