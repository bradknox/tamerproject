package edu.utexas.cs.tamerProject.applet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlviz.app.VisualizerFactory;
import org.rlcommunity.rlviz.app.VisualizerPanel;
import org.rlcommunity.rlviz.app.frames.VisualizerVizFrame;

import rlVizLib.messaging.environment.EnvVisualizerNameRequest;
import rlVizLib.messaging.environment.EnvVisualizerNameResponse;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.RenderObject;
import rlVizLib.visualization.VisualizerPanelInterface;


public class RLPanel extends JPanel 
					implements Observer, KeyListener, MouseListener, VisualizerPanelInterface {
	private static final long serialVersionUID = 111112222333445L;
	
	public RunLocalExperiment runLocal;
//	EnvPanel envPanel;
	AgentInterface agent;
	EnvironmentInterface env;
	
	Timer repaintTimer;
	AbstractVisualizer theEnvViz;
	public static String defaultEnvVisualizerClassName = 
					"org.rlcommunity.visualizers.generic.GenericEnvVisualizer";
	BufferedImage bufferedEnvImage = null;
	protected final Object envImageLock = new Object();
	
	final double DUR_OF_DISPLAY = 3; // in seconds
	public static String nameForTimeSteps = "Time"; //"Time step"
	public static boolean DISPLAY_SECONDS_FOR_TIME = false; // display time step number if false. TODO return to true for turk
	public static String nameForEpisodes = "Game number"; //"Episode"
	public static boolean DISPLAY_REW_THIS_EP = false;
	public static boolean PRINT_REW_AS_INT = false;
	public static String nameForRew = "Reward";
	
	double timeOfLastDurChange = getCurrentTimeInSecs();
	double timeOfLastEpStart = getCurrentTimeInSecs();
	
	public static boolean enableSpeedControls = true;
	public static boolean enableSingleStepControl = true;
		
	
	public void init(AgentInterface agent, EnvironmentInterface env) {	
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setRequestFocusEnabled(true);
		addKeyListener(this);
		addMouseListener(this);
		//setDoubleBuffered(true);
		setBackground(Color.white);	
		System.out.println("RLPanel size after in RLPanel.init(): " + this.getWidth() + ", " + this.getHeight());
		
		runLocal = new RunLocalExperiment();
		runLocal.theAgent = agent;
		runLocal.theEnvironment = env;
		runLocal.init();
		System.out.println("runLocal: " + runLocal);
		this.agent = runLocal.theAgent;
		this.env = runLocal.theEnvironment;
        EnvVisualizerNameResponse theNameResponse = EnvVisualizerNameRequest.Execute();
        String vizClassName = theNameResponse.getTheVisualizerClassName();
        theEnvViz = createVisualizer(vizClassName);
        //theEnvViz.verbose = true;
        theEnvViz.setParentPanel(this);

		
		//runLocal.glue.addObserver(this);
		/* At least one visualizer class for each environment observes TinyGlue, and 
		 * theEnvViz.redrawCurrentImage() appears to depend on the environment visualizer's 
		 * update() being called first. Therefore this class also observes runLocal to get 
		 * any visualization changes that were missed previously. I didn't dig deeply enough 
		 * to be certain, but this second addition removes a one-step visualization delay. */
		//runLocal.addObserver(this);  

		// The following three lines should be investigated if some domains aren't working.
		//Dimension dim = this.getSize();
		//VisualizerPanel vizPanel = new VisualizerPanel(dim, VisualizerVizFrame.EnvVisualizerType);
		//theEnvViz.setParentPanel(vizPanel);
		theEnvViz.startVisualizing();
		theEnvViz.notifyPanelSizeChange();
		
		//System.out.println("SwingUtilities.isEventDispatchThread(): " + SwingUtilities.isEventDispatchThread() );
		
	    ActionListener actionListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent actionEvent) {
	    		Thread.currentThread().setName("RLPanelActionListener-AWT-EventQueue");
	    		if (RLApplet.DEBUG_TIME) {
	    			System.out.println("Time of action performed in RLPanel: " + ((System.currentTimeMillis() / 1000.0) % 1000));
		    		System.out.flush();
	    		}
	    		if (runLocal.glue.getTimeStep() == 1) // This is a hack to make it show the initial state in case the agent takes a while to respond with an action.
	    			update(null, null);
	    		repaint();
	        }
	    };
	    repaintTimer = new Timer(33, actionListener);
	    repaintTimer.start();
		requestFocus();
		
		

	}
	
	
	

	public void paintComponent (Graphics g) {
    	paintComponent(g, getCurrentTimeInSecs());
	}
    public void paintComponent( Graphics g, double currTime ) {
    	if (RLApplet.DEBUG_TIME) {
			System.out.println("RLPanel paint() at "  + ((System.currentTimeMillis() / 1000.0) % 1000) + ". isFocusOwner: " + isFocusOwner());
	    	//System.out.println("paint(), " + this.isDoubleBuffered());
	    	System.out.flush();
    	}
    	//System.out.println("RLPanel object in RLPanel.paint(): " + System.identityHashCode(this));
    	if (!runLocal.expInitialized)
    		return;
    	synchronized(envImageLock) {
    		super.paintComponent(g);    		
    		g.drawImage(bufferedEnvImage, 0, -12, this.getWidth(), this.getHeight(), this);
//    		System.out.println("size in RLPanel.paint(): " + this.getWidth() + ", " + this.getHeight());
//    		System.out.flush();
    	}
    	requestFocusInWindow();
    }
    

    protected void drawMeta(Graphics2D g, double currTime) {
    	Color startColor = g.getColor();
    	g.setColor(Color.black);
    	if (RLPanel.enableSpeedControls && 
    			currTime - this.timeOfLastDurChange < DUR_OF_DISPLAY) {
    		String speedStr = "Steps per second: " + 1000.0 / ((int)RunLocalExperiment.stepDurInMilliSecs);
    		g.drawString(speedStr, 
    				4*g.getFontMetrics().charWidth(' '), 
    				g.getFont().getSize() + 12);
    	}
    	

    	/*
    	 * Draw reward on panel
    	 */
    	if (DISPLAY_REW_THIS_EP) {
    		double rewThisEp = this.runLocal.getReturnThisEp();
    		String rewVal;
    		if (PRINT_REW_AS_INT)
    			rewVal = "" + (int)rewThisEp;
    		else
    			rewVal = "" + rewThisEp;
    				
    		String rewStr = RLPanel.nameForRew + ": " + rewVal;
    		g.drawString(rewStr, (this.getWidth() / 40), 
        			this.getHeight() - (int)(0.2 * g.getFontMetrics().getHeight()));
    	}
    	
    	

    	/*
    	 * draw time on panel
    	 */
    	int stepNum = runLocal.glue.getTimeStep();
    	int time = DISPLAY_SECONDS_FOR_TIME ? (int)(Math.round((float)stepNum 
    						* RunLocalExperiment.stepDurInMilliSecs * 0.001)) 
    						: stepNum;
    	String timeStr = nameForTimeSteps + ": " + time;
    	//System.out.println("timeStr: " + timeStr);
    	g.drawString(timeStr,
    			(this.getWidth() - g.getFontMetrics().stringWidth(timeStr) - 4*g.getFontMetrics().charWidth(' ')), 
    			this.getHeight() - (int)(0.2 * g.getFontMetrics().getHeight()));
    	
    	// draw episode number if a new episode recently began
    	if (currTime - this.timeOfLastEpStart < DUR_OF_DISPLAY) {
    		String epStr = nameForEpisodes + ": " + runLocal.glue.getEpisodeNumber(); 
    		g.drawString(epStr,
        			(this.getWidth() - g.getFontMetrics().stringWidth(epStr)) / 2, 
        			this.getHeight() - (int)(0.2 * g.getFontMetrics().getHeight()));
    	}
    	g.setColor(startColor);
    }
    
    /*
     * Called when any observed Observable object changes. This class observes
     * runLocal and the TinyState object in runLocal. 
     */
	public void update(Observable observable, Object obj) {
    	if (RLApplet.DEBUG_TIME) {
    		System.out.println("Time of RLPanel update(): " + ((System.currentTimeMillis() / 1000.0) % 1000));
    		System.out.flush();
    	}

//		//System.out.println("update time in RLPanel: " + String.format("%f", ((new Date()).getTime() / 1000.0)));
//		String observableName = observable == null? "null" : observable.getClass().getSimpleName();
//		System.out.println("update in RLPanel from Observable " + observableName);
//		//String objName = obj == null? "null" : obj.getClass().getName();
//		//System.out.println("obj passed to update in RLPanel: " + objName); 
//
//		if (obj != null && obj.getClass().getSimpleName().equals("BufferedImage")) {
//			// do the stuff below 
//			System.out.println("Got buffered image in RLPanel.update()");
//		}
//		
//		// create the environmental image to be displayed
//		if (SwingUtilities.isEventDispatchThread()) {
////			System.out.println("thread inside update()");
//			makeCurrentEnvImage();
//		}
//		else { // do image update in event dispatch thread
//			try {
//				SwingUtilities.invokeAndWait(new Runnable() {
//				    public void run() {makeCurrentEnvImage();}
//				});
//			} catch (Exception e) {e.printStackTrace();}
//		}
//		if (runLocal.glue.getTimeStep() == 1)
//			this.timeOfLastEpStart = getCurrentTimeInSecs();
	}
	
	private void makeCurrentEnvImage() {
		synchronized(envImageLock) {
			//System.out.println("last draw time before redraw: " + this.theEnvViz.lastDrawTime); // to test whether there is a delay the actual redrawing
			//this.theEnvViz.redrawCurrentImage(); not right
			//this.theEnvViz.forceRedraw();
			
			//System.out.println("last draw time after redraw: " + this.theEnvViz.lastDrawTime);
			this.bufferedEnvImage = getImageCopy(theEnvViz.getLatestImage());
			//System.out.println("last draw time after copy: " + this.theEnvViz.lastDrawTime);
			drawMeta(bufferedEnvImage.createGraphics(), getCurrentTimeInSecs());
//			System.out.println("size in RLPanel.makeCurrentEnvImage(): " + this.getWidth() + ", " + this.getHeight());
//			System.out.flush();
		}
	}


	
	
	
	
	public void keyPressed( KeyEvent e ) {
		System.out.println("pressed in rlpanel: " + e.getKeyChar());
		boolean stepDurChange = false;
		if (RLPanel.enableSpeedControls 
				&& e.getKeyChar() == '+') { // speed up
			stepDurChange = true;
			RunLocalExperiment.stepDurInMilliSecs = Math.max(RunLocalExperiment.stepDurInMilliSecs / Math.cbrt(2.0), 1); // speed doubles every 3 presses
		}
		else if (RLPanel.enableSpeedControls 
				&& e.getKeyChar() == '-') { // slow down
			stepDurChange = true;
			RunLocalExperiment.stepDurInMilliSecs = Math.max((RunLocalExperiment.stepDurInMilliSecs * Math.cbrt(2.0)), 1.0); // speed halves every 3 presses
		}
		else if (e.getKeyChar() == '0') { // stop exp
			runLocal.stopExp();
		}
		else if (RLPanel.enableSingleStepControl 
				&& e.getKeyChar() == '1') { // take step (only makes sense when stopped)
			runLocal.takeStep(true);
		}
		else if (e.getKeyChar() == '2') { // start exp
			runLocal.startExp();
		}
		if (stepDurChange) { // handle change in step duration
			runLocal.stopExp();
			timeOfLastDurChange = getCurrentTimeInSecs();
			runLocal.startExp();
		}
		if (e.getKeyChar() == 'u') { // start exp
			update(null, null);
		}
		e.consume();
		
	}
	public void keyReleased( KeyEvent e ) {}
	public void keyTyped( KeyEvent e ) {}

	
	public void mouseEntered( MouseEvent e ) { }
	public void mouseExited( MouseEvent e ) { }
	public void mousePressed( MouseEvent e ) { }
	public void mouseReleased( MouseEvent e ) { }
	public void mouseClicked( MouseEvent e ) {
		//System.out.println("click");
		requestFocus();
		//System.out.println("has focus? " + hasFocus());
		//mouseX = e.getX(); //mouseY = e.getY();
	}
	
	
	
	
	
	public static double getCurrentTimeInSecs(){
		return (new Date()).getTime() / 1000.0;
	}
	
	protected BufferedImage getImageCopy(BufferedImage orig) {
//		System.out.println("Width and height in RLPanel: " + this.getWidth() + ", " + this.getHeight());
	    BufferedImage copy = new BufferedImage(this.getWidth(), this.getHeight(),
	        orig.getType());
	    Graphics g = copy.createGraphics();
	    g.drawImage(orig, (this.getWidth() - orig.getWidth()) / 2, 
	    		this.getHeight() - orig.getHeight(), null);
	    g.dispose();
	    return copy;
	}
	
	
	// this method is adapted from Brian Tanner's VisualizerFactory class
	public AbstractVisualizer createVisualizer(String vizClassName) {
		Class<?> theClass = null;
		try {
			theClass = Class.forName(vizClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		AbstractVisualizer theViz = null;
	    //First try and load the one with all parameters
	    theViz = VisualizerFactory.createVisualizer(theClass, runLocal.glue, null);
	    if (theViz == null) { //IF that didn't work, try without theControlTarget
	        theViz = VisualizerFactory.createVisualizer(theClass, runLocal.glue);
	    }
	    if (theViz == null) { //IF that didn't work, try without theGlueState
	        theViz = VisualizerFactory.createVisualizer(theClass);
	    }
	    return theViz;
	}
//	public class EnvPanel extends JPanel {    
//		private static final long serialVersionUID = -2771746395089917617L;
//		public void drawEnv(Graphics g, BufferedImage envImage) {
//	        g.clearRect(0,-12, this.getWidth(),this.getHeight()); 
//			g.drawImage(envImage, 0, -12, this.getWidth(), this.getHeight(), this);
//	    }
//	}





	public void receiveNotificationVizChanged() {
//		System.out.println("Viz changed, yo");
//		System.out.flush();
		// create the environmental image to be displayed
		if (SwingUtilities.isEventDispatchThread()) {
//			System.out.println("thread inside update()");
			makeCurrentEnvImage();
		}
		else { // do image update in event dispatch thread
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				    public void run() {
				    	makeCurrentEnvImage();
				    }
				});
			} catch (Exception e) {e.printStackTrace();}
		}
		if (runLocal.glue.getTimeStep() == 1)
			this.timeOfLastEpStart = getCurrentTimeInSecs();
		
	}
			
}


