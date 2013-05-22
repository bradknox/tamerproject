package org.rlcommunity.environments.octopus.components;

import org.rlcommunity.environments.octopus.config.*;
import org.rlcommunity.environments.octopus.odeframework.*;

import java.util.*;

public class Arm {
    
    private ArmBase base;
    
    private List<Compartment> compartments;
    private List<Node> upperNodes, lowerNodes;
    private Set<Node> nodes;
    
    private ODEEquationPart equationPart;
    
    public Arm(ArmSpec spec) {
        List<NodePairSpec> nodePairs = spec.getNodePair();
            
        compartments = new ArrayList<Compartment>();
        nodes = new LinkedHashSet<Node>();
        
        base = new ArmBase(nodePairs.get(0));

        upperNodes = new ArrayList<Node>();
        lowerNodes = new ArrayList<Node>();
        upperNodes.add(base.getUpperNode());
        lowerNodes.add(base.getLowerNode());
        
        for (NodePairSpec np: nodePairs.subList(1, nodePairs.size())) {
            upperNodes.add(new UnconstrainedNode(np.getUpper()));
            lowerNodes.add(new UnconstrainedNode(np.getLower()));
        }
        nodes.addAll(upperNodes);
        nodes.addAll(lowerNodes);
        
        /* freeze the node lists/sets */
        upperNodes = Collections.unmodifiableList(upperNodes);
        lowerNodes = Collections.unmodifiableList(lowerNodes);
        nodes = Collections.unmodifiableSet(nodes);
        
        for (int i = 1; i < upperNodes.size(); i++) {
            compartments.add(new Compartment(
                    upperNodes.get(i-1), upperNodes.get(i),
                    lowerNodes.get(i), lowerNodes.get(i-1)
                ));
        }
        
        /* freeze the compartment list */
        compartments = Collections.unmodifiableList(compartments);
        
        /* create equation part view */
        List<ODEEquationPart> parts = new ArrayList<ODEEquationPart>();
        parts.add(base);
        parts.addAll(upperNodes);
        parts.addAll(lowerNodes);
        equationPart = new ODEEquationPartAggregate(parts);
    }
    
    public void updateInfluences() {
        for (Compartment c: compartments) {
            c.updateInfluences();
        }
    }
    
    public ArmBase getBase() {
        return base;
    }
    
    public List<Compartment> getCompartments() {
        return compartments;
    }
    
    public Set<Node> getNodes() {
        return nodes;
    }
    
    public ODEEquationPart asEquationPart() {
        return equationPart;
    }
}
