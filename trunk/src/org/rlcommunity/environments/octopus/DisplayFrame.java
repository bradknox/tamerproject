package org.rlcommunity.environments.octopus;

import org.rlcommunity.environments.octopus.components.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import java.lang.reflect.InvocationTargetException;

public class DisplayFrame {

    private static final int REDRAW_DELAY = 30;
    private static final float DRAWING_SCALE = 30.0f;
    private static final Paint ARM_PAINT = Color.GREEN;
    private static final float FOOD_SIZE = 0.6f;
    private static final Shape FOOD_SHAPE =
            new Ellipse2D.Double(-FOOD_SIZE / 2, -FOOD_SIZE / 2, FOOD_SIZE, FOOD_SIZE);
    private static final Paint FOOD_PAINT = Color.ORANGE;
    private static final float TARGET_SIZE = 0.2f;
    private static final Shape TARGET_SHAPE =
            new Ellipse2D.Double(-TARGET_SIZE / 2, -TARGET_SIZE / 2, TARGET_SIZE, TARGET_SIZE);
    private static final Paint ELIGIBLE_TARGET_PAINT = Color.RED;
    private static final Paint INELIGIBLE_TARGET_PAINT = new Color(.8f, .8f, .8f, .7f);
    private static final float ARM_HUE = 0.3f;
    private static final Paint WATER_PAINT = new Color(0, 0.5f, 1.0f, 0.1f);
    private Octopus env;
    private JFrame frame;
    private DisplayPanel panel;
    private JLabel rewardLabel;
    private double totalReward;

    public DisplayFrame(Octopus env) {
        this.env = env;

        final Runnable startGuiWindow = new Runnable() {

                    public void run() {
                        System.out.println("\t\t\t in Run making new frame");
                        frame = new JFrame("Octopus");
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        JPanel mainPanel = new JPanel(new BorderLayout());
                        mainPanel.add(panel = new DisplayPanel(), BorderLayout.CENTER);
                        mainPanel.add(rewardLabel = new JLabel(), BorderLayout.PAGE_END);

                        System.out.println("\t\t\t setting content pane");
                        frame.setContentPane(mainPanel);
                        frame.pack();
                        frame.setVisible(true);
                        System.out.println("\t\t\t just set it to visible");
                    }
                };

        Thread appThread = new Thread() {

                    public void run() {
                        try {
                            SwingUtilities.invokeAndWait(startGuiWindow);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("Finished on " + Thread.currentThread());
                    }
                    };
        appThread.start();

        System.out.println("\t\t\t DisplayFrame constructor");

        env.addObserver(new DisplayObserver());
        totalReward = 0.0;
    }

    public void addKeyListener(KeyListener kl) {
        frame.addKeyListener(kl);
    }

    private class DisplayObserver implements EnvironmentObserver {

        public void episodeStarted() {
            try {
                if (panel == null) {
                    Thread.sleep(1000);
                }
                totalReward = 0.0;
                panel.repaint();
            } catch (InterruptedException ex) {
                Logger.getLogger(DisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void stateChanged(double reward) {
            totalReward += reward;
            rewardLabel.setText(String.format("%.3f", totalReward));
            panel.repaint();
            try {
                Thread.sleep(REDRAW_DELAY);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); /* re-assert */
            }
        }

        public void episodeFinished() {
            rewardLabel.setText(String.format("Episode complete. Final reward: %.3f", totalReward));
            panel.repaint();
        }
    }

    private class DisplayPanel extends JPanel {
        
        private final Dimension PREFERRED_SIZE = new Dimension(640, 480);
        
        public Dimension getPreferredSize() { return PREFERRED_SIZE; }
        
        public void paintComponent(Graphics g) {
            List<Compartment> compartments = env.getArm().getCompartments();
            
            int displayWidth = DisplayPanel.this.getWidth();
            int displayHeight = DisplayPanel.this.getHeight();
            
            Graphics2D g2 = (Graphics2D)g;
            
            g2.setPaint(Color.WHITE);
            g2.fillRect(0, 0, displayWidth, displayHeight);
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
            AffineTransform oldTransform = g2.getTransform();
            g2.translate(displayWidth / 4, displayHeight / 2);
            g2.scale(DRAWING_SCALE, -DRAWING_SCALE);
            g2.setStroke(new BasicStroke(1.0f / DRAWING_SCALE));

            /* draw mouth */
            if (env.getMouth() != null) {
                g2.setPaint(Color.BLACK);
                g2.fill(env.getMouth().getShape());
            }
        
            /* draw arm */
            for (int i = 0, n = compartments.size(); i < n; i++) {
                Compartment c = compartments.get(i);
                Shape s = c.getShape();
                float lum = 0.7f + 0.3f * (float)i/n;
                g2.setPaint(new Color(Color.HSBtoRGB(ARM_HUE, 1.0f, lum)));
                g2.fill(s);            
            }
                
            AffineTransform baseTransform = g2.getTransform();
            
            /* draw food */
            g2.setPaint(FOOD_PAINT);
            for (Node n: env.getFood()) {
                Vector2D pos = n.getPosition();
                g2.translate(pos.getX(), pos.getY());
                g2.fill(FOOD_SHAPE);
                g2.setTransform(baseTransform);
            }
            
            /* draw targets */
            for (Target t: env.getTargets()) {
                Vector2D pos = t.getPosition();
                g2.translate(pos.getX(), pos.getY());
                g2.setPaint(t.isHighlighted() ?
                    ELIGIBLE_TARGET_PAINT :
                    INELIGIBLE_TARGET_PAINT);
                g2.fill(TARGET_SHAPE);
                g2.setTransform(baseTransform);
            }
            
            /* draw water level */
            g2.setPaint(WATER_PAINT);
            Shape waterShape = new Rectangle2D.Double(
                    -100, Constants.get().getSurfaceLevel()-200,
                    200, 200);
            g2.fill(waterShape);
            
            g2.setTransform(oldTransform);
        }
    }
}