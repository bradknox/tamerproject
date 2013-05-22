/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.environments.continuousgridworld;

import java.awt.geom.Rectangle2D;

/**
 *
 * @author btanner
 */
public class MapGenerator {
    static final int MAP_EMPTY = 0;
    static final int MAP_CUP   = 1;
    static final int MAP_MAZE   = 2;

    static void makeMap(int number, AbstractContinuousGridWorld theGridWorld) {
        switch (number) {
            case MAP_EMPTY: // Empty map
                break;
            case MAP_CUP: // Cup map
                theGridWorld.addBarrierRegion(new Rectangle2D.Double(50.0d, 50.0d, 10.0d, 100.0d), 1.0d);
                theGridWorld.addBarrierRegion(new Rectangle2D.Double(50.0d, 50.0d, 100.0d, 10.0d), 1.0d);
                theGridWorld.addBarrierRegion(new Rectangle2D.Double(150.0d, 50.0d, 10.0d, 100.0d), 1.0d);
                break;
            case MAP_MAZE:
                //Left wall with break
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(20.0d, 20.0d, 10.0d, 80.0d), 1.0d);
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(20.0d, 115.0d, 10.0d, 70.0d), 1.0d);

               //Second left wall with opening at the top
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(50.0d, 40.0d, 10.0d, 140.0d), 1.0d);

               //cross wall under the goal
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(50.0d, 110.0d, 70.0d, 10.0d), 1.0d);


               //Going down from the cross wall
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(110.0d, 110.0d, 10.0d, 60.0d), 1.0d);

               //second cross wall under the goal
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(80.0d, 140.0d, 30.0d, 10.0d), 1.0d);

               //Going down from the second cross wall
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(80.0d, 140.0d, 10.0d, 30.0d), 1.0d);

               //Correct path goal protector wall
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(110.0d, 65.0d, 10.0d, 60.0d), 1.0d);

               //Top wall
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(20.0d, 20.0d, 160.0d, 10.0d), 1.0d);

               //Second to top wall
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(50.0d, 40.0d, 100.0d, 10.0d), 1.0d);

               //Far right wall
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(170.0d, 20.0d, 10.0d, 160.0d), 1.0d);

               //Second from Far right wall
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(140.0d, 40.0d, 10.0d, 140.0d), 1.0d);
 
                //Bottom wall with break
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(20.0d, 180.0d, 70.0d, 10.0d), 1.0d);
               theGridWorld.addBarrierRegion(new Rectangle2D.Double(105.0d, 180.0d, 75.0d, 10.0d), 1.0d);
                break;

            default:
                throw new IllegalArgumentException ("Map number "+number);
        }
    }

}
