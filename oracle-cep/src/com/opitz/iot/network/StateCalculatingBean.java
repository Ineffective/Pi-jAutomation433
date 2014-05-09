package com.opitz.iot.network;

import java.util.Date;
import java.util.Map;

import com.bea.wlevs.ede.api.Adapter;
import com.bea.wlevs.ede.api.EventRejectedException;
import com.bea.wlevs.ede.api.RunnableBean;
import com.bea.wlevs.ede.api.StreamSender;
import com.bea.wlevs.ede.api.StreamSink;
import com.bea.wlevs.ede.api.StreamSource;

public class StateCalculatingBean implements StreamSink, StreamSource {

	private Map<String, UserStateEvent> userMacPairs;
	private StreamSender eventSender;

	@Override
	public void onInsertEvent(Object event) throws EventRejectedException {
		
		UserNodeStateEvent userNodeState = (UserNodeStateEvent) event;
		System.out.println("User: " + userNodeState.getUsername() + " was " + userNodeState.isNodeFound());
		if(userMacPairs.get(userNodeState.getUsername()) != null){
			calculateUserState(userNodeState);
		}
		else{
			instantiateNewUserState(userNodeState);
		}
	}

	private UserStateEvent calculateUserState(UserNodeStateEvent userNodeState) {
		UserStateEvent currentKnownUserState = userMacPairs.get(userNodeState
				.getUsername());


		// our 6 cases, see confluence for description
		
		//user found
		if (userNodeState.isNodeFound()) {
			switch (currentKnownUserState.getUserState()) {
			case ONLINE:
				currentKnownUserState.setLastSeen(new Date());
				break;
			case OFFLINE:
				//user was offline is now online
				raiseEvent(new UserStateEvent(userNodeState.getUsername(), UserState.ONLINE));
				currentKnownUserState.setLastSeen(new Date());
				currentKnownUserState.setUserState(UserState.ONLINE);
				break;
			case MISSING:
				currentKnownUserState.setLastSeen(new Date());
				currentKnownUserState.setUserState(UserState.ONLINE);
				break;

			}
		} 
		//if user not found
		else {
			switch (currentKnownUserState.getUserState()) {
			case ONLINE:
				currentKnownUserState.setUserState(UserState.MISSING);
				break;
			case OFFLINE:
				//nothing
				break;
			case MISSING:
				//threshold of 5 min exceeded?
				if(currentKnownUserState.getLastSeen().getTime() < (System.currentTimeMillis() - 1000 * 60 * 5)){
					currentKnownUserState.setUserState(UserState.OFFLINE);
					raiseEvent(new UserStateEvent(userNodeState.getUsername(), UserState.OFFLINE));
				}
				break;

			}
		}

		return null;
	}
	
	private void instantiateNewUserState(UserNodeStateEvent userNodeStateEvent){
		if(userNodeStateEvent.isNodeFound()){
			UserStateEvent stateEvent = new UserStateEvent(userNodeStateEvent.getUsername(), UserState.ONLINE);
			raiseEvent(stateEvent);
			putUserStateIntoCache(stateEvent);
		}else{
			UserStateEvent stateEvent = new UserStateEvent(userNodeStateEvent.getUsername(), UserState.OFFLINE);
			raiseEvent(stateEvent);
			putUserStateIntoCache(stateEvent);
		}
	}

	private void putUserStateIntoCache(UserStateEvent use){
		userMacPairs.put(use.getUsername(), use);
	}

	private void raiseEvent(UserStateEvent userStateEvent) {
		eventSender.sendInsertEvent(userStateEvent);
	}

	public Map<String, UserStateEvent> getUserStates() {
		return userMacPairs;
	}

	public void setUserStates(Map<String, UserStateEvent> userStates) {
		this.userMacPairs = userStates;
	}

	@Override
	public void setEventSender(StreamSender sender) {
		eventSender = sender;
	}

}
