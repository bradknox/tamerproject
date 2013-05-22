package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.SoccerTeam.soccerTeamNames;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;

public class ChaseBall extends State<PlayerInterface> {

    boolean debugThis = false;
    static ChaseBall instance = null;

    static {
        instance = new ChaseBall();
    }

    @Override
    public void enter(PlayerInterface player) {
        if (debugThis) {
            System.out.println("\t\t------ chasing ball on");
        }
        player.Steering().SetTarget(player.Ball().Pos());
        player.Steering().SeekOn();
        if (debugThis) {
            System.out.println("\t\tPlayer: " + player.ID() + "------ SeekOn");
        }

    }

    @Override
    public void execute(PlayerInterface player) {
        //if the ball is within kicking range the player changes state to KickBall.
        if (player.BallWithinKickingRange()) {
            if (player.Team().getTeamName() == soccerTeamNames.takers) {
                player.GetFSM().changeState(KickBall.Instance());
            } else {
                player.GetFSM().changeState(WaitForAgentChoice.Instance());
            }
            if (debugThis) {
                System.out.println("Player: " + player.ID() + " set to " + player.GetFSM().getCurrentState().getClass());
            }

            return;
        }

        //if the player is the closest player to the ball then he should keep
        //chasing it
        if (player.isClosestTeamMemberToBall()) {
            player.Steering().SetTarget(player.Ball().Pos());
            return;
        }


        //if the player is not closest to the ball anymore, he should return back
        //to his home region and wait for another opportunity
        player.GetFSM().changeState(ReturnToHomeSpot.Instance());
        if (debugThis) {
            System.out.println("Player: " + player.ID() + " set to " + player.GetFSM().getCurrentState().getClass());
        }
//		  player.GetFSM().setState(Wait.Instance());

    }

    @Override
    public void exit(PlayerInterface player) {
        player.Steering().SeekOff();
        if (debugThis) {
            System.out.println("\t\tPlayer: " + player.ID() + "------ SeekOff");
        }

    }

    public static ChaseBall Instance() {
        return instance;
    }
}
