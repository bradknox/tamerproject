package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.SoccerTeam.soccerTeamNames;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;

public class ReceivePass extends State<PlayerInterface> {

    boolean debugThis = true;
    static ReceivePass instance = null;

    static {
        instance = new ReceivePass();
    }

    @Override
    public void enter(PlayerInterface player) {
        if (debugThis) {
            System.out.println(getClass().getName() + "::enter called for player: " + player.ID());
        }

        //let the team know this player is receiving the ball
        player.Team().setReceiver(player);

        //this player is also now the controlling player
        player.Team().setControllingPlayer(player);

        //there are two types of receive behavior. One uses arrive to direct
        //the receiver to the position sent by the passer in its telegram. The
        //other uses the pursuit behavior to pursue the ball. 
        //This statement selects between them dependent on the probability
        //ChanceOfUsingArriveTypeReceiveBehavior, whether or not an opposing
        //player is close to the receiving player, and whether or not the receiving
        //player is in the opponents 'hot region' (the third of the pitch closest
        //to the opponent's goal

        //bt -- changing this to just use pursuit
        player.Steering().PursuitOn();
        player.Steering().SetTarget(player.Ball().Pos());

    }

    @Override
    public void execute(PlayerInterface player) {
        if (debugThis) {
            System.out.println(getClass().getName() + "::execute called for player: " + player.ID());
        }

        //if the ball comes close enough to the player or if his team lose control
        //he should change state to chase the ball
        if (!player.Team().InControl()) {
            if (debugThis) {
                System.out.println("\tChanging to Chase because we're not in control or it's too far");
            }
            player.GetFSM().changeState(ChaseBall.Instance());
            return;
        }

        //if the player has 'arrived' at the steering target he should wait and
        //turn to face the ball
        if (player.AtTarget()) {
            player.Steering().ArriveOff();
            player.Steering().PursuitOff();
            player.TrackBall();

            if (player.BallWithinKickingRange()) {
                if (player.Team().getTeamName() == soccerTeamNames.takers) {
                    player.GetFSM().changeState(KickBall.Instance());
                } else {
                    player.GetFSM().changeState(WaitForAgentChoice.Instance());
                }
            } else {
                player.GetFSM().changeState(ChaseBall.Instance());
            }
        }
    }

    @Override
    public void exit(PlayerInterface player) {
        if (debugThis) {
            System.out.println(getClass().getName() + "::exit called for player: " + player.ID());
        }

        player.Steering().ArriveOff();
        player.Steering().PursuitOff();

        player.Team().setReceiver(null);
    }

    public static ReceivePass Instance() {
        return instance;
    }
}
