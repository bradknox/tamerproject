package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.KeepAwayGlueSupport;
import org.rlcommunity.environments.keepAway.messages.ReceivePassTelegram;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;
import org.rlcommunity.environments.keepAway.Prm;
import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;
import org.rlcommunity.rlglue.codec.types.Action;

public class WaitForAgentChoice extends State<PlayerInterface> {

    boolean debugThis = true;
    static WaitForAgentChoice instance = null;

    static {
        instance = new WaitForAgentChoice();
    }

    public static WaitForAgentChoice Instance() {
        return instance;
    }

    @Override
    public void enter(PlayerInterface player) {
        //let the team know this player is controlling
        player.Team().setControllingPlayer(player);

        //the player can only make so many kick attempts per second.
        if (!player.isReadyForNextKick()) {
            player.GetFSM().changeState(ChaseBall.Instance());
            if (debugThis) {
                System.out.println("Player: " + player.ID() + " set to " + player.GetFSM().getCurrentState().getClass());
            }
        } else {
            KeepAwayGlueSupport.getInstance().setPlayerToAct(player);
        }
    }

    private void stopBall(PlayerInterface player) {
//This would be a condition where it would be sensible to stop the ball
        //if(player.Ball().Velocity().LengthSq()>Prm.holdingBallSpeed){
        System.out.println("Trying to stop the ball!");
        Vector2D randomSmallVector = new Vector2D(Utilities.RandomClamped(), Utilities.RandomClamped());
//The illusion of stopping the ball
        if (player.isReadyForNextKick()) {
            player.Ball().setPos(randomSmallVector.add(player.Pos()));
            Vector2D BallTarget = player.Ball().Pos().addToCopy(player.Ball().Velocity());
            player.Kick(BallTarget, Prm.StoppingForce);
        } else {
            System.err.println("RL Version of stop ball was called when agent was not ready for kick, shouldn't happen");
        }
                //change state   
        player.GetFSM().changeState(ChaseBall.Instance());
    }

    private void dribbleBall(PlayerInterface player) {
        player.GetFSM().changeState(DribbleBall.Instance());
    }

    private void kickBall(PlayerInterface player) {
        //calculate the dot product of the vector pointing to the ball
        //and the player's heading
        Vector2D ToBall = player.Ball().Pos().subtractToCopy(player.Pos());
        double dot = player.Heading().Dot(ToBall.normalizeToCopy());

        //the dot product is used to adjust the shooting force. The more
        //directly the ball is ahead, the more forceful the kick
        double power = Prm.MaxShootingForce * dot;

        //Try to setup a pass
        PlayerInterface receiver = player.Team().getSomeoneElse(player);

        if (receiver != null) {
            Vector2D BallTarget = receiver.Pos().subtractToCopy(player.Ball().Pos());
            //		  BallTarget=AddNoiseToKick(player.Ball().Pos(), BallTarget);
            if (player.isReadyForNextKick()) {
                receiver.receiveMessage(new ReceivePassTelegram());
                player.Kick(BallTarget, Prm.MaxShootingForce);
            }
        } else {
            System.err.println("In RL-kickBall, no receivers?");
            Vector2D KickDirection = new Vector2D(Math.random() - .5d, Math.random() - .5d);
            if (player.isReadyForNextKick()) {
                player.Kick(KickDirection, power);
            } else {
                System.err.println("RL Version ofkickBall was called when agent was not ready for kick, shouldn't happen");
            }
        }
        
                //change state   
        player.GetFSM().changeState(ChaseBall.Instance());

    }

//	@Override
    public void execute(PlayerInterface player) {
        //Get the action
        Action theAction = KeepAwayGlueSupport.getInstance().popAction();
        if (theAction == null) {
            throw new IllegalStateException("Execute was called on WaitForAgentChoice but there is no action in the hopper!");
        }

        int primitiveAction = theAction.intArray[0];
        if(primitiveAction==0)stopBall(player);
        if(primitiveAction==1)dribbleBall(player);
        if(primitiveAction==2)kickBall(player);

        if (debugThis) {
            System.out.println("Player: " + player.ID() + " set to " + player.GetFSM().getCurrentState().getClass());
        }

    }

    @Override
    public void exit(PlayerInterface player) {
    }

    //----------------------------- AddNoiseToKick --------------------------------
    //
    //  this can be used to vary the accuracy of a player's kick. Just call it 
    //  prior to kicking the ball using the ball's position and the ball target as
    //  parameters.
    //-----------------------------------------------------------------------------
    Vector2D AddNoiseToKick(Vector2D BallPos, Vector2D BallTarget) {

        double displacement = (Math.PI - Math.PI * Prm.PlayerKickingAccuracy) * Utilities.RandomClamped();

        Vector2D toTarget = BallTarget.subtractToCopy(BallPos);
        toTarget.RotateAroundOrigin(displacement);

        return toTarget.add(BallPos);
    }
}
