package shaders;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import entities.Camera;
import entities.Light;
import toolbox.Maths;

public class AnimatedModelShader extends ShaderProgram {
	
	// max number of joints in a skeleton. used for setting the size of the array.
	//TODO currently set also in keyFrame class.
	private static final int MAX_JOINTS = 150;
	private static final int MAX_LIGHTS = 4;
	
	private static final String VERTEX_FILE = "src/main/java/shaders/animatedEntityVertex.txt";
	private static final String FRAGMENT_FILE = "src/main/java/shaders/animatedEntityFragment.txt";
	
	private int[] location_jointTransforms;
	private int[] location_lightPosition;
	private int[] location_lightColour;
	private int[] location_lightAttenuation;
	private int location_projectionViewMatrix;
	
	private int location_modelMatrix;
	private int location_viewMatrix;
	private int location_projectionMatrix;

	public AnimatedModelShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionViewMatrix = super.getUniformLocation("projectionView");
		location_jointTransforms = new int[MAX_JOINTS];
		
		for (int i=0; i<MAX_JOINTS; i++) {
			location_jointTransforms[i] = super.getUniformLocation("jointTransforms[" + i + "]");
		}
		
		location_lightPosition = new int[MAX_LIGHTS];
		for (int i=0; i<MAX_LIGHTS; i++) {
			location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
		}
		
		location_lightColour = new int[MAX_LIGHTS];
		for (int i=0; i<MAX_LIGHTS; i++) {
			location_lightColour[i] = super.getUniformLocation("lightColour[" + i + "]");
		}
		
		location_lightAttenuation = new int[MAX_LIGHTS];
		for (int i=0; i<MAX_LIGHTS; i++) {
			location_lightAttenuation[i] = super.getUniformLocation("lightAttenuation[" + i + "]");
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
	
	public void loadLights(List<Light> lights) {
		for (int i=0; i<MAX_LIGHTS; i++) {
			if (i < lights.size()) {
				super.load3DVector(location_lightPosition[i], lights.get(i).getPosition());
				super.load3DVector(location_lightColour[i], lights.get(i).getColour());
				super.load3DVector(location_lightAttenuation[i], lights.get(i).getAttenuation());
			} else {
				// Loading 'empty' lights for the shader to use.
				super.load3DVector(location_lightPosition[i], new Vector3f(0, 0, 0));
				super.load3DVector(location_lightColour[i], new Vector3f(0, 0, 0));
				super.load3DVector(location_lightAttenuation[i], new Vector3f(1, 0, 0));
			}
		}
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
