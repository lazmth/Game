package particles;

import org.joml.Vector2f;
import org.joml.Vector3f;

import entities.Camera;
import entities.Player;
import window.Window;

public class Particle {

	private Vector3f position;
	private Vector3f velocity;
	// A value of less than 1 will give a floaty effect.
	private float gravityEffect;
	private float lifeLength;
	private float rotation;
	private float scale;
	
	private ParticleTexture texture;
	
	// Texture offsets for current and next animation stage.
	private Vector2f texOffset1 = new Vector2f();
	private Vector2f texOffset2 = new Vector2f();
	private float blendFactor;
	
	private float elapsedAliveTime = 0;
	private float distanceFromCameraSq;

	/**
	 * When a particle is created, it is automatically added to the particle master.
	 */
	public Particle(ParticleTexture texture, Vector3f position, Vector3f velocity, 
			float gravityEffect, float lifeLength, float rotation, float scale) {
		super();
		this.texture = texture;
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		ParticleMaster.addParticle(this);
	}
	
	/*
	 * Returns boolean representing whether the particle has exceeded its
	 * life length.
	 */
	protected boolean update(Camera camera) {
		velocity.y += Player.GRAVITY * gravityEffect * Window.getLastFrameTime();
		Vector3f change = new Vector3f(velocity);
		// Velocity is of course a rate. Scale the vector by time to get displacement.
		change.mul(Window.getLastFrameTime());
		position.add(change);
		updateTextureCoordInfo();
		
		// Calculating distance.
		Vector3f vectorDifference = new Vector3f(0.0f);
		camera.getPosition().sub(position, vectorDifference);
		distanceFromCameraSq = vectorDifference.lengthSquared();
		
		elapsedAliveTime += Window.getLastFrameTime();
		
		return elapsedAliveTime < lifeLength;
	}

	protected Vector3f getPosition() {
		return position;
	}

	protected float getRotation() {
		return rotation;
	}

	protected float getScale() {
		return scale;
	}

	protected ParticleTexture getTexture() {
		return texture;
	}

	protected Vector2f getTexOffset1() {
		return texOffset1;
	}

	protected Vector2f getTexOffset2() {
		return texOffset2;
	}

	protected float getBlendFactor() {
		return blendFactor;
	}
	
	private void updateTextureCoordInfo() {
		float lifeFactor = elapsedAliveTime / lifeLength;
		int stageCount = texture.getNumerOfRows() * texture.getNumerOfRows();
		float atlasProgression = lifeFactor * stageCount;
		int index1 = (int) Math.floor(atlasProgression);
		// The next index is index1 + 1, unless index1 is the last index - then remain
		// at index 1.
		int index2 = index1 < stageCount - 1 ? index1 + 1 : index1;
		this.blendFactor = atlasProgression % 1;
		setTextureOffset(texOffset1, index1);
		setTextureOffset(texOffset2, index2);
	}
	
	private void setTextureOffset(Vector2f offset, int index) {
		int column = index % texture.getNumerOfRows();
		int row = index / texture.getNumerOfRows();
		offset.x = (float) column / texture.getNumerOfRows();
		offset.y = (float) row / texture.getNumerOfRows();
	}

	protected float getDistanceFromCamera() {
		return distanceFromCameraSq;
	}

}
