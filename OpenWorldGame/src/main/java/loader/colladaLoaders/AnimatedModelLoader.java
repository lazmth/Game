package loader.colladaLoaders;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class AnimatedModelLoader {
	
	// VAO Information.
	private List<Vector3f> vertexPositions = new ArrayList<Vector3f>();
	private List<Vector3f> vertexNormals = new ArrayList<Vector3f>();
	private List<Vector2f> vertexTexCoords = new ArrayList<Vector2f>();
	// Joint IDs are refer to joints which affect a given vertex.
	// The joint transforms that are loaded into the vertex shader will
	// be indexed by their joint IDs also.
	private List<Integer> jointIds = new ArrayList<Integer>();
	private List<Float> jointWeights = new ArrayList<Float>();
	
	// Float arrays are needed to load the data into a VAO. The above datatypes
	// make it easier to manipulate the positions of vertex data in the lists.
	// i.e. a vector3f will group three position values for a vertex together so they
	// can be more easily manipulated than the serialised equivalent.
	private float[] positionsArray;
	private float[] normalsArray;
	private float[] texCoordsArray;
	private int[] jointIdsArray;
	private float[] jointWeightsArray;
	
	// One for each joint.
	private List<Matrix4f> inverseBindTransforms = new ArrayList<Matrix4f>();

}
