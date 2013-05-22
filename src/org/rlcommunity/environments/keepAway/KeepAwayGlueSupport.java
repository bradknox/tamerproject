package org.rlcommunity.environments.keepAway;

import org.rlcommunity.environments.keepAway.kaMessages.KAHistoricStateResponse;
import org.rlcommunity.environments.keepAway.kaMessages.KAStateResponse;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

public class KeepAwayGlueSupport {

    private static KeepAwayGlueSupport instance = null;

    public static KeepAwayGlueSupport getInstance() {
        if (KeepAwayGlueSupport.instance == null) {
            throw new IllegalStateException("Someone asked for KeepAwayGlueSupport object, which is singleton, but it has not been instantiated yet!");
        }
        return instance;
    }
    private SoccerPitch m_pPitch = null;
    private KeepAway m_kEnv = null;
    private Action m_CurrentAction = null;
    private PlayerInterface m_PlayerToAct = null;
    private boolean visualizerNameRequested = false;

    public KeepAwayGlueSupport(KeepAway p_kEnv) {
        this.m_kEnv = p_kEnv;
        this.m_pPitch = null;
        if (KeepAwayGlueSupport.instance != null) {
            throw new IllegalStateException("New KeepAwayGlueSupport object was created but it's singleton and has already been created");
        }
        KeepAwayGlueSupport.instance = this;
    }

    public boolean playerReadyToAct() {
        return m_PlayerToAct!=null;
    }
    public PlayerInterface popPlayerToAct() {
        PlayerInterface thePlayer = m_PlayerToAct;
        m_PlayerToAct = null;
        return thePlayer;
    }

    public void setPlayerToAct(PlayerInterface thePlayer) {
        m_PlayerToAct = thePlayer;
    }

    public void init(SoccerPitch p_pPitch) {
        this.m_pPitch = p_pPitch;
    }

    public Observation makeObservation() {
        PlayerInterface theControllingPlayer = popPlayerToAct();
        if (theControllingPlayer == null) {
            throw new IllegalStateException("in makeObservaton the theControllingPlayer was null");
        }

        //Right now, just the ball position
        Observation o = new Observation(1, 2);
        SoccerBall theBall = m_pPitch.getBall();
        if (theBall == null) {
            throw new IllegalStateException("in makeObservaton the ball was null");
        }

        Vector2D oldPos = theBall.OldPos();
        if (oldPos == null) {
            return o;
        }
        o.doubleArray[0] = theBall.OldPos().x;
        o.doubleArray[1] = theBall.OldPos().y;
        o.intArray[0] = theControllingPlayer.ID();

        return o;
    }

    public Action peekAction() {
        return m_CurrentAction;
    }

    public Action popAction() {
        Action theAction = m_CurrentAction;
        m_CurrentAction = null;
        return theAction;
    }

    public void setAction(Action theAction) {
        m_CurrentAction = theAction;
    }

    public String env_message(String theMessage) {
        EnvironmentMessages theMessageObject;
        try {
            theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent mountain Car a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.getTheMessageType() == EnvMessageType.kEnvQueryVisualizerName.id()) {
            visualizerNameRequested = true;
            if (m_pPitch != null) {
                m_pPitch.shouldLog = true;
            }
        }

        if (theMessageObject.canHandleAutomatically(m_kEnv)) {
            String theResponseString = theMessageObject.handleAutomatically(m_kEnv);
            return theResponseString;
        }

//		If it wasn't handled automatically, maybe its a custom Mountain Car Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();

            if (theCustomType.equals("GETKASTATE")) {
                //It is a request for the state

                if (m_pPitch == null) {
                    return "";
                }

                KAStateResponse theResponseObject = new KAStateResponse(m_pPitch);

                return theResponseObject.makeStringResponse();
            }
            if (theCustomType.equals("GETKASTATEHISTORY")) {
                //It is a request for the state

                if (m_pPitch == null) {
                    return "";
                }

                KAHistoricStateResponse theResponseObject = new KAHistoricStateResponse(m_pPitch.vLog().latestLogs);
//				System.out.println("Sent: "+P.vLog().latestLogs.size()+" messages");
                m_pPitch.vLog().newLogs();

                return theResponseObject.makeStringResponse();
            }

        }


        System.err.println("Got a message that the soccer env can't handle");
        return null;
    }

    public boolean visualizerNameRequested() {
        return visualizerNameRequested;
    }
}
