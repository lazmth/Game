package animation;

import java.util.List;

public class Animation {
	
	private List<KeyFrame> keyFrames;
	private int currentFrame;
	private String name;
	private double duration;
	
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
		int nextFrame = currentFrame + 1;
		if (nextFrame > keyFrames.size() - 1) {
			currentFrame = 0;
		} else {
			currentFrame = nextFrame;
		}
	}
}
