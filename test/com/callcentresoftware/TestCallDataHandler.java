package com.callcentresoftware;

import static com.callcentresoftware.CallPlayer.startPlayingCallToHandler;
import static com.callcentresoftware.CallPlayer.waitForActiveCallCountToDropTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.callcentresoftware.impl.CallDataHandlerImpl;

public class TestCallDataHandler {

	private CallDataHandler getNewCallDataHandler() {
		return new CallDataHandlerImpl();
	}

	@Test
	public void test00() {
		CallDataHandler handler = getNewCallDataHandler();
		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(0, handler.getNumberOfCompletedCalls());
		assertEquals(0L, handler.getTotalEventDuration(EventType.RING));
		assertEquals(0L, handler.getTotalCallTimeForParty("8019995555"));
	}

	@Test
	public void test01() {
		CallDataHandler handler = getNewCallDataHandler();
		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(0, handler.getNumberOfCompletedCalls());

		handler.onCallData("1,DIAL,123,456");
		handler.onCallData("1,RING,123,456");
		assertEquals(1, handler.getNumberOfActiveCalls());
		assertEquals(0, handler.getNumberOfCompletedCalls());

		handler.onCallData("1,DROP,123,456");
		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(1, handler.getNumberOfCompletedCalls());
	}

	@Test
	public void test02() {
		CallDataHandler handler = getNewCallDataHandler();
		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(0, handler.getNumberOfCompletedCalls());

		handler.onCallData("1,DIAL,123,456");
		handler.onCallData("2,DIAL,124,457");
		handler.onCallData("3,DIAL,125,458");
		assertEquals(3, handler.getNumberOfActiveCalls());
		assertEquals(0, handler.getNumberOfCompletedCalls());

		handler.onCallData("1,DROP,123,456");
		assertEquals(2, handler.getNumberOfActiveCalls());
		assertEquals(1, handler.getNumberOfCompletedCalls());

		handler.onCallData("2,DROP,124,457");
		handler.onCallData("3,DROP,125,458");
		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(3, handler.getNumberOfCompletedCalls());
	}

	@Test
	public void test03() {
		CallDataHandler handler = getNewCallDataHandler();

		handler.onCallData("1,DIAL,123,456");
		handler.onCallData("1,RING,123,456");
		handler.onCallData("1,TALK,123,456");
		sleep(1000L);
		handler.onCallData("1,DROP,123,456");

		assertEquals(1000L, round(handler.getTotalEventDuration(EventType.TALK)));
	}

	@Test
	public void test04() {
		CallDataHandler handler = getNewCallDataHandler();

		handler.onCallData("1,DIAL,123,456");
		handler.onCallData("1,RING,123,456");
		handler.onCallData("1,TALK,123,456");
		sleep(1000L);
		handler.onCallData("1,DROP,123,456");

		assertEquals(1000L, round(handler.getTotalCallTimeForParty("123")));
	}

	@Test
	public void test05() {
		CallDataHandler handler = getNewCallDataHandler();
		startPlayingCallToHandler(1L, "test/datafiles/empty-call.txt", handler);
		waitForActiveCallCountToDropTo(0);

		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(0, handler.getNumberOfCompletedCalls());
		assertEquals(0L, handler.getTotalEventDuration(EventType.TALK));
	}

	@Test
	public void test06() {
		CallDataHandler handler = getNewCallDataHandler();
		startPlayingCallToHandler(1L, "test/datafiles/9-second-call-with-hold.txt", handler);
		waitForActiveCallCountToDropTo(0);

		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(1, handler.getNumberOfCompletedCalls());
		assertEquals(1000L, round(handler.getTotalEventDuration(EventType.DIAL)));
		assertEquals(2000L, round(handler.getTotalEventDuration(EventType.RING)));
		assertEquals(5000L, round(handler.getTotalEventDuration(EventType.TALK)));
		assertEquals(1000L, round(handler.getTotalEventDuration(EventType.HOLD)));
		assertEquals(0L, round(handler.getTotalEventDuration(EventType.DROP)));
	}

