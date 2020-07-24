package animation;

import java.util.List;

import window.Window;

public class Animation {
	
	private List<KeyFrame> keyFrames;
	private int currentFrame;
	private String name;
	private double duration;
	
	private double timeCounter = 0;
	
	private double durationSeconds;
	
	private double speedFactor = 1.0f;
	
	/**
	 * 
	 * @param name
	 * @param keyFrames
	 * @param duration
	 * 	- Measured in 'ticks!'.
	 * @param ticksPerSec
	 */
	public Animation(String name, List<KeyFrame> keyFrames, double duration, double ticksPerSec) {
		this.name = name;
		this.keyFrames = keyFrames;
		this.duration = duration;
		currentFrame = 0;
		
		durationSeconds = duration / ticksPerSec;
	}

	public List<KeyFrame> getKeyFrames() {
		return keyFrames;
	}

	public int getCurrentFrame() {
		return currentFrame;
	}

	public String getName() {
		return name;
	}

	public double getDuration() {
		return duration;
	}
	
	public KeyFrame getNextFrame() {
		update();
		return this.keyFrames.get(currentFrame);
	}
	
	public void reset() {
		currentFrame = 0;
		timeCounter = 0;
	}
	
	public void increaseSpeedFactor() {
		this.speedFactor += 0.01f;
	}
	
	public void decreaseSpeedFactor() {
		double newSpeed = speedFactor - 0.01f;
		if (newSpeed > 0) {
			speedFactor = newSpeed;
		}
	}
	
	public void update() {
		timeCounter += Window.getLastFrameTime();
		
		double frameLength = durationSeconds / (keyFrames.size()) * speedFactor;
		
		while (timeCounter >= frameLength) {
			currentFrame++;
			timeCounter -= frameLength;
			currentFrame %= keyFrames.size();
		}
	}
}
