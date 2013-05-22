package edu.utexas.cs.tamerProject.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.Timer;

public class VizUtils {

	
	public static JFrame createGenericDisplay(int width, int height){
		JFrame frame = new JFrame( "" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize(width, height); // set frame size
		
		return frame;
	}
	
	
	
	public static Timer setRepaintTimer(ActionListener listeningObj, int refreshInterv, int initDelay) {
		Timer timer = new Timer(refreshInterv, listeningObj);
		timer.setInitialDelay(initDelay);
		timer.start();
		return timer;
	}

	

	public static void drawArray(int startX, int startY, double regionWidth, 
								double regionHt, double[][] array, 
								Graphics2D g2d, double[] colorIndicator,
								boolean dividingLines) {
		double rectWidth = regionWidth / array[0].length;
		double rectHt = regionHt / array.length;
		int divLineMargin = 0;
		if (dividingLines)
			divLineMargin = 1;

		for (int row = 0; row < array.length; row++) {
			for (int col = 0; col < array[0].length; col++) {
//				System.out.println("row, col: " + row + ", " + col);
				double intensity = Math.min(array[row][col], 1.0);
//				System.out.println("intensity: " + intensity);
				if (intensity < 0) {
//					System.out.println("intensity: " + intensity);
//					System.out.println("colorIndicator[0]: " + colorIndicator[0]);
//					System.out.println("colorIndicator[1]: " + colorIndicator[1]);
//					System.out.println("colorIndicator[2]: " + colorIndicator[2]);
					intensity = Math.min(1.0, -intensity);
				}
				try{
					g2d.setPaint(new Color((float)(intensity * colorIndicator[0]),
							(float)(intensity * colorIndicator[1]),
							(float)(intensity * colorIndicator[2])));
				}
				catch (Exception e) {
					System.err.println(e);
					System.out.println("intensity: " + intensity);
					System.err.println(Arrays.toString(e.getStackTrace()));
					System.exit(1);
				}
				g2d.fill(new Rectangle2D.Double(startX + (int)(row * rectWidth),
						startY + (int)(col * rectHt),
						(int)((row + 1) * rectWidth) - (int)(row * rectWidth) - divLineMargin,
						(int)((col + 1) * rectHt) - (int)(col * rectHt) - divLineMargin));
			}
		}
	}

	
	
	
	// assumes that input array has an integer square root
	public static double[][] convertTo2D(double[] array) {
		double sqrtArrayLen = Math.sqrt(array.length); 
		int sizePerD = (int)sqrtArrayLen;
		if (sizePerD != sqrtArrayLen && Math.pow(sizePerD, 2) + 1 != array.length) {
			System.err.println("Attempted to convertTo2D() an array that did not have an integer square root. Size: " + array.length);
		}
		double[][] newArray = new double[sizePerD][sizePerD];
		for (int row = 0; row < sizePerD; row++) {
			for (int col = 0; col < sizePerD; col++) {
				newArray[row][col] = array[(row * sizePerD) + col];
			}
		}
		return newArray;
	}
	

}
