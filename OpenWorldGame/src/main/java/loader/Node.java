package loader;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

/**
 * Holds the relevant information from ASSIMP aiNode objects.
 * A node holds hierarchical information - its parent and child nodes,
 * and provides means to navigate through the hierarchy, both up and down.
 * 
 * Note that every bone in a model is an aiNode, but nodes are slightly more 
 * general and can also hold other information, which we are not interested in.
 * 
 * The hierarchy of bones is ultimately used to calculate the model-space
 * transformations for each bone, at each given frame. Animations are stored as 
 * local (bone-space) transformations, and we need a bone's parent's bind 
 * transformation to calculate the equivalent model-space transform.
 * 
 * During an animation, we must multiply a node by all of its parents
 * transformations to get into the space of the parent node, before finally
 * multiplying by the nodes own transformation.
 * 
 * @author Billy
 *
 */
public class Node {
	
	private final List<Node> children;
	private List<Matrix4f> transformations;
	private final String name;
	private final Node parent;
	
	public Node(String name, Node parent) {
		this.name = name;
		this.parent = parent;
		this.transformations = new ArrayList<>();
		this.children = new ArrayList<>();
	}
	
	/**
	 * A recursive method which returns the node's transformation after 
	 * taking into account the transformation of all of its parents.
	 * 
	 * The returned transformation is then in model-space (still not the final
	 * transformation we need - we would still need to take into account the
	 * inverse bind transformation).
	 * 
	 * @param node
	 * @param framePos
	 * @return
	 */
	public static Matrix4f getParentTransforms(Node node, int framePos) {
		if (node == null) {
			return new Matrix4f();
		} else {
			Matrix4f parentTransform = new Matrix4f(getParentTransforms(node.getParent(), framePos));
			
			List<Matrix4f> transformations = node.getTransformations();
			Matrix4f nodeTransform;
			int numTransformations = transformations.size();
			
			// There is a transformation for each frame.
			// framePos is frame 1, frame 2 etc.
			if (framePos < numTransformations) {
				nodeTransform = transformations.get(framePos);
			} else if (numTransformations > 0) {
				// Else get the last transformation.
				nodeTransform = transformations.get(numTransformations - 1);
			} else {
				nodeTransform = new Matrix4f();
			}
			
			return parentTransform.mul(nodeTransform);
		}
	}
	
	public void addChild(Node node) {
		this.children.add(node);
	}
	
	public void addTransformation(Matrix4f transformation) {
		transformations.add(transformation);
	}
	
	public Node findByName(String targetName) {
		Node result = null;
		
		if(this.name.equals(targetName)) {
			result = this;
		} else {
			for (Node child : children) {
				result = child.findByName(targetName);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets the number of frames in the animation for this joint. The number of
	 * frames is the maximum of all the frames for each child. e.g. if a wrist 
	 * joint moves just once, but a finger on the hand moves 5 times, then the 
	 * number of frames for the model is 5.
	 * 
	 */
	public int getNumAnimationFrames() {
		int numFrames = this.transformations.size();
		
		for (Node child : children) {
			int childFrame = child.getNumAnimationFrames();
			numFrames = Math.max(numFrames, childFrame);
		}
		return numFrames;
	}

	public List<Node> getChildren() {
		return children;
	}

	public List<Matrix4f> getTransformations() {
		return transformations;
	}

	public String getName() {
		return name;
	}

	public Node getParent() {
		return parent;
	}
	
	public void resetTransformations() {
		this.transformations = new ArrayList<>();
	}
	
	

}
