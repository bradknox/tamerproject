package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.messages.Telegram;

public class StateMachine<entityType> {

	private entityType owner;
	
	private State<entityType> currentState;
	private State<entityType> previousState;
	private State<entityType> globalState;
	
	public StateMachine(entityType owner){
		this.owner=owner;
		currentState=null;
		previousState=null;
		globalState=null;
	}
	
	public void update(){
		if(globalState!=null)globalState.execute(owner);
		
		if(currentState!=null)currentState.execute(owner);
	}
	
	public void changeState(State<entityType> newState){
		if(newState==null){
			System.err.println("Tried to change into a null state");
			Thread.dumpStack();
			System.exit(1);
		}
		previousState=currentState;
		
		if(currentState!=null)currentState.exit(owner);
		currentState=newState;
		currentState.enter(owner);
	}
	
	public void revertToPreviousState(){
		changeState(previousState);
	}

	public State<entityType> getCurrentState() {
		return currentState;
	}

	private void  setCurrentState(State<entityType> currentState) {
		this.currentState = currentState;
	}

	public State<entityType> getPreviousState() {
		return previousState;
	}

	private void setPreviousState(State<entityType> previousState) {
		this.previousState = previousState;
	}

	public State<entityType> getGlobalState() {
		return globalState;
	}

	public void setGlobalState(State<entityType> globalState) {
		this.globalState = globalState;
	}

	public entityType getOwner() {
		return owner;
	}

	public void receiveMessage(entityType theEntity, Telegram theMessage) {
		if(currentState!=null)currentState.receiveMessage(theEntity,theMessage);
		if(globalState!=null)globalState.receiveMessage(theEntity,theMessage);
	}
	
	
	
}
