package com.callcentresoftware;

public interface CallDataHandler {

	/**
	 * Called when data from the phone system comes in.
	 *
	 * @param data
	 *            A comma separated string of values. See {@link Column} for
	 *            more info. Example data: "3,RING,8019995555,8011234567"
	 */
	void onCallData(String data);

	/**
	 * Determines the number of calls that are in progress (haven't ended in a
	 * DROP yet).
	 *
	 * @return The number of active calls.
	 */
	int getNumberOfActiveCalls();

	/**
	 * Determines the number of calls that have completed (a completed call ends
	 * in a DROP event).
	 *
	 * @return The number of completed calls.
	 */
	int getNumberOfCompletedCalls();

	/**
	 * For the given {@link EventType}, calculates the total duration of that
	 * type of event in milliseconds.
	 *
	 * @param type
	 *            The type of event you want the duration of.
	 * @return The total duration of given event for all calls in milliseconds.
	 */
	long getTotalEventDuration(EventType type);

	/**
	 * Given a party, calculates the total duration of all calls they were on.
	 * The duration of a call includes all of the time between the DIAL and DROP
	 * events.
	 *
	 * @param party
	 *            The party for which you interested in his call time.
	 * @return The total call time for the given party.
	 */
	long getTotalCallTimeForParty(String party);

}

