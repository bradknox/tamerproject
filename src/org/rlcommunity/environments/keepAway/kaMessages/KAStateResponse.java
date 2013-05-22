package org.rlcommunity.environments.keepAway.kaMessages;

import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

import java.util.StringTokenizer;
import java.util.Vector;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;
import org.rlcommunity.environments.keepAway.SoccerPitch;
import org.rlcommunity.environments.keepAway.Wall2D;

public class KAStateResponse extends AbstractResponse {
Vector2D ballPosition;
Vector<Wall2D> theWalls;
SoccerTeamFacadeInterface Keepers;
SoccerTeamFacadeInterface Takers;

public static KAStateResponse makeKAStateResponseFromPayLoad(String thePayLoadString){
	KAStateResponse theResponse=new KAStateResponse();
	fillKAStateResponseFromPayload(thePayLoadString, theResponse);
	return theResponse;
}

private static void fillKAStateResponseFromPayload(String thePayLoadString, KAStateResponse theResponse){
	StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");
	
	String ballPositionString=stateTokenizer.nextToken();
	theResponse.ballPosition=new Vector2D(ballPositionString);
	
	theResponse.theWalls=new Vector<Wall2D>();
	int wallCount=Integer.parseInt(stateTokenizer.nextToken());
	for(int i=0;i<wallCount;i++){
		String nextWallString=stateTokenizer.nextToken();
		theResponse.theWalls.add(new Wall2D(nextWallString));
	}
	
	String keeperString=stateTokenizer.nextToken();
	theResponse.Keepers=new SoccerTeamFacade(keeperString);
	String takerString=stateTokenizer.nextToken();
	theResponse.Takers=new SoccerTeamFacade(takerString);

}

private KAStateResponse(){
}
public KAStateResponse(SoccerPitch p) {
	this(p.getBall().Pos(),p.Walls(),p.theKeepers,p.theTakers);
}

	private KAStateResponse(Vector2D ballPosition, Vector<Wall2D> theWalls, SoccerTeamFacadeInterface Keepers,SoccerTeamFacadeInterface Takers) {
		

		this.ballPosition=ballPosition;
		if(ballPosition==null)this.ballPosition=new Vector2D();
		
		this.theWalls=theWalls;
		if(theWalls==null)this.theWalls=new Vector<Wall2D>();

		this.Keepers=Keepers;
		this.Takers=Takers;
	}
	
	public static String makePayLoadFor(SoccerPitch p){
		return makePayLoadFor(p.getBall().Pos(),p.Walls(),p.theKeepers,p.theTakers);
	}
	
	private static String makePayLoadFor(Vector2D ballPosition, Vector<Wall2D> theWalls, SoccerTeamFacadeInterface Keepers,SoccerTeamFacadeInterface Takers){
		StringBuffer b=new StringBuffer();
		b.append(ballPosition.stringSerialize());
		b.append(":");
		b.append(theWalls.size());
		b.append(":");
		for(int i=0;i<theWalls.size();i++){
			b.append(theWalls.get(i).stringSerialize());
			b.append(":");
		}
		
		b.append(Keepers.stringSerialize());
		b.append(":");
		b.append(Takers.stringSerialize());

		return b.toString();
	}

	private String makePayLoadForThis(){
		return makePayLoadFor(ballPosition,theWalls,Keepers,Takers);
	}
	
	
	public KAStateResponse(String responseMessage) throws NotAnRLVizMessageException {
		GenericMessage theGenericResponse = new GenericMessage(responseMessage);
		String thePayLoadString=theGenericResponse.getPayLoad();
		
		fillKAStateResponseFromPayload(thePayLoadString, this);

	}

	public String makeStringResponse() {
		StringBuffer theResponseBuffer= new StringBuffer();
		theResponseBuffer.append("TO=");
		theResponseBuffer.append(MessageUser.kBenchmark.id());
		theResponseBuffer.append(" FROM=");
		theResponseBuffer.append(MessageUser.kEnv.id());
		theResponseBuffer.append(" CMD=");
		theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
		theResponseBuffer.append(" VALTYPE=");
		theResponseBuffer.append(MessageValueType.kStringList.id());
		theResponseBuffer.append(" VALS=");
		theResponseBuffer.append(makePayLoadForThis());
		
		return theResponseBuffer.toString();
	}

	public Vector2D getBallPosition() {
		return ballPosition;
	}

	public Vector<Wall2D> getTheWalls() {
		return theWalls;
	}

	public SoccerTeamFacadeInterface getKeepers() {
		return Keepers;
	}
	public SoccerTeamFacadeInterface getTakers() {
		return Takers;
	}
	
	

}
