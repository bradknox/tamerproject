package org.rlcommunity.environments.octopus;

import java.net.URL;
import org.rlcommunity.environments.octopus.odeframework.ODESolver;
import org.rlcommunity.environments.octopus.odeframework.RungeKutta4Solver;
import org.rlcommunity.environments.octopus.odeframework.ODEState;
import org.rlcommunity.environments.octopus.protocol.*;



import org.rlcommunity.environments.octopus.config.ChoiceSpec;
import org.rlcommunity.environments.octopus.config.EnvSpec;
import org.rlcommunity.environments.octopus.config.FoodSpec;
import org.rlcommunity.environments.octopus.config.FoodTaskDef;
import org.rlcommunity.environments.octopus.config.NewConfig;
import org.rlcommunity.environments.octopus.config.ObjectiveSpec;
import org.rlcommunity.environments.octopus.config.SequenceSpec;
import org.rlcommunity.environments.octopus.config.TargetSpec;
import org.rlcommunity.environments.octopus.config.TargetTaskDef;
import org.rlcommunity.environments.octopus.config.TaskDef;
import org.rlcommunity.environments.octopus.components.Arm;
import org.rlcommunity.environments.octopus.components.ArmBase;
import org.rlcommunity.environments.octopus.components.Compartment;
import org.rlcommunity.environments.octopus.components.Constants;
import org.rlcommunity.environments.octopus.components.EnvironmentSimulator;
import org.rlcommunity.environments.octopus.components.Food;
import org.rlcommunity.environments.octopus.components.Mouth;
import org.rlcommunity.environments.octopus.components.Target;
import org.rlcommunity.environments.octopus.visualizer.OctopusVisualizer;
import java.util.*;
import org.rlcommunity.environments.octopus.messages.OctopusStateResponse;
import org.rlcommunity.environments.octopus.messages.OctopusCoreDataResponse;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;
import rlVizLib.Environments.EnvironmentBase;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.RLVizVersion;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;
import rlVizLib.messaging.interfaces.HasImageInterface;
import rlVizLib.general.hasVersionDetails;

public class Octopus extends EnvironmentBase implements HasAVisualizerInterface, HasImageInterface {

    boolean useDiscreteActions = true;
    private Arm arm;
    private Set<Target> targets;
    private Set<Food> allFood,  uneatenFood;
    private Mouth mouth;
    private TaskTracker taskTracker;
    private EnvironmentSimulator envSimulator;
    private ODESolver solver;
    private ODEState initialState;
    private List<EnvironmentObserver> observers;
    ODEState currentState = null;
    public boolean useLocalViz = true;

    public static void main(String[] args) {
        EnvironmentLoader L = new EnvironmentLoader(new Octopus());
        L.run();
    }

    public Octopus() {
        NewConfig newC = new NewConfig();
        if (Constants.get() == null) {
            Constants.init(newC.getConstants());
        }
        initFromSpec(newC.getEnvironment());
    }
    
