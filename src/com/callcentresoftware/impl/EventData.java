/ **
		* @author Ram Kashyap
		* @since 2019-09-15
		*/
		
package com.callcentresoftware.impl;

import com.callcentresoftware.EventType;

public class EventData {
	EventType event;
	Long eventStartTime;

	EventData(EventType event, Long eventStartTime){
		this.event = event;
		this.eventStartTime = eventStartTime;	
	}
	
	/**
	 * For an event return the elapsed time
	 * @param duration or time elapsed for an event
	 * */
	public Long getTimeElapsed(Long eventEndTime) {
		return eventEndTime-this.eventStartTime;
	}
	
	/** For an event get the event start time
	 * @return The event start time*/	
	public Long getEventStartTime() {
		return this.eventStartTime;
	}
	
	
}
