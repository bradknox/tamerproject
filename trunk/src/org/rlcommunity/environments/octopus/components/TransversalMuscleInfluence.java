package org.rlcommunity.environments.octopus.components;


public class TransversalMuscleInfluence extends MuscleInfluence {

    public TransversalMuscleInfluence(Node n1, Node n2, double width) {
        super(n1, n2, width);
        
        // Overriden constants
        this.ActiveConstant = Constants.get().getMuscleActive()*this.initialLength;
        this.PassiveConstant = Constants.get().getMusclePassive()*this.initialLength;
        this.DampingConstant = Constants.get().getMuscleDamping()*this.initialLength;
    }

}
