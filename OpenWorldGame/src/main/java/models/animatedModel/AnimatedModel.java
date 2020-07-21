package models.animatedModel;

import org.joml.Matrix4f;

import animation.Animation;
import models.TexturedModel;

/**
 * Represents an entity which can be animated. Contains usual entity
 * information such as vertex positions and normals, along with additional
 * information for animation.
 * 
 * @author Billy
 *
 */
public class AnimatedModel {
	
	private TexturedModel texturedModel;
	
	// This holds the joint hierarchy. Each joint has a list of its children.
	private Joint rootJoint;
	private int jointCount;
	
	public AnimatedModel(TexturedModel texturedModel, Joint rootJoint, int jointCount) {
		this.texturedModel = texturedModel;
		this.rootJoint = rootJoint;
		this.jointCount = jointCount;
	}

	public TexturedModel getTexturedModel() {
		return texturedModel;
	}

	public Joint getRootJoint() {
		return rootJoint;
	}

	public int getJointCount() {
		return jointCount;
	}
	
	/**
	 * @return
	 * 		- The array of model-space joint transformations, which can be loaded to a vertex shader 
	 * 		  for animating the model.
	 */
	public Matrix4f[] getJointTransforms() {
		Matrix4f[] jointTransforms = new Matrix4f[jointCount];
		addJointsToArray(rootJoint, jointTransforms);
		return jointTransforms;
		
	}
	
	/**
	 * Puts each joint transform into its correct place in the joint matrices list, according
	 * to its joint index.
	 * 
	 * @param headJoint
	 * 		- The current joint, at the head of its tree.
	 * @param jointMatrices
	 * 		- The array of matrices in to which the joints should be added.
	 */
	private void addJointsToArray(Joint headJoint, Matrix4f[] jointMatrices) {
		jointMatrices[headJoint.index] = headJoint.getAnimatedTransform();
		for (Joint childJoint : headJoint.children) {
			addJointsToArray(childJoint, jointMatrices);
		}
	}

}
