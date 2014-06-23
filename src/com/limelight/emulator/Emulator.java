package com.limelight.emulator;

import java.io.File;
import java.io.IOException;

import com.limelight.emulator.av.Video;
import com.limelight.emulator.av.VideoPacketizer;
import com.limelight.emulator.control.Control;
import com.limelight.emulator.dataparser.H264Parser;
import com.limelight.emulator.handshake.Handshake;
import com.limelight.emulator.input.Input;

public class Emulator {
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: gfe-emulator <.h264 file>");
			return;
		}
		
		H264Parser parser = new H264Parser();
		parser.loadFile(new File(args[1]));
		
		Handshake h = new Handshake();
		Control c = new Control();
		Input i = new Input();
		Video v = new Video(new VideoPacketizer(parser));
		
		h.start();
		c.start();
		i.start();
		v.start();
	}
}
