package models.animatedModel;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;


/**
 * Represents a joint as part of a 'skeleton' The joint is moved to deform vertices
 * around it during an animation.
 * 
 * Each joint stores its inverse bind transform matrix, which is used to transform a
 * local transformation to a model-space one. This is ultimately used in the calculation
 * of the animated transform matrix which is used to transform a joint from its bind
 * position to its location in each animation frame.
 * 
 * The inverse bind transformation information can be extracted from Collada files. In the
 * tutorial version of this class, the bind local transform was loaded and then the inverse bind
 * transformation was calculated from that. This is unnecessary however.
 * 
 * @author Billy
 *
 */
public class Joint {
	
	public final int index;// (into the joint transform array). i.e. the place in the 
	// array where we can find/place the transform for this joint.
	public final String name;
	// e.g. shoulder joint - this is how we refer to the joint.
	public final List<Joint> children = new ArrayList<Joint>();

	private Matrix4f animatedTransform = new Matrix4f();
	
	private Matrix4f inverseBindTransform = new Matrix4f();

	/**
	 * @param index
	 *      - the joint's index (ID).
	 * @param name
	 * 		- the name of the joint. This is how the joint is named in the
	 *        collada file, and so is used to identify which joint a joint
	 *        transform in an animation keyframe refers to.
	 * @param inverseBindTransform
	 *      - a model-space transformation for moving from the bind position
	 *        and rotation to the origin.
	 */
	public Joint(int index, String name, Matrix4f inverseBindTransform) {
		this.index = index;
		this.name = name;
		this.inverseBindTransform = inverseBindTransform;
	}
	
	/**
	 * Used during the creation of a joint hierarchy.
	 */
	public void addChild(Joint child) {
		children.add(child);
	}
	
	/**
	 * Used by the animator to update this joint's transformation to the one relevant
	 * for the current animation frame.
	 */
	public void setAnimationTransform(Matrix4f animationTransform) {
		this.animatedTransform = animationTransform;
	}
	
	/**
	 * This is what is loaded into the shader to deform the mesh in the animation.
	 */
	public Matrix4f getAnimatedTransform() {
		return animatedTransform;
	}
	
	public Matrix4f getInverseBindTransform() {
		return inverseBindTransform;
	}
	
}
