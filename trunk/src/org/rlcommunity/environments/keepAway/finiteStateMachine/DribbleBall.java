package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.Prm;
import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;

public class DribbleBall extends State<PlayerInterface> {

    boolean debugThis = true;
    final static DribbleBall instance = new DribbleBall();

    public static DribbleBall Instance() {
        return instance;
    }

    @Override
    public void enter(PlayerInterface player) {
        //let the team know this player is controlling
        player.Team().setControllingPlayer(player);
        if (debugThis) {
            System.out.println(player.ID() + " entering dribble");
        }
    }

    @Override
    public void execute(PlayerInterface player) {
        //Default, in case we have no place to go
        Vector2D dribbleDirection = Utilities.randomClampedVector();

        PlayerInterface closestOpponent = player.Team().getClosestOpponent(player);
        if (closestOpponent != null) {
            //Get the vector to this opponent
            dribbleDirection = closestOpponent.Pos().subtractToCopy(player.Pos());
            dribbleDirection.multiply(-1.0d);
        }

        double dot = dribbleDirection.Dot(player.Heading());

        //if the ball is between the player and the home goal, it needs to swivel
        // the ball around by doing multiple small kicks and turns until the player 
        //is facing in the correct direction
        if (dot < 0) {
            //the player's heading is going to be rotated by a small amount (Pi/4) 
            //and then the ball will be kicked in that direction
            Vector2D direction = player.Heading();

            //calculate the sign (+/-) of the angle between the player heading and the 
            //facing direction of the goal so that the player rotates around in the 
            //correct direction
            double QuarterPi = Math.PI / 4.0d;
            double angle = QuarterPi * -1 *
                    dribbleDirection.Sign(player.Heading());

            direction.RotateAroundOrigin(angle);

            //this value works well when the player is attempting to control the
            //ball and turn at the same time
            double KickingForce = 0.8;

            player.Ball().Kick(direction, KickingForce);
        } //kick the ball down the field
        else {
            player.Kick(dribbleDirection, Prm.MaxDribbleForce);
        }

        //the player has kicked the ball so he must now change state to follow it
        player.GetFSM().changeState(ChaseBall.Instance());

        return;
    }

    @Override
    public void exit(PlayerInterface player) {

    }
}
