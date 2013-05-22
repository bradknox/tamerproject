package org.rlcommunity.environments.keepAway.messages;

import org.rlcommunity.environments.keepAway.finiteStateMachine.TelegramTypes;

public class Telegram {
	private TelegramTypes m_Type;
	
	public Telegram(TelegramTypes telegramType){
		m_Type=telegramType;
	}
	
	public TelegramTypes type(){
		return m_Type;
	}
	
}
