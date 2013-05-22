package org.rlcommunity.environments.keepAway;

import java.util.Vector;

import org.rlcommunity.environments.keepAway.players.PlayerInterface;

import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

public class SoccerPitch {

    public SoccerBall theBall;
    public SoccerTeam theKeepers;
    public SoccerTeam theTakers;
    //Logger so that rlViz can draw the whole game
    public boolean shouldLog = false;
    VizLogger vLog = null;
    double fieldWidth = 100;
    double fieldHeight = 50;
    //container for the boundary walls
    Vector<Wall2D> m_vecWalls;

    //defines the dimensions of the playing area
    Region m_pPlayingArea;

    //the playing field is broken up into regions that the team
    //can make use of to implement strategies.
    Vector<Region> m_Regions;

    //true if a goal keeper has possession
    boolean m_bGoalKeeperHasBall;

    //true if the game is in play. Set to false whenever the players
    //are getting ready for kickoff
    boolean gameActive = false;
    //set true to pause the motion
    boolean m_bPaused;

    //local copy of client window dimensions
//	int m_cxClient, m_cyClient;
    void TogglePause() {
        m_bPaused = !m_bPaused;
    }

    boolean Paused() {
        return m_bPaused;
    }

    boolean GoalKeeperHasBall() {
        return m_bGoalKeeperHasBall;
    }

    void SetGoalKeeperHasBall(boolean b) {
        m_bGoalKeeperHasBall = b;
    }

    Region PlayingArea() {
        return m_pPlayingArea;
    }

    public Vector<Wall2D> Walls() {
        return m_vecWalls;
    }

    SoccerBall Ball() {
        return theBall;
    }

    Region GetRegionFromIndex(int idx) {
        assert ((idx > 0) && (idx < m_Regions.size()));

        return m_Regions.get(idx);
    }

    boolean GameOn() {
        return gameActive;
    }

    void SetGameOn() {
        gameActive = true;
    }

    void SetGameOff() {
        gameActive = false;
    }

    //------------------------------- ctor -----------------------------------
    //------------------------------------------------------------------------
    SoccerPitch(double fieldWidth, double fieldHeight) {
        this.fieldHeight = fieldHeight;
        this.fieldWidth = fieldWidth;

        m_bPaused = false;
        m_bGoalKeeperHasBall = false;
        m_Regions = new Vector<Region>();
        gameActive = true;

        m_vecWalls = new Vector<Wall2D>();

        //define the playing area
        m_pPlayingArea = new Region(0, 0, 100, 50);

        vLog = new VizLogger();

        //create the regions  
        //bt Delete region code

        //create the goals
        //bt Deleted goal code

        //create the soccer ball
        theBall = new SoccerBall(new Vector2D(), Prm.BallSize, Prm.BallMass, m_vecWalls, m_pPlayingArea);
        placeBallAtCenter();


        //create the teams 
        theKeepers = new SoccerTeam(this, SoccerTeam.soccerTeamNames.keepers);
        theTakers = new SoccerTeam(this, SoccerTeam.soccerTeamNames.takers);

        //make sure each team knows who their opponents are
        theKeepers.setOpponents(theTakers);

        theTakers.setOpponents(theKeepers);

        //create the walls
        Vector2D TopLeft = new Vector2D(m_pPlayingArea.Left(), m_pPlayingArea.Top());
        Vector2D TopRight = new Vector2D(m_pPlayingArea.Right(), m_pPlayingArea.Top());
        Vector2D BottomRight = new Vector2D(m_pPlayingArea.Right(),
                m_pPlayingArea.Bottom());
        Vector2D BottomLeft = new Vector2D(m_pPlayingArea.Left(),
                m_pPlayingArea.Bottom());
        m_vecWalls.add(new Wall2D(BottomLeft, BottomRight));
        m_vecWalls.add(new Wall2D(TopLeft, BottomLeft));
        m_vecWalls.add(new Wall2D(TopLeft, TopRight));
        m_vecWalls.add(new Wall2D(TopRight, BottomRight));

    }

    //----------------------------- Update -----------------------------------
    //
    //  this demo works on a fixed frame rate (60 by default) so we don't need
    //  to pass a time_elapsed as a parameter to the game entities
    //------------------------------------------------------------------------
    static int tick = 0;

    void Update() {
        if (m_bPaused) {
            return;
        }

        //update the balls
        theBall.Update();

        //update the teams
        theKeepers.Update();
        theTakers.Update();
        if (shouldLog) {
            vLog.addToLog(this);
        }

        //Do a variety of little checks

        boolean takerCloseEnoughToBall = theTakers.distanceOfClosestPlayer < Prm.hasBallMinDistanceSq;
        boolean ballMovingSlowEnough = theBall.Velocity().LengthSq() < Prm.holdingBallSpeed;

        boolean keeperCloseEnoughToBall = theKeepers.distanceOfClosestPlayer < Prm.hasBallMinDistanceSq;
        if (takerCloseEnoughToBall && ballMovingSlowEnough) {
            gameActive = false;
        }

        //if a goal has been detected reset the pitch ready for kickoff
        //bt::todo change this to an out of bounds check
        if (false) {
            gameActive = false;

        //get the teams ready for kickoff
        //bt: come back to this later
        //	    m_pRedTeam.GetFSM().ChangeState(PrepareForKickOff::Instance());
        //    m_pBlueTeam.GetFSM().ChangeState(PrepareForKickOff::Instance());
        }
    }

    void placeBallAtPosition(Vector2D thePosition) {
        theBall.PlaceAtPosition(thePosition);
    }

    void placeBallAtCenter() {
        Vector2D centerPosition = new Vector2D(fieldWidth / 2.0d, fieldHeight / 2.0d);
        placeBallAtPosition(centerPosition);
    }
    //------------------------------ Render ----------------------------------
    //------------------------------------------------------------------------
    boolean Render() {
        return true;
    }

    public String toString() {
        return theBall.toString();
    }

    public SoccerBall getBall() {
        return theBall;
    }

    public void resetPositions() {
        placeBallAtCenter();
        theKeepers.resetPositions();
        theTakers.resetPositions();

    }

    public void giveBallToRandomKeeper() {
        PlayerInterface somePlayer = theKeepers.getRandomPlayer();
        placeBallAtPosition(somePlayer.Pos());
    }

    public VizLogger vLog() {
        return vLog;
    }

    void resetBehaviors() {
        theKeepers.resetBehavior();
        theTakers.resetBehavior();
    }

    void reset() {
        resetPositions();
        resetBehaviors();
        giveBallToRandomKeeper();
        gameActive = true;

    }
}
