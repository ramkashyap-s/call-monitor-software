package com.callcentresoftware;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicInteger;

public class CallPlayer {

	private static AtomicInteger playCount = new AtomicInteger(0);

	public static void startPlayingCallToHandler(long callId, String callEventsFile, CallDataHandler handler) {
		playCount.incrementAndGet();
		new Thread(() -> {
			playFile(callId, callEventsFile, handler);
			playCount.decrementAndGet();
		}).start();
	}

	private static void playFile(long callId, String callEventsFile, CallDataHandler handler) {
		try (BufferedReader reader = new BufferedReader(new FileReader(callEventsFile))) {
			String line = reader.readLine();
			while (line != null && !line.isEmpty()) {
				if (line.startsWith(",")) {
					handler.onCallData(callId + line);
				} else if (line.startsWith("#")) {
					continue;
				} else {
					try { Thread.sleep(Long.parseLong(line)); } catch (Exception ignore) { }
				}
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void waitForActiveCallCountToDropTo(int count) {
		do {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		} while (playCount.get() > count);
	}

}
