/ **
  * @author Ram Kashyap
  * @since 2019-09-15
		*/
package com.callcentresoftware.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.callcentresoftware.CallDataHandler;
import com.callcentresoftware.Column;
import com.callcentresoftware.EventType;

public class CallDataHandlerImpl implements CallDataHandler {
	
	private Map<Long, CallData> callTracker;
	private Map<String, Long> partyDuration;
	private Map<EventType, Long> eventDuration;
	private AtomicInteger numberOfActiveCalls;
	private AtomicInteger numberOfCompletedCalls;
	
	public CallDataHandlerImpl() {
		super();
		this.numberOfActiveCalls = new AtomicInteger();
		this.numberOfCompletedCalls = new AtomicInteger();
		this.callTracker = new ConcurrentHashMap<>(); // track calldata using callId
		this.partyDuration = new ConcurrentHashMap<>(); // track total call duration for a party
		this.eventDuration = new ConcurrentHashMap<>(); // track total call duration for an event
	}
	
	/**
	 * Called when data from the phone system comes in.
	 *
	 * @param data
	 *            A comma separated string of values. See {@link Column} for
	 *            more info. Example data: "3,RING,8019995555,8011234567"
	 *            
	 * Assumptions: 
	 * 1. Considers data only if it has all the four fields
	 * 2. All the events would follow a valid event ordering
	 * 3. Unique CallId's
	 * */
	@Override
	public void onCallData(String data) {
		// variables for storing split data 
		String[] dataValues;
		long callId;
		EventType event;
		String callingParty;
		String receivingParty;	

		// process string and store data values 
		if(data != null && !data.isEmpty() && data.split(",").length == 4) {
			dataValues = data.split(",");
			callId = Long.parseLong(dataValues[0]);			
			event = EventType.valueOf(dataValues[1]);
			callingParty = dataValues[2];
			receivingParty = dataValues[3];

			if(event.equals(EventType.DIAL)) {
				this.dialEvent(callId, event);
			} 
			else if(event.equals(EventType.RING) || event.equals(EventType.TALK) || event.equals(EventType.HOLD)) {				
				this.ringTalkHoldEvent(callId, event);
			} 
			else {	// if the event is DROP
				this.dropEvent(event, callId, callingParty, receivingParty);
			}
		}		
	}
	
	
	/** 
	 * Creates a new call and a new dial event and updates the callTracker with the new call
	 * @param callId
	 *            callId of the new call
	 * @param event
	 * 			  current event            
	 */
	private void dialEvent(long callId, EventType event) {
		long currentTime = System.currentTimeMillis();
		// create a new event and call 
		EventData currentEvent = new EventData(event,currentTime);
		CallData currentCall = new CallData(currentEvent, currentTime);					
		
		// map current call with callId
		callTracker.putIfAbsent(callId,currentCall);	
		
		// increment the active call count
		numberOfActiveCalls.incrementAndGet();		
	}
	
	
	/** 
	 * Update the current call with the new RING/TALK/HOLD events and update the eventDuration with the previous event duration
	 * @param callId
	 *            callId of the new call
	 * @param event
	 * 			  current event            
	 */
	private void ringTalkHoldEvent(long callId, EventType event) {
		long currentTime = System.currentTimeMillis();
		// create a new event  					
		EventData currentEvent = new EventData(event,currentTime);
		CallData currentCall = callTracker.get(callId);						
		EventData previousEvent = currentCall.getCurrentEvent();
		
		// update eventDuration map with the previous event's duration  
		eventDuration.merge(previousEvent.event, previousEvent.getTimeElapsed(currentTime), Long::sum); 
		
		// update current call with current event
		currentCall.setCurrentEvent(currentEvent);
	}
	
	
	/** 
	 * Update the current call with the new RING/TALK/HOLD events and update the eventDuration with the previous event duration
	 * @param callId
	 *            callId of the new call
	 * @param event
	 * 			  current event            
	 */	
	private void dropEvent(EventType event, long callId, String callingParty, String receivingParty) {
		long currentTime = System.currentTimeMillis();
		// create a new event  										
		EventData currentEvent = new EventData(event,currentTime);
		CallData currentCall = callTracker.get(callId);
		EventData previousEvent = currentCall.getCurrentEvent();

		// update eventDuration map with the previous event's duration  
		eventDuration.merge(previousEvent.event, previousEvent.getTimeElapsed(currentTime), Long::sum); 
		
		// update current call with current event
		currentCall.setCurrentEvent(currentEvent);					
		
		// update partyDuration map with the calling and receiving party's duration  
		partyDuration.merge(callingParty, currentCall.getTimeElapsed(currentTime), Long::sum);		
		partyDuration.merge(receivingParty, currentCall.getTimeElapsed(currentTime), Long::sum);							
		
		// remove call from callTracker map
		callTracker.remove(callId);		
		
		// increment the number of completed calls and decrement the number of active calls
        numberOfActiveCalls.decrementAndGet();
		numberOfCompletedCalls.incrementAndGet();					

		
		// map current call with callId
		callTracker.putIfAbsent(callId,currentCall);			
	}

	
	@Override
	public int getNumberOfActiveCalls() {
		return numberOfActiveCalls.get();
	}

	
	@Override
	public int getNumberOfCompletedCalls() {
		return numberOfCompletedCalls.get();
	}

	
	@Override
	public synchronized long getTotalEventDuration(EventType type) {
		return eventDuration.getOrDefault(type,0L);
	}

	
	@Override
	public synchronized long getTotalCallTimeForParty(String party) {
		return partyDuration.getOrDefault(party, 0L);
	}

}
