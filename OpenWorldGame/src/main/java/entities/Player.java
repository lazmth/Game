package entities;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import models.TexturedModel;
import models.animatedModel.AnimatedModel;
import terrain.Terrain;
import window.Window;

public class Player extends AnimatedEntity {
	
	private static float runSpeed;
	private static float turnSpeed = 160;
	private static float verticalSpeed = 20;
	private static float jumpPower = 30;
	
	public static float GRAVITY = -40;
	
	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float currentVerticalSpeed = 0;
	
	public Player(AnimatedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
		
	}
	
	public void move(Terrain terrain) {
		checkInputs();
		super.increaseRotation(0, currentTurnSpeed * Window.getLastFrameTime(), 0);
		
		float horizontalDistance = currentSpeed * Window.getLastFrameTime();
		float dx = (float) (horizontalDistance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (horizontalDistance * Math.cos(Math.toRadians(super.getRotY())));

		currentVerticalSpeed += GRAVITY * Window.getLastFrameTime();
		float dy = currentVerticalSpeed * Window.getLastFrameTime();
		
		super.increasePosition(dx, dy, dz);
		
		float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x,
				super.getPosition().z);
		if (super.getPosition().y < terrainHeight) {
			currentVerticalSpeed = 0;
			super.getPosition().y = terrainHeight;
		}	
	}
	
	public void checkInputs() {
		if (Window.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			runSpeed = 200;
		} else {
			runSpeed = 40;
		}
		
		if (Window.isKeyPressed(GLFW.GLFW_KEY_W)) {
			currentSpeed = runSpeed;
		} else if (Window.isKeyPressed(GLFW.GLFW_KEY_S)) {
			currentSpeed = -runSpeed;
		} else {
			currentSpeed = 0;
		}
		

		if (Window.isKeyPressed(GLFW.GLFW_KEY_D)) {
			currentTurnSpeed = -turnSpeed;
		} else if (Window.isKeyPressed(GLFW.GLFW_KEY_A)) {
			currentTurnSpeed = turnSpeed;
		} else {
			currentTurnSpeed = 0;
		}
		
		if (Window.isKeyPressed(GLFW.GLFW_KEY_R)) {
			currentVerticalSpeed += verticalSpeed;
		} else if (Window.isKeyPressed(GLFW.GLFW_KEY_F)) {
			currentVerticalSpeed += -verticalSpeed;
		}
	}

}