	@Test
	public void test07() {
		CallDataHandler handler = getNewCallDataHandler();
		long simultaneousCallCount = 100L;
		for (long i = 0; i < simultaneousCallCount; i++) {
			startPlayingCallToHandler(i, "test/datafiles/9-second-call-with-hold.txt", handler);
		}
		waitForActiveCallCountToDropTo(0);

		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(simultaneousCallCount * 1, handler.getNumberOfCompletedCalls());
		assertEquals(simultaneousCallCount * 1000L, round10(handler.getTotalEventDuration(EventType.DIAL)));
		assertEquals(simultaneousCallCount * 2000L, round10(handler.getTotalEventDuration(EventType.RING)));
		assertEquals(simultaneousCallCount * 5000L, round10(handler.getTotalEventDuration(EventType.TALK)));
		assertEquals(simultaneousCallCount * 1000L, round10(handler.getTotalEventDuration(EventType.HOLD)));
		assertEquals(simultaneousCallCount * 0L, round10(handler.getTotalEventDuration(EventType.DROP)));
	}

	@Test
	public void test08() {
		CallDataHandler handler = getNewCallDataHandler();
		long simultaneousCallCount = 1000L;
		for (long i = 0; i < simultaneousCallCount; i++) {
			startPlayingCallToHandler(i, "test/datafiles/9-second-call-with-hold.txt", handler);
		}

		waitForActiveCallCountToDropTo((int) simultaneousCallCount / 2);
		assertTrue("There should still be active calls.", handler.getNumberOfActiveCalls() > 0);
		int completedCalls = handler.getNumberOfCompletedCalls();
		assertTrue("There should be completed calls, but not all of them.", completedCalls > 0 && completedCalls < simultaneousCallCount);

		waitForActiveCallCountToDropTo(0);
		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(simultaneousCallCount * 1, handler.getNumberOfCompletedCalls());
	}

	@Test
	public void test09() {
		CallDataHandler handler = getNewCallDataHandler();
		long simultaneousCallCount = 10L;
		for (long i = 0; i < simultaneousCallCount; i++) {
			startPlayingCallToHandler(i, "test/datafiles/3-second-call-with-lots-of-holds.txt", handler);
		}
		waitForActiveCallCountToDropTo(0);

		assertEquals(0, handler.getNumberOfActiveCalls());
		assertEquals(simultaneousCallCount * 1, handler.getNumberOfCompletedCalls());
		assertEquals(simultaneousCallCount * 3000L, round10(handler.getTotalCallTimeForParty("216")));
	}

	@Test
	public void test10ExtraCredit() {
		CallDataHandler handler = getNewCallDataHandler();
		long simultaneousCallCount = 1000L;
		for (long i = 0; i < simultaneousCallCount; i++) {
			startPlayingCallToHandler(i, "test/datafiles/3-second-call-with-lots-of-holds.txt", handler);
		}
		waitForActiveCallCountToDropTo(0);
		assertEquals(simultaneousCallCount * 3000L, round100(handler.getTotalCallTimeForParty("216")));

		System.out.println("Testing to see how fast we can calculate call time by party, over and over...");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			handler.getTotalCallTimeForParty("216");
		}
		long runningTime = System.currentTimeMillis() - start;
		System.out.println("Total time: " + runningTime);
		assertTrue("To pass this test you'll need to optimize your implementation (an n-squared algorithm is too slow).", runningTime < 1000L);
	}

	/** Rounds to the nearest 1000. */
	private long round(long n) {
		return (n + 500L) / 1000L * 1000L;
	}

	/** Rounds to the nearest 10000. */
	private long round10(long n) {
		return (n + 5000L) / 10000L * 10000L;
	}

	/** Rounds to the nearest 100000. */
	private long round100(long n) {
		return (n + 50000L) / 100000L * 100000L;
	}

	private void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException ignore) {
		}
	}

}

