package org.rlcommunity.environments.octopus.odeframework;

/**
 * <p>An adapter that allows an ODEEquationPart to be viewed as an
 * ODEEquation. Will typically be constructed upon the root of an
 * ODEEquationPart hierarchy. See {@link ODEEquationPart} for information on
 * the concepts involved.</p>
 *
 * <p>Like {@link ODEEquationPartAggregate}, this class should be used by
 * composition, and not by inheritance.</p>
 */
public class PartBasedEquation implements ODEEquation {
    
    private ODEEquationPart part;
    
    public PartBasedEquation(ODEEquationPart part) {
        this.part = part;
    }
    
    public ODEState getDeriv(double time, ODEState state) {
        part.assumeTimeAndState(time, state);
        return part.getODEStateDerivative();
    }
}
