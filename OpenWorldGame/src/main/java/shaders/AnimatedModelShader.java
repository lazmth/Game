package shaders;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import entities.Camera;
import toolbox.Maths;

public class AnimatedModelShader extends ShaderProgram {
	
	// max number of joints in a skeleton. used for setting the size of the array.
	private static final int MAX_JOINTS = 13;
	
	private static final String VERTEX_FILE = "src/main/java/shaders/animatedEntityVertex.txt";
	private static final String FRAGMENT_FILE = "src/main/java/shaders/animatedEntityFragment.txt";
	
	private int[] location_jointTransforms;
	private int location_projectionViewMatrix;
	private int location_lightDirection; //TODO add dynamic lighting.
	
	private int location_modelMatrix;
	private int location_viewMatrix;
	private int location_projectionMatrix;

	public AnimatedModelShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionViewMatrix = super.getUniformLocation("projectionView");
		location_lightDirection = super.getUniformLocation("lightDirection");
		location_jointTransforms = new int[MAX_JOINTS];
		
		for (int i=0; i<MAX_JOINTS; i++) {
			location_jointTransforms[i] = super.getUniformLocation("jointTransforms[" + i + "]");
		}
		
		location_modelMatrix = super.getUniformLocation("modelMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
	}

	@Override
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
		super.load3DVector(location_lightDirection, lightPosition);
	}
	
	public void loadProjectionViewMatrix(Matrix4f projectionViewMatrix) {
		super.loadMatrix(location_projectionViewMatrix, projectionViewMatrix);
	}
	public void loadProjectionMatrix(Matrix4f projection) {
		super.loadMatrix(location_projectionMatrix, projection);
	}

	public void loadModelMatrix(Matrix4f model) {
		super.loadMatrix(location_modelMatrix, model);
	}
	
	public void loadViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}

}
