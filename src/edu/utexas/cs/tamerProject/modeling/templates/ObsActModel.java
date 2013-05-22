package edu.utexas.cs.tamerProject.modeling.templates;

import java.util.ArrayList;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * The class Model is a very generic model that only does predictions from
 * observations and actions. Hand-coded reward models should implement this 
 * class directly.
 * 
 * @author bradknox
 *
 */
public interface ObsActModel {
	public double predictLabel(Observation obs, Action act);
}
