package entities;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import window.Window;

public class Camera {

	private final Player PLAYER;
	
	private float distanceFromPlayer = 90;
	private float angleAroundPlayer = 0;

	private Vector3f position = new Vector3f(0.0f);
	// Pitch is how high/low the camera is aimed. Rotation around x-axis.
	private float pitch = 20;
	// Yaw is how much left/right it is facing. Rotation around y-axis.
	private float yaw = 135;
	// Roll is rotation around the z-axis.
	private float roll;
	
	public Camera(Player player) {
		this.PLAYER = player;
	}
	
	public void move() {
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180 - (PLAYER.getRotY() + angleAroundPlayer);
	}
	
	public void invertPitch() {
		pitch = -pitch;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	public float getPitch() {
		return pitch;
	}
	public float getYaw() {
		return yaw;
	}
	public float getRoll() {
		return roll;
	}
	
	private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
		// Offsets are relative to the player position.
		float theta = PLAYER.getRotY() + angleAroundPlayer;
		float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));
		position.x = PLAYER.getPosition().x - offsetX;
		position.z = PLAYER.getPosition().z - offsetZ;
		position.y = PLAYER.getPosition().y + verticalDistance;
	}
	
	private float calculateHorizontalDistance() {
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
	}
	
	private float calculateVerticalDistance() {
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
	}
	
	private void calculateZoom() {
		float zoomLevel = (float) (Window.getScrollDelta() * 0.8f);
		distanceFromPlayer -= zoomLevel;
		Window.resetScrollDelta();
	}
	
	private void calculatePitch() {
		if (Window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
			float pitchChange = (float) (Window.getMouseDelta()[1] * 0.1f);
			pitch += pitchChange;
		}
	}
	
	private void calculateAngleAroundPlayer() {
		if (Window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			float angleChange = (float) (Window.getMouseDelta()[0] * 0.3f);
			angleAroundPlayer -= angleChange;
		}
	}
}
