/ **
		* @author Ram Kashyap
		* @since 2019-09-15
		*/
package com.callcentresoftware.impl;


public class CallData {
	private long callStartTime;
	private EventData currentEvent;

	public CallData (EventData currentEvent, long callStartTime) {
		this.callStartTime = callStartTime;
		this.currentEvent = currentEvent;		
	}	
	
	/** For a given call get the call start time
	 * @return The call start time*/
	public long getCallStartTime() {
		return callStartTime;
	}

	/** 
	 * For a given call get the current/recent event
	 * @return 
	 * 		current or recent event
	 * */
	public EventData getCurrentEvent() {		
		return currentEvent;
	}

	/** For a given call set the current/recent event
	 * @param current or recent event*/
	public void setCurrentEvent(EventData currentEvent) {
		this.currentEvent = currentEvent;
	}


	/** For a given call return the elapsed time
	 * @param duration or time elapsed for a call*/
	public long getTimeElapsed(long callEndTime) {
		return callEndTime-this.callStartTime;
	}
	

}

