package entities;

import org.joml.Vector3f;

import animation.Animation;
import models.animatedModel.AnimatedModel;

public class AnimatedEntity {

	AnimatedModel model;
	
	private Vector3f position;
	private float rotX;
	private float rotY;
	private float rotZ;
	private float scale;
	
	public AnimatedEntity(AnimatedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}
	
	public void updateAnimation() {
		model.getCurrentAnimation().update();
	}
	
	public void setCurrentAnimation(String animationName) {
		Animation animation = model.getAnimation(animationName);
		model.setCurrentAnimation(animation);
		model.getCurrentAnimation().reset();
	}

	public void increasePosition(float dx, float dy, float dz) {
		this.position.x+= dx;
		this.position.y+= dy;
		this.position.z+= dz;
	}
	
	public void increaseRotation(float dx, float dy, float dz) {
		this.rotX+= dx;
		this.rotY+= dy;
		this.rotZ+= dz;
	}
	
	public AnimatedModel getModel() {
		return model;
	}
	public void setModel(AnimatedModel model) {
		this.model = model;
	}
	public Vector3f getPosition() {
		return position;
	}
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	public float getRotX() {
		return rotX;
	}
	public void setRotX(float rotX) {
		this.rotX = rotX;
	}
	public float getRotY() {
		return rotY;
	}
	public void setRotY(float rotY) {
		this.rotY = rotY;
	}
	public float getRotZ() {
		return rotZ;
	}
	public void setRotZ(float rotZ) {
		this.rotZ = rotZ;
	}
	public float getScale() {
		return scale;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}
}
