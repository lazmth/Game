package skybox;

import org.joml.Vector3f;
import org.joml.Matrix4f;

import entities.Camera;
import shaders.ShaderProgram;
import toolbox.Maths;
import window.Window;

public class SkyboxShader extends ShaderProgram{

	private static final String VERTEX_FILE = "src/main/java/skybox/skyboxVertexShader.txt";
	private static final String FRAGMENT_FILE = "src/main/java/skybox/skyboxFragmentShader.txt";
	
	// Measured in degrees per second.
	private final float ROTATION_SPEED = 0.4f;
	
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_fogColour;
	private int location_cubeMap;
	private int location_cubeMap2;
	private int location_blendFactor;
	
	private float currentRotation = 0;
	
	public SkyboxShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix){
		super.loadMatrix(location_projectionMatrix, matrix);
	}

	public void loadViewMatrix(Camera camera){
		// No translation is applied in relation to the camera.
		Matrix4f matrix = Maths.createViewMatrix(camera);
		matrix.m30(0);
		matrix.m31(0);
		matrix.m32(0);
		
		currentRotation += ROTATION_SPEED * Window.getLastFrameTime();
		matrix.rotate((float) Math.toRadians(currentRotation), new Vector3f(0, 1, 0));
		
		super.loadMatrix(location_viewMatrix, matrix);
	}
	
	public void loadFogColour(float r, float g, float b) {
		super.load3DVector(location_fogColour, new Vector3f(r, g, b));
	}
	
	public void connectTextureUnits() {
		// Telling each sampler which texture unit to use.
		super.loadInt(location_cubeMap, 0);
		super.loadInt(location_cubeMap2, 1);
	}
	
	public void loadBlendFactor(float blendFactor) {
		super.loadFloat(location_blendFactor, blendFactor);
	}
	
	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_fogColour = super.getUniformLocation("fogColour");
		location_cubeMap = super.getUniformLocation("cubeMap");
		location_cubeMap2 = super.getUniformLocation("cubeMap2");
		location_blendFactor = super.getUniformLocation("blendFactor");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

}
