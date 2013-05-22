package edu.utexas.cs.tamerProject.applet;

/*
By Brad Knox.
*/
import java.applet.AppletContext;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;

import java.util.Properties;


import javax.swing.JApplet;


public class RLApplet extends JApplet 
			implements Observer {// implements KeyListener, MouseListener{
	private static final long serialVersionUID = 69786163333408557L;
	
	protected RLPanel rlPanel;
	protected boolean hasBeenReset = false;
	private final int STEPS_PER_LOG_SAVE = 30; // saves at episode end too
	
	public static boolean DEBUG_TIME = false; // keep false as default
	public static boolean IN_BROWSER = true;
	
	public void init() {
		super.init();
		RunLocalExperiment.isApplet = true;
		int height = this.getHeight();
		int width = this.getWidth();
		rlPanel = makeRLPanel();
		System.out.println("setting to size: " + width + ", " + height);
		System.out.println("size before in RLApplet.init(): " + this.getWidth() + ", " + this.getHeight());
		System.out.flush();
		rlPanel.setSize(width, height);
		System.out.println("size after in RLApplet.init(): " + this.getWidth() + ", " + this.getHeight());
		System.out.println("rlpanel size after in RLApplet.init(): " + rlPanel.getWidth() + ", " + rlPanel.getHeight());
		System.out.flush();
		this.setBackground(Color.black);

		if (IN_BROWSER) {
			
			// Get max number of episodes from parameters set in HTML; will exit once this limit is reached
			String numEpsStr = getParameter("numEpisodes");
			if (numEpsStr != null) RunLocalExperiment.numEpisodes = Integer.parseInt(numEpsStr);
	
			// Get max total number of steps from parameters set in HTML; will exit once this limit is reached
			String maxTotalStepsStr = getParameter("maxTotalSteps");
			if (maxTotalStepsStr != null)
				RunLocalExperiment.maxTotalSteps = Integer.parseInt(maxTotalStepsStr);
				
			// If set, this gives a number of time steps that one episode must last before the experiment will
			// end. This can be used while training a subject before an experiment to force a certain control
			// or training performance before moving to the real experiment.
			String finishExpAtStepsStr = getParameter("timeRequirement");
			if (finishExpAtStepsStr != null)
				RunLocalExperiment.finishExpIfNumStepsInOneEp = Integer.parseInt(finishExpAtStepsStr);		
					
			// Get the duration of a time step in milliseconds. Smaller values create faster transitions.
			String speedStr = getParameter("speed");
			System.out.println("speed: " + speedStr);
			if (speedStr != null) 
				RunLocalExperiment.stepDurInMilliSecs = Integer.valueOf(speedStr);
			
			// Boolean value determines whether the keys '+' and '-' can speed and slow transitions.
			String speedControlsStr = getParameter("speedControls");
			if (speedControlsStr != null) 
				RLPanel.enableSpeedControls = Boolean.valueOf(speedControlsStr);
	
			// Boolean value determines whether the key '1' can be used to move one step forward (only useful when paused).
			String singleStepControlStr = getParameter("singleStepControl");
			if (singleStepControlStr != null) 
				RLPanel.enableSingleStepControl = Boolean.valueOf(singleStepControlStr);
			
		}
		
		initPanel();
		
		
		
		System.out.println("\n\n\nEnd of init()\n\n\n");
	}
	
	public RLPanel getRLPanel(){return this.rlPanel;}
	public RLPanel makeRLPanel() {
		return new RLPanel();
	}
	
	public void initPanel() {
		rlPanel.init(null, null);
		this.getContentPane().add(rlPanel);
		this.rlPanel.runLocal.addObserver(this);
		rlPanel.runLocal.initExp();
		rlPanel.runLocal.startExp();
	}
	
    /*
     * Called when TinyState object (in RunLocal) changes 
     */
	public void update(Observable observable, Object obj) {
//		System.out.println("size in RLApplet.update(): " + this.getWidth() + ", " + this.getHeight());
//		System.out.flush();
		boolean timeToLog = false;
		if (this.rlPanel.runLocal.glue.getTimeStep() % STEPS_PER_LOG_SAVE == 0) // i.e., save every n steps and at the end of the episode
			timeToLog = true;
		if (observable instanceof RunLocalExperiment && obj instanceof Boolean) {
			boolean endOfEp = ((Boolean)obj).booleanValue();
			if (endOfEp)
				timeToLog = true;
		}
		if (timeToLog) {
			saveLog();
		}
	}
	
	public void reset() {
		this.requestFocusInWindow();
		this.getContentPane().removeAll();
		this.hasBeenReset = true;
		this.init();
	}
	
	
	
	
	

	/**
	 * Get a connection to the servlet.
	 */
	private URLConnection getServletConnection()
		throws MalformedURLException, IOException {

		// Connect to Servlet
		URL urlServlet = new URL(getCodeBase(), "AppletLogger");
		URLConnection con = urlServlet.openConnection();

		// Configuration
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty(
			"Content-Type",
			"application/x-java-serialized-object");

		return con;
	}
	
	/**
	 * Send the inputField data to the servlet and show the result in the outputField.
	 */
	protected void sendStringToServlet(String msg) {
		try {
			// send string to the servlet
			URLConnection con = getServletConnection();
			OutputStream outstream = con.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(outstream);
			oos.writeObject(msg.replace(' ', '%'));
			oos.flush();
			oos.close();

			// receive response from servlet
			InputStream instr = con.getInputStream();
			ObjectInputStream inputFromServlet = new ObjectInputStream(instr);
			String result = (String) inputFromServlet.readObject();
			inputFromServlet.close();
			instr.close();
			System.out.println("\n\nServlet says " + result); // show servlet response

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send the input string to PHP
	 */
	protected String sendStringToPHP(String msg) {
		String response = "";
		try {
			//msg = msg.replace(" ", "%20");
			msg = URLEncoder.encode(msg, "UTF-8");
			System.out.println("message size: " + msg.length());
			String urlAddition = "tamer.php?tamerStr=" + msg;
			System.out.println("sending " + urlAddition);
//			URL url = new URL(getCodeBase(), urlAddition);
			URL base = new URL("http://www.cs.utexas.edu/~bradknox/turk/");
			URL url = new URL(base, urlAddition);
			URLConnection con = url.openConnection();

			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty(
				"Content-Type",
				"application/x-java-serialized-object"); // this might not be correct, but I haven't looked it up since it works


			InputStream is = con.getInputStream();
			response = inputStreamToStr(is);
			
			System.out.println("response: " + response);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public static String inputStreamToStr(InputStream is) throws Exception{
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in;
		in = new InputStreamReader(is, "UTF-8");
		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if (read>0) {
				out.append(buffer, 0, read);
			}
		} while (read>=0);
		return out.toString();
	}
	
	public void saveLog() {}
	

}
