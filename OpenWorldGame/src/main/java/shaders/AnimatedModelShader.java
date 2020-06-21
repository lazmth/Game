package shaders;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import entities.Camera;
import toolbox.Maths;

public class AnimatedModelShader extends ShaderProgram {
	
	// max number of joints in a skeleton. used for setting the size of the array.
	private static final int MAX_JOINTS = 50;
	
	private static final String VERTEX_FILE = "/shaders/animatedModelVertex.glsl";
	private static final String FRAGMENT_FILE = "/shaders/animatedModelFragment.glsl";
	
	private int[] location_jointTransforms;
	private int location_projectionViewMatrix;
	private int location_lightPosition; //TODO add dynamic lighting.
	
	
	public AnimatedModelShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	protected void getAllUniformLocations() {
		location_projectionViewMatrix = super.getUniformLocation("projectionViewMatrix");
		location_lightPosition = super.getUniformLocation("lightPosition");
		location_jointTransforms = new int[MAX_JOINTS];
		
		for (int i=0; i<MAX_JOINTS; i++) {
			location_jointTransforms[i] = super.getUniformLocation("jointTransforms[" + i + "]");
		}
	}
	
	protected void bindAttributes() {
		super.bindAttribute(0, "in_position");
		super.bindAttribute(1, "in_textureCoords");
		super.bindAttribute(2, "in_normal");
		super.bindAttribute(3, "in_jointIndices");
		super.bindAttribute(4, "in_weights");
	}
	
	public void loadJointTransforms(Matrix4f[] jointTransforms) {
		for (int i=0; i<MAX_JOINTS; i++) {
			super.loadMatrix(location_jointTransforms[i], jointTransforms[i]);
		}
	}
	
	public void loadLight(Vector3f lightPosition) {
		super.load3DVector(location_lightPosition, lightPosition);
	}
	
	public void loadProjectionViewMatrix(Matrix4f projectionViewMatrix) {
		super.loadMatrix(location_projectionViewMatrix, projectionViewMatrix);
	}
	
}
