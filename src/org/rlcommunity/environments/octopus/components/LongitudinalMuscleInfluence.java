package org.rlcommunity.environments.octopus.components;


public class LongitudinalMuscleInfluence extends MuscleInfluence {

    public LongitudinalMuscleInfluence(Node n1, Node n2, double width) {
        super(n1, n2, width);
        this.ActiveConstant = 0.5*Constants.get().getMuscleActive()*width*width/initialLength;
        this.PassiveConstant = 0.5*Constants.get().getMusclePassive()*width*width/initialLength;
        this.DampingConstant = 0.5*Constants.get().getMuscleDamping()*width*width/initialLength;
    }

}
