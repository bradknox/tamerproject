package org.rlcommunity.environments.octopus.odeframework;

public abstract class ODESolver {

    /**
     * This method solves the ODE from the initial time to the final time
     * by dividing this interval into timesteps and calling the other
     * solve method for each time step.
     * 
     * @param eq The ODE to solve
     * @param initialState Initial state of the ODE
     * @param initialTime Time at which to start
     * @param finalTime Time at which to stop
     * @param timeStep Hint of the time step to use
     * @return The state of the ODE at finalTime
     */
    public ODEState solve(ODEEquation eq, ODEState initialState, 
            double initialTime, double finalTime, double timeStep) {
        double t;
        ODEState y = initialState;
        for(t = initialTime; t < finalTime; t+= timeStep) {
            y = solve(eq, y, t, timeStep);
        }
        if(t < finalTime) {
            y = solve(eq, y, t, finalTime - t);
        }
        return y;
        
    }

    public abstract ODEState solve(ODEEquation eq, ODEState initialState, 
            double time, double timeStep);
}
