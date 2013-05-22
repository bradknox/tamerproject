package org.rlcommunity.environments.octopus.odeframework;

/**
 * <p>Represents an ODE system without time shifts. Such an equation has the
 * form y'(t) = f(t, y(t)), where y is a vector. Note that although the
 * appears to be first order, higher-order dynamics can be expressed by
 * introducing additional variables.</p>
 */
public interface ODEEquation {

    /**
     * <p>Evaluates the function f(t, y(t)) at the given time t and state y(t).
     * (i.e., computes the derivative y'(t) at the given state and time.) The
     * returned ODEState must have the same size as y.</p>
     *
     * @param t the value of the time parameter t
     * @param y the value of the state parameter y(t)
     * @return the resulting value of f(t, y(t))
     */
    public ODEState getDeriv(double t, ODEState y);
}
