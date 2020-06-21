package animation;

public class Animation {
	
	private final float length;
	private final KeyFrame[] keyFrames;
	
	/**
	 * 
	 * @param length
	 * 		- The total length of the animation, in seconds.
	 * @param keyFrames
	 * 		- An array of key frames which make up the animation.
	 */
	public Animation(float length, KeyFrame[] keyFrames) {
		this.length = length;
		this.keyFrames = keyFrames;
	}

	public float getLength() {
		return length;
	}

	public KeyFrame[] getKeyFrames() {
		return keyFrames;
	}
	
}
