/* Helicopter Domain for RL - Competition - RLAI's Port of Pieter Abbeel's code submission
 * Copyright (C) 2007, Pieter Abbeel
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package org.rlcommunity.environments.helicopter;

import java.util.Random;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;


public class HelicopterState {

    /* some constants indexing into the helicopter's state */
    static final int ndot_idx = 0; // north velocity
    static final int edot_idx = 1; // east velocity
    static final int ddot_idx = 2; // down velocity
    static final int n_idx = 3; // north
    static final int e_idx = 4; // east
    static final int d_idx = 5; // down
    static final int p_idx = 6; // angular rate around forward axis
    static final int q_idx = 7; // angular rate around sideways (to the right) axis
    static final int r_idx = 8; // angular rate around vertical (downward) axis
    static final int qx_idx = 9; // quaternion entries, x,y,z,w   q = [ sin(theta/2) * axis; cos(theta/2)]
    static final int qy_idx = 10; // where axis = axis of rotation; theta is amount of rotation around that axis
    static final int qz_idx = 11;  // [recall: any rotation can be represented by a single rotation around some axis]
    final static int qw_idx = 12;
    final int state_size = 13;

	static final int NUMOBS = 12;
    // note: observation returned is not the state itself, but the "error state" expressed in the helicopter's frame (which allows for a simpler mapping from observation to inputs)
    // observation consists of:
    // u, v, w  : velocities in helicopter frame
    // xerr, yerr, zerr: position error expressed in frame attached to helicopter [xyz correspond to ned when helicopter is in "neutral" orientation, i.e., level and facing north]
    // p, q, r
    // qx, qy, qz
    public boolean env_terminal = false;
    public int num_sim_steps = 0;
    double wind[] = new double[2];
    Random randomNumberGenerator = new Random();

    public final Random getRandom() {
        return randomNumberGenerator;
    }
    // upper bounds on values state variables can take on (required by rl_glue to be put into a string at environment initialization)
    static double MAX_VEL = 5.0; // m/s
    static double MAX_POS = 20.0;
    static double MAX_RATE = 2 * 3.1415 * 2;
    static double MAX_QUAT = 1.0;
    static double MIN_QW_BEFORE_HITTING_TERMINAL_STATE = Math.cos(30.0 / 2.0 * Math.PI / 180.0);
    static double MAX_ACTION = 1.0;
    static double WIND_MAX = 5.0; // 
    static double mins[] = {-MAX_VEL, -MAX_VEL, -MAX_VEL, -MAX_POS, -MAX_POS, -MAX_POS, -MAX_RATE, -MAX_RATE, -MAX_RATE, -MAX_QUAT, -MAX_QUAT, -MAX_QUAT, -MAX_QUAT};
    static double maxs[] = {MAX_VEL, MAX_VEL, MAX_VEL, MAX_POS, MAX_POS, MAX_POS, MAX_RATE, MAX_RATE, MAX_RATE, MAX_QUAT, MAX_QUAT, MAX_QUAT, MAX_QUAT};
    // very crude helicopter model, okay around hover:
    final double heli_model_u_drag = 0.18;
    final double heli_model_v_drag = 0.43;
    final double heli_model_w_drag = 0.49;
    final double heli_model_p_drag = 12.78;
    final double heli_model_q_drag = 10.12;
    final double heli_model_r_drag = 8.16;
    final double heli_model_u0_p = 33.04;
    final double heli_model_u1_q = -33.32;
    final double heli_model_u2_r = 70.54;
    final double heli_model_u3_w = -42.15;
    final double heli_model_tail_rotor_side_thrust = -0.54;
    final double DT = .1; // simulation time scale  [time scale for control --- internally we integrate at 100Hz for simulating the dynamics]
    final static int NUM_SIM_STEPS_PER_EPISODE = 6000; // after 6000 steps we automatically enter the terminal state
    public HeliVector velocity = new HeliVector(0.0d, 0.0d, 0.0d);
    public HeliVector position = new HeliVector(0.0d, 0.0d, 0.0d);
    public HeliVector angular_rate = new HeliVector(0.0d, 0.0d, 0.0d);
    public Quaternion q = new Quaternion(0.0d, 0.0d, 0.0d, 1.0d);
    public double noise[] = new double[6];

    public HelicopterState() {
    }

    public HelicopterState(HelicopterState stateToCopy) {
        velocity = new HeliVector(stateToCopy.velocity);
        position = new HeliVector(stateToCopy.position);
        angular_rate = new HeliVector(stateToCopy.angular_rate);
        q = new Quaternion(stateToCopy.q);
        for (int i = 0; i < 6; i++) {
            noise[i] = stateToCopy.noise[i];
        }
        num_sim_steps = stateToCopy.num_sim_steps;
        env_terminal = stateToCopy.env_terminal;
    }

    double MyMin(double x, double y) {
        return (x < y ? x : y);
    }

    double MyMax(double x, double y) {
        return (x > y ? x : y);
    }

    public void reset() {
        this.velocity = new HeliVector(0.0d, 0.0d, 0.0d);
        this.position = new HeliVector(0.0d, 0.0d, 0.0d);
        this.angular_rate = new HeliVector(0.0d, 0.0d, 0.0d);
        this.q = new Quaternion(0.0d, 0.0d, 0.0d, 1.0);
        this.num_sim_steps = 0;
        this.env_terminal = false;
    }

    public double box_mull() {
        double x1 = randomNumberGenerator.nextDouble();
        double x2 = randomNumberGenerator.nextDouble();
        return Math.sqrt(-2.0f * Math.log(x1)) * Math.cos(2.0f * Math.PI * x2);
    }

    public double rand_minus1_plus1() {

        double x1 = randomNumberGenerator.nextDouble();
        return 2.0f * x1 - 1.0f;
    }

    public double getRandomNumber() {
        return randomNumberGenerator.nextDouble();
    }

	private void checkObservationConstraints(double observationDoubles[]){
		for(int i=0; i<NUMOBS; i++){
			if(observationDoubles[i] > maxs[i]) observationDoubles[i] = maxs[i];
			if(observationDoubles[i] < mins[i]) observationDoubles[i] = mins[i];
		}
	}
	
    public Observation makeObservation() {
        Observation o = new Observation(0, state_size - 1);
        //observation is the error state in the helicopter's coordinate system (that way errors/observations can be mapped more directly to actions)
        HeliVector ned_error_in_heli_frame = this.position.express_in_quat_frame(this.q);
        HeliVector uvw = this.velocity.express_in_quat_frame(this.q);

        /*System.out.println("New position: [" + Double.toString(position.x) + "," + 
        Double.toString(position.y) + "," +
        Double.toString(position.z) + "]");
        System.out.println("Error position: [" + Double.toString(ned_error_in_heli_frame.x) + "," + 
        Double.toString(ned_error_in_heli_frame.y) + "," +
        Double.toString(ned_error_in_heli_frame.z) + "]");*/

        o.doubleArray[0] = uvw.x;
        o.doubleArray[1] = uvw.y;
        o.doubleArray[2] = uvw.z;

        o.doubleArray[n_idx] = ned_error_in_heli_frame.x;
        o.doubleArray[e_idx] = ned_error_in_heli_frame.y;
        o.doubleArray[d_idx] = ned_error_in_heli_frame.z;
        o.doubleArray[p_idx] = angular_rate.x;
        o.doubleArray[q_idx] = angular_rate.y;
        o.doubleArray[r_idx] = angular_rate.z;

        // the error quaternion gets negated, b/c we consider the rotation required to bring the helicopter back to target in the helicopter's frame
        o.doubleArray[qx_idx] = q.x;
        o.doubleArray[qy_idx] = q.y;
        o.doubleArray[qz_idx] = q.z;

		checkObservationConstraints(o.doubleArray);
        return o;
    }

    public void stateUpdate(Action a) {
        // saturate all the actions, b/c the actuators are limited: 
        //[real helicopter's saturation is of course somewhat different, depends on swash plate mixing etc ... ]
        for (int i = 0; i < 4; ++i) {
            a.doubleArray[i] = MyMin(MyMax(a.doubleArray[i], -1.0), +1.0);
        }


        final double noise_mult = 2.0;
        final double noise_std[] = {0.1941, 0.2975, 0.6058, 0.1508, 0.2492, 0.0734}; // u, v, w, p, q, r
        double noise_memory = .8;
        //generate Gaussian random numbers

        for (int i = 0; i < 6; ++i) {
            noise[i] = noise_memory * noise[i] + (1.0d - noise_memory) * box_mull() * noise_std[i] * noise_mult;
        }

        double dt = .01;  //integrate at 100Hz [control at 10Hz]
        for (int t = 0; t < 10; ++t) {

            // Euler integration:

            // *** position ***
            this.position.x += dt * this.velocity.x;
            this.position.y += dt * this.velocity.y;
            this.position.z += dt * this.velocity.z;

            /*System.out.println("New position: [" + Double.toString(position.x) + "," + 
            Double.toString(position.y) + "," +
            Double.toString(position.z) + "]");*/
            // *** velocity ***
            HeliVector uvw = this.velocity.express_in_quat_frame(this.q);
            /*System.out.println("uvw: [" + Double.toString(uvw.x) + "," + 
            Double.toString(uvw.y) + "," +
            Double.toString(uvw.z) + "]");*/
            HeliVector wind_ned = new HeliVector(wind[0], wind[1], 0.0);
            HeliVector wind_uvw = wind_ned.express_in_quat_frame(this.q);
            HeliVector uvw_force_from_heli_over_m = new HeliVector(-heli_model_u_drag * (uvw.x + wind_uvw.x) + noise[0],
                    -heli_model_v_drag * (uvw.y + wind_uvw.y) + heli_model_tail_rotor_side_thrust + noise[1],
                    -heli_model_w_drag * uvw.z + heli_model_u3_w * a.doubleArray[3] + noise[2]);

            HeliVector ned_force_from_heli_over_m = uvw_force_from_heli_over_m.rotate(this.q);
            this.velocity.x += dt * ned_force_from_heli_over_m.x;
            this.velocity.y += dt * ned_force_from_heli_over_m.y;
            this.velocity.z += dt * (ned_force_from_heli_over_m.z + 9.81d);

            /*System.out.println("New velocity: [" + Double.toString(velocity.x) + "," + 
            Double.toString(velocity.y) + "," +
            Double.toString(velocity.z) + "]");*/

            // *** orientation ***
            HeliVector axis_rotation = new HeliVector(this.angular_rate.x * dt,
                    this.angular_rate.y * dt,
                    this.angular_rate.z * dt);
            Quaternion rot_quat = axis_rotation.to_quaternion();
            this.q = this.q.mult(rot_quat);

            /*System.out.println("New orientation: [" + Double.toString(this.q.x) + "," + 
            Double.toString(q.y) + "," +
            Double.toString(q.z) + "," + 
            Double.toString(q.w) + "]");*/


            // *** angular rate ***

            double p_dot = -heli_model_p_drag * this.angular_rate.x + heli_model_u0_p * a.doubleArray[0] + noise[3];
            double q_dot = -heli_model_q_drag * this.angular_rate.y + heli_model_u1_q * a.doubleArray[1] + noise[4];
            double r_dot = -heli_model_r_drag * this.angular_rate.z + heli_model_u2_r * a.doubleArray[2] + noise[5];

            this.angular_rate.x += dt * p_dot;
            this.angular_rate.y += dt * q_dot;
            this.angular_rate.z += dt * r_dot;

            /*System.out.println("New angular rate: [" + Double.toString(this.angular_rate.x) + "," + 
            Double.toString(angular_rate.y) + "," +
            Double.toString(angular_rate.z) + "]");*/

            if (!env_terminal && (Math.abs(this.position.x) > MAX_POS ||
                    Math.abs(this.position.y) > MAX_POS ||
                    Math.abs(this.position.y) > MAX_POS ||
                    Math.abs(this.velocity.x) > MAX_VEL ||
                    Math.abs(this.velocity.y) > MAX_VEL ||
                    Math.abs(this.velocity.z) > MAX_VEL ||
                    Math.abs(this.angular_rate.x) > MAX_RATE ||
                    Math.abs(this.angular_rate.y) > MAX_RATE ||
                    Math.abs(this.angular_rate.z) > MAX_RATE ||
                    Math.abs(this.q.w) < MIN_QW_BEFORE_HITTING_TERMINAL_STATE)) {
                env_terminal = true;

            }
        }
    }

    public String stringSerialize() {
        StringBuffer b = new StringBuffer();
        b.append("hs_");
        velocity.stringSerialize(b);
        position.stringSerialize(b);
        angular_rate.stringSerialize(b);
        q.stringSerialize(b);
        b.append("_noise");
        for (int i = 0; i < noise.length; i++) {
            b.append("_n" + i + "_" + noise[i]);
        }

        return b.toString();
    }
}
