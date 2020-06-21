package entities;

import org.joml.Vector3f;

import entities.Entity;
import models.TexturedModel;

public class Light {
	
	private Vector3f position;
	private Vector3f colour;
	// Values in the region of (1,0.001f,0.002f) are good.
	// These are coefficients to a quadratic equation for the
	// attenuation factor.
	private Vector3f attenuation = new Vector3f(1, 0, 0);
	
	/*
	 * By default lights don't have attenuation.
	 */
	public Light(Vector3f position, Vector3f colour) {
		super();
		this.position = position;
		this.colour = colour;
		
	}
	
	public Light(Vector3f position, Vector3f colour, Vector3f attenuation) {
		super();
		this.position = position;
		this.colour = colour;
		this.attenuation = attenuation;
	}
	
	public Vector3f getAttenuation() {
		return attenuation;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getColour() {
		return colour;
	}
	
	public void setColour(Vector3f colour) {
		this.colour = colour;
	}
	
}
