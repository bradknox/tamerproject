package org.rlcommunity.environments.keepAway;

public class Prm {

    public static final double MaxShootingForce = 3.5d;
    public static final double PlayerKickingDistanceSq = 1.0;
    public static final double PlayerInTargetRangeSq = 5.0;
    public static final double hasBallMinDistanceSq = 2.0;
    public static final double holdingBallSpeed = 1.0;
    public static final int kickFrequency = 2;
    public static final double StoppingForce = .5;
    public static final double MaxDribbleForce = 1.5;
    //this doesn't work
    public static final boolean bNonPenetrationConstraint = false;
    public static final double playerRadius = .2d;
//should be negative
    static double Friction = -.1d;
    public static double PlayerKickingAccuracy = .9d;
    static double BallSize = 1.0d;
    static double BallMass = 1.0d;
//?
    public static double SeparationCoefficient = .01;
//??
    public static double ViewDistance = 100;
//??
    public static double PlayerMaxTurnRate = 3.0d;
    public static double playerMass = 5.0d;
    public static double playerMaxSpeed = .8d;
}
