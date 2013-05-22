package org.rlcommunity.environments.octopus.odeframework;

import java.util.*;

/**
 * <p>A "compound" physical entity that consists of several ODEEquationParts.
 * This class itself implements ODEEquationPart, so hierarchical structures
 * can be created.</p>
 *
 * <p>"Physical" classes representing compound entities should use this class
 * by composition, and not by inheritance.</p>
 */
public class ODEEquationPartAggregate implements ODEEquationPart {
    
    private List<ODEEquationPart> parts;

    public ODEEquationPartAggregate(List<ODEEquationPart> parts) {
        this.parts = new ArrayList<ODEEquationPart>(parts);
    }
    
    public void assumeTimeAndState(double time, ODEState state) {
        int i = 0;
        for(ODEEquationPart p:parts) {
            int length = p.getStateLength();
            double partState[] = new double[length];
            System.arraycopy(state.getArray(),i,partState,0,length);
            i += length;
            p.assumeTimeAndState(time, new ODEState(partState));
        }
    }

    public ODEState getCurrentODEState() {
        double[] state = new double[getStateLength()];
        int i = 0;
        for(ODEEquationPart p:parts) {
            int length = p.getStateLength();
            /* Dominated nodes have a 0 length. */
            if(length > 0) {
                ODEState partState = p.getCurrentODEState();
                System.arraycopy(partState.getArray(),0,state,i,length);
                i += length;
            }
        }
        return new ODEState(state);
    }

    /* (non-Javadoc)
     * @see ODEFramework.ODEEquationPart#getODEStateDerivative()
     */
    public ODEState getODEStateDerivative() {
        double[] state = new double[getStateLength()];
        int i = 0;
        for(ODEEquationPart p:parts) {
            int length = p.getStateLength();
            /* Dominated nodes have a 0 length. */
            if(length > 0) {
                ODEState partDeriv = p.getODEStateDerivative();
                System.arraycopy(partDeriv.getArray(),0,state,i,length);
                i += length;
            }
        }
        return new ODEState(state);
    }

    public int getStateLength() {
        int count = 0;
        for(ODEEquationPart p:parts) {
            count += p.getStateLength();
        }
        return count;
    }

}
