package org.rlcommunity.environments.octopus.odeframework;

/**
 * <p>A vector of state variable (or derivative) values for an ODE
 * system. Each ODEState instance is backed by a double array. Instances of
 * this class are not immutable, as the backing array can be obtained
 * using {@link #getArray} and modified.</p>
 */
public class ODEState {
    private double[] state;
    
    public ODEState(double[] state) {
        this.state = state;
    }
    
    /**
     * <p>Returns the backing array for this ODEState. The returned reference
     * is a 'live' reference, not a copy.</p>
     *
     * @return the backing array for this ODEState.
     */
    public double[] getArray() {
        return state;
    }

    /**
     * <p>Returns a new ODEState equal to the sum of this ODEState, plus
     * the given state multiplied by the given scale factor. This ODEState
     * is not changed.</p>
     */
    public ODEState addScaled(ODEState s, double scale) {
        double[] temp = state.clone();
        for(int i = 0; i < state.length; i++) {
            temp[i] += scale*s.getArray()[i];
        }
        return new ODEState(temp);
    }
}
