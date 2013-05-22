package org.rlcommunity.environments.octopus.odeframework;

/**
 * Runge-Kutta 4th order ODE solver
 *
 */
public class RungeKutta4Solver extends ODESolver {

    @Override
    public ODEState solve(ODEEquation eq, ODEState initialState, double time, double timeStep) {
        ODEState k1 = eq.getDeriv(time, initialState);
        ODEState k2 = eq.getDeriv(time + timeStep/2, initialState.addScaled(k1, timeStep/2));
        ODEState k3 = eq.getDeriv(time + timeStep/2, initialState.addScaled(k2, timeStep/2));
        ODEState k4 = eq.getDeriv(time + timeStep, initialState.addScaled(k3, timeStep));
        ODEState sum = k1.addScaled(k2,2).addScaled(k3,2).addScaled(k4,1);
        return initialState.addScaled(sum, timeStep/6);
    }

}
