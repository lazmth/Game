package models.animatedModel;

import java.util.Map;

import animation.Animation;
import models.TexturedModel;

public class AnimatedModel {
	
	private TexturedModel[] meshes;
	private Map<String, Animation> animations;
	private Animation currentAnimation;
	
	/**
	 * An animated model can be made up of multiple seperate meshes, which
	 * are essentially just seperate textured models. This is done in 
	 * modelling software when to ease the creation of models.
	 * 
	 * @param meshes
	 * @param animations
	 */
	public AnimatedModel(TexturedModel[] meshes, Map<String, Animation> animations) {
		this.meshes = meshes;
		this.animations = animations;
	}
	
	public Object[] getAvailableAnimations() {
		// Cannot upcast to string.
		return animations.keySet().toArray();
	}
	
	public Animation getAnimation(String name) {
		return animations.get(name);
	}
	
	public Animation getCurrentAnimation() {
		return currentAnimation;
	}
	
	public void setCurrentAnimation(Animation animation) {
		this.currentAnimation = animation;
	}
	
	public TexturedModel[] getMeshes() {
		return meshes;
	}
}
