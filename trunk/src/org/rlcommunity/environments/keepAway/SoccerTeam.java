package org.rlcommunity.environments.keepAway;

import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

import java.util.Collection;
import java.util.Vector;

import org.rlcommunity.environments.keepAway.kaMessages.SoccerTeamFacadeInterface;

import org.rlcommunity.environments.keepAway.players.Attacker;
import org.rlcommunity.environments.keepAway.players.PlayerBase;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;
import org.rlcommunity.environments.keepAway.players.PlayerBase.player_role;

public class SoccerTeam implements SoccerTeamFacadeInterface {
//Team ids
public enum soccerTeamNames{keepers, takers};


soccerTeamNames teamName;

SoccerPitch thePitch=null;
SoccerTeam m_tOpponents=null;

Vector<PlayerInterface> thePlayers=null;
PlayerInterface m_pReceiver=null;

PlayerInterface closestPlayerToBall=null;
double distanceOfClosestPlayer=Double.MAX_VALUE;

private PlayerInterface m_pControllingPlayer;

public soccerTeamNames getTeamName(){
    return  teamName;
}

public SoccerTeam(SoccerPitch thePitch, soccerTeamNames whatTeam){
	this.thePitch=thePitch;
	this.teamName=whatTeam;
	thePlayers=new Vector<PlayerInterface>();
	
	int extraPlayers=0;

        if(teamName==soccerTeamNames.keepers){
	Region homeRegion=new Region(50,5,5,5);
	PlayerBase p=new Attacker(this,new Vector2D(0,1),new Vector2D(),Prm.playerMass,1.0,Prm.playerMaxSpeed*.9d,1.0,Prm.playerRadius,player_role.attacker,homeRegion);
	thePlayers.add(p);
	homeRegion=new Region(10,35,5,5);
	p=new Attacker(this,new Vector2D(0,1),new Vector2D(),Prm.playerMass,1.0,Prm.playerMaxSpeed,1.0,Prm.playerRadius,player_role.attacker,homeRegion);
	thePlayers.add(p);
	homeRegion=new Region(80,35,5,5);
	p=new Attacker(this,new Vector2D(0,1),new Vector2D(),Prm.playerMass,1.0,Prm.playerMaxSpeed,1.0,Prm.playerRadius,player_role.attacker,homeRegion);
	thePlayers.add(p);
	
	for(int i=0;i<extraPlayers;i++){
		homeRegion=new Region(Math.random()*100, Math.random()*50, 5, 5);
		p=new Attacker(this,new Vector2D(0,1),new Vector2D(),Prm.playerMass,1.0,Prm.playerMaxSpeed,1.0,Prm.playerRadius,player_role.attacker,homeRegion);
		thePlayers.add(p);
	}
}else{
	//Takers
	Region homeRegion=new Region(20,30,5,5);
	PlayerBase p=new Attacker(this,new Vector2D(0,1),new Vector2D(),Prm.playerMass,1.0,Prm.playerMaxSpeed,1.0,Prm.playerRadius,player_role.attacker,homeRegion);
	thePlayers.add(p);
	homeRegion=new Region(70,30,5,5);
	p=new Attacker(this,new Vector2D(0,1),new Vector2D(),Prm.playerMass,1.0,Prm.playerMaxSpeed,1.0,Prm.playerRadius,player_role.attacker,homeRegion);
	thePlayers.add(p);
	for(int i=0;i<extraPlayers;i++){
		homeRegion=new Region(Math.random()*100, Math.random()*50, 5, 5);
		p=new Attacker(this,new Vector2D(0,1),new Vector2D(),Prm.playerMass,1.0,Prm.playerMaxSpeed,1.0,Prm.playerRadius,player_role.attacker,homeRegion);
		thePlayers.add(p);
	}
}
	
}

public void setOpponents(SoccerTeam opTeam){
	this.m_tOpponents=opTeam;
}

public String stringSerialize(){
	String response=thePlayers.size()+"_";
	for (PlayerInterface thisPlayer : thePlayers) {
		response+=thisPlayer.stringSerialize()+"_";		
	}
	return response;
}


public void Update() {
	  //this information is used frequently so it's more efficient to 
	  //calculate it just once each frame
	  CalculateClosestPlayerToBall();

	  
	for (PlayerInterface thisPlayer : thePlayers) {
		thisPlayer.Update();
	}
	
}

public final SoccerPitch Pitch() {
	return thePitch;
}

public PlayerInterface getPlayer(int which) {
	return thePlayers.get(which);
}

public int getPlayerCount() {
	return thePlayers.size();
}


public PlayerInterface PlayerClosestToBall() {
	return closestPlayerToBall;
}

//------------------------ CalculateClosestPlayerToBall ------------------
//
//  sets m_iClosestPlayerToBall to the player closest to the ball
//------------------------------------------------------------------------
void CalculateClosestPlayerToBall()
{
  double ClosestSoFar = Double.MAX_VALUE;

	for (PlayerInterface thisPlayer : thePlayers) {

    //calculate the dist. Use the squared value to avoid sqrt
//    double dist = Vec2DDistanceSq((*it)->Pos(), Pitch()->Ball()->Pos());
		double dist = thisPlayer.Pos().distanceToSquared(Pitch().Ball().Pos());
		
	    //keep a record of this value for each player
		thisPlayer.setDistanceToBallSquared(dist);
    
    if (dist < ClosestSoFar)
    {
      ClosestSoFar = dist;
      closestPlayerToBall = thisPlayer;
    }
  }

  distanceOfClosestPlayer = ClosestSoFar;
}

public PlayerInterface getSomeoneElse(PlayerInterface player) {
	Vector<PlayerInterface> candidates=new Vector<PlayerInterface>();
	for (PlayerInterface somePlayer : thePlayers) {
		if(somePlayer!=player)candidates.add(somePlayer);
	}
	
	if(candidates.size()==0)
		return null;
	
	int randIndex=(int)(Math.random()*candidates.size());
	return candidates.get(randIndex);
}

public void resetPositions() {
	for (PlayerInterface somePlayer : thePlayers) {
		somePlayer.setPos(somePlayer.homeRegion().Center());
	}
	
}
public void resetBehavior() {
	for (PlayerInterface somePlayer : thePlayers) {
		somePlayer.resetFSM();
	}
	
}

public PlayerInterface getRandomPlayer() {
	int randomIndex=(int)(Math.random()*(double)thePlayers.size());
	return thePlayers.get(randomIndex);
}


public final Vector<PlayerInterface> getThePlayers(){
	return thePlayers;
}

public void setReceiver(PlayerInterface player) {
	this.m_pReceiver=player;
}

public boolean InControl() {
	return m_pControllingPlayer!=null;
}

public PlayerInterface getControllingPlayer(){return m_pControllingPlayer;}

public void              setControllingPlayer(PlayerInterface thePlayer)
{
  m_pControllingPlayer = thePlayer;

  //rub it in the opponents faces!
  Opponents().lostControl();
}


public boolean  inControl(){if(m_pControllingPlayer!=null)return true; else return false;}
void  lostControl(){m_pControllingPlayer = null;}
SoccerTeam Opponents(){return m_tOpponents;}

public PlayerInterface getClosestOpponent(PlayerInterface thisPlayer) {
	double closest=Double.MAX_VALUE;
	PlayerInterface closestSoFar=null;
	
	for (PlayerInterface somePlayer : Opponents().getThePlayers()) {
		double thisDistance=somePlayer.Pos().distanceTo(thisPlayer.Pos());
		if(thisDistance<closest){
			closestSoFar=somePlayer;
			closest=thisDistance;
		}
	}
	return closestSoFar;}

public Collection<PlayerInterface> getAllPlayersOnBothTeams() {
	Vector<PlayerInterface> allPlayers=new Vector(getThePlayers());
	allPlayers.addAll(Opponents().getThePlayers());
	return allPlayers;
}

}
