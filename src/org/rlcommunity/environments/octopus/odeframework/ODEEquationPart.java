package org.rlcommunity.environments.octopus.odeframework;

/**
 * <p>A physical entity whose state is defined by a subset of an ODE system's
 * state variables. The entity may be simple (indivisible) or compound. Using
 * {@link ODEEquationPart aggregate}, entities may be hierarchically nested,
 * with the root representing the entire system.</p>
 *
 * <p>This interface, together with {@link PartBasedEquation}, allow
 * "physical" class models, where objects directly represent physical
 * entities, to be viewed as ODEEquations and thus used with ODE solvers.
 * While ODE solvers might require the computation of derivatives at arbitrary
 * times and states, entities in physical class models might not be able
 * to compute derivatives at any state other than the current system state, 
 * particularly if they depend on the states of other entities in
 * the system.</p>
 *
 * <p>To work around this limitation, the notion of an assumed time and state
 * is introduced. This is the time and state that an ODEEquationPart must
 * assume upon calls to {@link #getODEStateDerivative}, and is set with
 * {@link #assumeTimeAndState}. It is guaranteed upon each call to
 * getODEStateDerivative that the entire ODE system will be assuming the same
 * time and state given in the last call to assumeTimeAndState.</p>
 */
public interface ODEEquationPart {

    /**
     * Returns how many state variables this object expects.
     * @return The number of doubles in the state of this object.
     */
    public int getStateLength();
    
    /**
     * <p>Causes the object to assume the time and state given.
     * This method is used for "priming" the object for a subsequent
     * call to {@link getODEStateDerivative} during a solution step. It can
     * also be used to update the "real" state of the object after a solution
     * step, in which case the time parameter is insignificant.</p>
     *
     * @param time the assumed time
     * @param state the assumed state
     */
    public void assumeTimeAndState(double time, ODEState state);
    
    /**
     * <p>Returns the current (assumed) state of this ODEEquationPart.</p>
     *
     * @return The current state as an array of double values.
     */
    public ODEState getCurrentODEState();
    
    /**
     * <p>Returns the values of the state variable derivatives, evaluated
     * at the current assumed time and state.</p>
     *
     * @return the state variable derivatives at the current assumed
     *         a time and state
     */
    public ODEState getODEStateDerivative();
}
