package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.messages.Telegram;

public abstract class State<entityType> {
	public abstract void enter(entityType theEntity);
	public abstract void execute(entityType theEntity);
	public abstract void exit(entityType theEntity);
	public void receiveMessage(entityType theEntity, Telegram theMessage){}
}