        public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());

        return p;
    }


    public void initFromSpec(EnvSpec spec) {
        arm = new Arm(spec.getArm());
        targets = new HashSet<Target>();
        allFood = new HashSet<Food>();
        uneatenFood = new HashSet<Food>();
        mouth = null;

        taskTracker = makeTaskTracker(spec.getTask());

        envSimulator = new EnvironmentSimulator(arm, allFood);

        initialState = envSimulator.getODEState();
        solver = new RungeKutta4Solver();

        observers = new ArrayList<EnvironmentObserver>();
    }

    public Octopus(EnvSpec spec) {
        initFromSpec(spec);
    }

    public String env_init() {
//        if (useLocalViz) {
//            DisplayFrame display = new DisplayFrame(this);
//        }


        return makeTaskSpec();
    }

    private String makeTaskSpec() {
        double minXPos = -12.0d;
        double maxXPos = 12.0d;

        double minYPos = -12.0d;
        double maxYPos = 6.0d;

        double minVel = -1.0d;
        double maxVel = 1.0d;
        double minAction = 0.0d;
        double maxAction = 1.0d;


        TaskDescription ts = getTaskSpec();

        TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
        //Angle of the arm
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-4.0d * Math.PI, 4.0d * Math.PI));
        //Angular Velocity
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-1.0d, 1.0d));
        //Now we want to add the variable for all the compartments
        for (int i = 0; i < 4 * (2 * arm.getCompartments().size() + allFood.size()); i++) {
            if (i % 4 == 0) {
                theTaskSpecObject.addContinuousObservation(new DoubleRange(minXPos, maxXPos));
            }
            if (i % 4 == 1) {
                theTaskSpecObject.addContinuousObservation(new DoubleRange(minYPos, maxYPos));
            }
            if (i % 4 == 2 || i % 4 == 3) {
                theTaskSpecObject.addContinuousObservation(new DoubleRange(minVel, maxVel));
            }
        }
        int NDA = 8;


        if (useDiscreteActions) {
            theTaskSpecObject.addDiscreteAction(new IntRange(0, NDA - 1));
        } else {
            theTaskSpecObject.addContinuousAction((new DoubleRange(minAction, maxAction, ts.getNumActionVariables())));
        }
        theTaskSpecObject.setRewardRange(new DoubleRange(-1, 20));


        String newTaskSpecString = theTaskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(newTaskSpecString);

        return newTaskSpecString;

    }

    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder P) {
        Octopus theOctopus = new Octopus();
        String taskSpec = theOctopus.makeTaskSpec();
        return new TaskSpecPayload(taskSpec, false, "");
    }

    public TaskDescription getTaskSpec() {
        int numCompartments = arm.getCompartments().size();
        int numFood = allFood.size();
        int numStateVars = 2 + 4 * (2 * numCompartments + numFood);
        int numActionVars = 3 * numCompartments;
        return new TaskDescription(numStateVars, numActionVars);
    }

    public Observation env_start() {
        taskTracker.reset();
        envSimulator.setODEState(initialState);

        currentState = initialState;

        for (EnvironmentObserver o : observers) {
            o.episodeStarted();
        }
        return makeObservation();
    }

    public Reward_observation_terminal env_step(Action theAction) {
        //Bail if we're terminal
        boolean terminal = taskTracker.isTerminal();
        if (terminal) {
            currentState = envSimulator.getODEState();
            return makeRewardObservation(0.0, terminal);
        }

        //BT: This code is expecting a double array called action, I'll give it to them
        double[] action = theAction.doubleArray;
        List<Compartment> compartments = arm.getCompartments();
        /* guard against oversized or undersized action arrays */

        if (useDiscreteActions) {
            int theDiscreteAction = theAction.intArray[0];
            action = new double[2 + compartments.size() * 3];
            handleIntAction(theDiscreteAction, compartments.size(), action);
        }
        double actionSum = 0.0d;
        for (int i = 0; i < action.length; i++) {
            actionSum += action[i];
        }
        ArmBase base = arm.getBase();
        base.setAction(action[0], action[1]);

        for (int i = 0; i < compartments.size(); i++) {
            compartments.get(i).setAction(
                    action[2 + 3 * i], action[2 + 3 * i + 1], action[2 + 3 * i + 2]);
        }

        ODEState odeState = envSimulator.getODEState();
        double timeStep = .2;
        odeState = solver.solve(envSimulator.asEquation(), odeState, 0, 5, timeStep);
        envSimulator.setODEState(odeState);
        currentState = odeState;

        taskTracker.update();

        terminal = taskTracker.isTerminal();
        for (EnvironmentObserver o : observers) {
            o.stateChanged(taskTracker.getReward());
            if (terminal) {
                o.episodeFinished();
            }
        }
        double reward = taskTracker.getReward() - actionSum / (double) action.length;
        //want to add a small penality for actions;

        return makeRewardObservation(reward, terminal);

    }

    /* accessors for parts of the environment (needed for display) */
    public Arm getArm() {
        return arm;
    }

    public Set<Food> getFood() {
        return Collections.unmodifiableSet(uneatenFood);
    }

    public Mouth getMouth() {
        return mouth;
    }

    public Set<Target> getTargets() {
        return Collections.unmodifiableSet(targets);
    }

    /* observation support (needed for display) */
    public void addObserver(EnvironmentObserver o) {
        observers.add(o);
    }

    protected Observation makeObservation() {
        Observation o = new Observation();
        o.doubleArray = currentState.getArray();
        o.intArray = new int[0];
        return o;
    }

    public void env_cleanup() {
    }
    //Going to try and implement the actiosn below
    private void handleIntAction(int discreteAction, int numCompartments, double[] actionArray) {
        if (discreteAction == 0) {
            actionArray[0] = 1.0d;
        }
        if (discreteAction == 1) {
            actionArray[1] = 1.0d;
        }

        for (int i = 0; i < numCompartments / 2; i++) {
            if (discreteAction == 2) {
                actionArray[2 + 3 * i] = 1.0d;
            }
            if (discreteAction == 3) {
                actionArray[2 + 3 * i + 1] = 1.0d;
            }
            if (discreteAction == 4) {
                actionArray[2 + 3 * i + 2] = 1.0d;
            }
        }
        for (int i = numCompartments / 2; i < numCompartments; i++) {
            if (discreteAction == 5) {
                actionArray[2 + 3 * i] = 1.0d;
            }
            if (discreteAction == 6) {
                actionArray[2 + 3 * i + 1] = 1.0d;
            }
            if (discreteAction == 7) {
                actionArray[2 + 3 * i + 2] = 1.0d;
            }
        }
    }

    /**
     * 
     * 
     * 
     *
     * 
    - shift: rotate the base of the arm counter-clockwise
    - enter: rotate the base of the arm clockwise
    - Z: fully contract all dorsal muscles on the lower half of the arm
    - X: fully contract all transversal muscles on the lower half of the arm
    - C: fully contract all ventral muscles on the lower half of the arm
    - I: fully contract all dorsal muscles on the upper half of the arm
    - O: fully contract all transversal muscles on the upper half of the arm
    - P: fully contract all ventral muscles on the upper half of the arm
     **/
    public URL getImageURL() {
       URL imageURL = Octopus.class.getResource("/images/octopus.png");
       return imageURL;
   }  
    
    public String env_message(String theMessage) {
        EnvironmentMessages theMessageObject;
        try {
            theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent Octopus a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            String theResponseString = theMessageObject.handleAutomatically(this);
            return theResponseString;
        }

//		If it wasn't handled automatically, maybe its a custom Mountain Car Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();
            AbstractResponse theResponse = null;
            if (theCustomType.equals("GETOCTSTATE")) {
                theResponse = new OctopusStateResponse(arm.getCompartments());
            }
            if (theCustomType.equals("GETOCTCOREDATA")) {
                theResponse = new OctopusCoreDataResponse(targets, Constants.get().getSurfaceLevel());
            }
            if (theResponse != null) {
                return theResponse.makeStringResponse();
            }
        }
        System.err.println("We need some code written in Env Message for Octopus.. unknown request received: " + theMessage);
        Thread.dumpStack();
        return null;
    }

    public RLVizVersion getTheVersionISupport() {
        return new RLVizVersion(1, 1);
    }

    public String getVisualizerClassName() {
        return OctopusVisualizer.class.getName();
    }

    private TaskTracker makeTaskTracker(TaskDef def) {
        /* can't use polymorphism here, since TaskDef subclasses are schema-
         * generated and shouldn't be modified (modifications are lost on
         * regeneration) */
        if (def instanceof FoodTaskDef) {
            return new FoodTaskTracker((FoodTaskDef) def);
        } else if (def instanceof TargetTaskDef) {
            return new TargetTaskTracker((TargetTaskDef) def);
        } else {
            throw new IllegalArgumentException("Unknown task definition given.");
        }
    }

    /**
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * TaskTracker class hierarchy BELOW
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     **/
    /* TaskTracker class hierarchy  */
    private interface TaskTracker {

        public void reset();

        public void update();

        public boolean isTerminal();

        public double getReward();
    }

    private abstract class TimeLimitTaskTracker implements TaskTracker {

        private int timeLimit;
        private double stepReward;
        private int timeLeft;

        protected TimeLimitTaskTracker(TaskDef def) {
            this.timeLimit = def.getTimeLimit().intValue();
            this.stepReward = def.getStepReward();
            timeLeft = timeLimit;
        }

        public void reset() {
            timeLeft = timeLimit;
        }

        public void update() {
            timeLeft--;
        }

        public boolean isTerminal() {
            return (timeLeft == 0);
        }

        public double getReward() {
            return stepReward;
        }
    }

    private class FoodTaskTracker extends TimeLimitTaskTracker {

        private boolean subgoalAchieved;
        private double reward;

        public FoodTaskTracker(FoodTaskDef taskDef) {
            super(taskDef);
            mouth = new Mouth(taskDef.getMouth());

            allFood = new HashSet<Food>();
            for (FoodSpec fs : taskDef.getFood()) {
                allFood.add(new Food(fs));
            }
            allFood = Collections.unmodifiableSet(allFood);

            uneatenFood.addAll(allFood);
        }

        @Override
        public void reset() {
            super.reset();
            uneatenFood.clear();
            uneatenFood.addAll(allFood);
        }

        public void update() {
            super.update();
            subgoalAchieved = false;
            reward = 0.0;
            for (Iterator<Food> i = uneatenFood.iterator(); i.hasNext();) {
                Food f = i.next();
                if (mouth.getShape().contains(f.getPosition().toPoint2D())) {
                    i.remove();
                    f.warp();
                    subgoalAchieved = true;
                    reward += f.getValue();
                }
            }
        }

        @Override
        public boolean isTerminal() {
            return (super.isTerminal() || uneatenFood.isEmpty());
        }

        public double getReward() {
            return subgoalAchieved ? reward : super.getReward();
        }
    }

    private class TargetTaskTracker extends TimeLimitTaskTracker {

        private ObjectiveTracker objectiveTracker;
        private boolean subgoalAchieved;
        private double reward;

        public TargetTaskTracker(TargetTaskDef def) {
            super(def);

            objectiveTracker = makeObjectiveTracker(def.getObjective());
        }

        @Override
        public void reset() {
            super.reset();
            objectiveTracker.reset();
            objectiveTracker.highlight();
        }

        public void update() {
            super.update();
            subgoalAchieved = objectiveTracker.check();
        }

        public double getReward() {
            return subgoalAchieved ? reward : super.getReward();
        }

        public boolean isTerminal() {
            return (super.isTerminal() || objectiveTracker.isAccomplished());
        }

        private ObjectiveTracker makeObjectiveTracker(ObjectiveSpec spec) {
            /* can't use polymorphism here, since ObjectiveSpec and subclasses
             * are schema-generated */
            if (spec instanceof SequenceSpec) {
                return new SequenceTracker((SequenceSpec) spec);
            } else if (spec instanceof ChoiceSpec) {
                return new ChoiceTracker((ChoiceSpec) spec);
            } else if (spec instanceof TargetSpec) {
                return new SingleTargetTracker((TargetSpec) spec);
            } else {
                throw new IllegalArgumentException("Unknown objective type.");
            }
        }

        private abstract class ObjectiveTracker {

            public abstract void reset();

            public abstract boolean check();

            public abstract boolean isAccomplished();

            /* for display purposes */
            public abstract void highlight();

            public abstract void unhighlight();
        }

        private class SequenceTracker extends ObjectiveTracker {

            private List<ObjectiveTracker> subObjectives;
            private int current;
            private boolean accomplished;

            public SequenceTracker(SequenceSpec spec) {
                subObjectives = new ArrayList<ObjectiveTracker>();
                for (ObjectiveSpec os : spec.getObjective()) {
                    subObjectives.add(makeObjectiveTracker(os));
                }
                current = 0;
                accomplished = false;
            }

            public void reset() {
                for (ObjectiveTracker o : subObjectives) {
                    o.reset();
                }
                current = 0;
                accomplished = false;
            }

            public boolean check() {
                boolean hit = subObjectives.get(current).check();
                if (hit && subObjectives.get(current).isAccomplished()) {
                    subObjectives.get(current).unhighlight();
                    current++;

                    if (current < subObjectives.size()) {
                        subObjectives.get(current).highlight();
                    } else {
                        accomplished = true;
                    }
                }
                return hit;
            }

            public boolean isAccomplished() {
                return accomplished;
            }

            public void highlight() {
                subObjectives.get(0).highlight();
                for (int i = 1; i < subObjectives.size(); i++) {
                    subObjectives.get(i).unhighlight();
                }
            }

            public void unhighlight() {
                for (ObjectiveTracker o : subObjectives) {
                    o.unhighlight();
                }
            }
        }

        private class ChoiceTracker extends ObjectiveTracker {

            private Set<ObjectiveTracker> subObjectives;
            private ObjectiveTracker selected;
            private boolean accomplished;

            public ChoiceTracker(ChoiceSpec spec) {
                subObjectives = new HashSet<ObjectiveTracker>();
                for (ObjectiveSpec os : spec.getObjective()) {
                    subObjectives.add(makeObjectiveTracker(os));
                }
                selected = null;
                accomplished = false;
            }

            public void reset() {
                for (ObjectiveTracker o : subObjectives) {
                    o.reset();
                }
                selected = null;
                accomplished = false;
            }

            public boolean check() {
                boolean hit = false;
                if (selected == null) {
                    for (ObjectiveTracker o : subObjectives) {
                        hit = o.check();
                        if (hit) {
                            selected = o;
                            break;
                        }
                    }

                    if (hit) {
                        for (ObjectiveTracker o : subObjectives) {
                            if (o != selected) {
                                o.unhighlight();
                            }
                        }
                    }
                } else {
                    hit = selected.check();
                }

                if (selected != null) {
                    accomplished = selected.isAccomplished();
                    if (accomplished) {
                        selected.unhighlight();
                    }
                }
                return hit;
            }

            public boolean isAccomplished() {
                return accomplished;
            }

            public void highlight() {
                for (ObjectiveTracker o : subObjectives) {
                    o.highlight();
                }
            }

            public void unhighlight() {
                for (ObjectiveTracker o : subObjectives) {
                    o.unhighlight();
                }
            }
        }

        private class SingleTargetTracker extends ObjectiveTracker {

            private Target target;
            private boolean accomplished;

            public SingleTargetTracker(TargetSpec spec) {
                target = new Target(spec);
                targets.add(target);
                accomplished = false;
            }

            public void reset() {
                accomplished = false;
            }

            public boolean check() {
                boolean hit = false;
                for (Compartment c : arm.getCompartments()) {
                    if (c.getShape().contains(target.getPosition().toPoint2D())) {
                        hit = true;
                        reward = target.getValue();
                        break;
                    }
                }

                accomplished = hit;
                return hit;
            }

            public boolean isAccomplished() {
                return accomplished;
            }

            public void highlight() {
                target.setHighlighted(true);
            }

            public void unhighlight() {
                target.setHighlighted(false);
            }
        }
    }
}

class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Octopus Arm";
    }

    public String getShortName() {
        return "Octopus";
    }

    public String getAuthors() {
        return "McGill RL Competition Team";
    }

    public String getInfoUrl() {
        return "http://library.rl-community.org/environments/octopus";
    }

    public String getDescription() {
        return "Alpha RL-Library Java Version of an octopus arm.";
    }
}
