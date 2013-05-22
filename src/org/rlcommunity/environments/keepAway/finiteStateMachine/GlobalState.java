package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.messages.Telegram;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;
import org.rlcommunity.environments.keepAway.Prm;
import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

public class GlobalState extends State<PlayerInterface> {
	boolean debugThis=false;
	
	static GlobalState instance=null;
	static{
		instance=new GlobalState();
	}
	
	public static GlobalState Instance(){return instance;}
	
	@Override
	public
	void enter(PlayerInterface player) {

	}

//	@Override
	public void execute(PlayerInterface player) {

	}
	@Override
	public	void exit(PlayerInterface player) {
	}

	@Override
	public void receiveMessage(PlayerInterface theEntity, Telegram theMessage) {
		//Receive ball
		if(theMessage.type()==TelegramTypes.receivePass)theEntity.GetFSM().changeState(ReceivePass.Instance());
	}

}
