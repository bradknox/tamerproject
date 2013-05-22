package org.rlcommunity.environments.octopus.odeframework;

/**
 * Implements the Euler method for solving ODE.
 *
 */
public class EulerSolver extends ODESolver {

    @Override
    public ODEState solve(ODEEquation eq, ODEState initialState, double time, double timeStep) {
        ODEState deriv = eq.getDeriv(time, initialState);
        // Compute new state using Euler's method
        // Yn+1 = Yn + deltaT * dYn/dt
        return initialState.addScaled(deriv, timeStep);
    }

}
