package org.rlcommunity.environments.keepAway.messages;

import org.rlcommunity.environments.keepAway.finiteStateMachine.TelegramTypes;

public class ReceivePassTelegram extends Telegram {
	
	public ReceivePassTelegram(){
		super(TelegramTypes.receivePass);
	}
}
