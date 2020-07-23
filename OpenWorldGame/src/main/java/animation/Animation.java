package animation;

import java.util.List;

import window.Window;

public class Animation {
	
	private List<KeyFrame> keyFrames;
	private int currentFrame;
	private String name;
	private double duration;
	
	private double timeCounter = 0;
	
	public Animation(String name, List<KeyFrame> keyFrames, double duration) {
		this.name = name;
		this.keyFrames = keyFrames;
		this.duration = duration;
		currentFrame = 0;
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
	
	public void update() {
		timeCounter += Window.getLastFrameTime();
		
		double frameLength = duration / (keyFrames.size() * 200);
		System.out.println(frameLength);
		
		while (timeCounter >= frameLength) {
			currentFrame++;
			timeCounter -= frameLength;
			currentFrame %= keyFrames.size();
		}
		
		
		
		
		
//		int nextFrame = currentFrame + 1;
//		if (nextFrame > keyFrames.size() - 1) {
//			currentFrame = 0;
//		} else {
//			currentFrame = nextFrame;
//		}
	}
}
