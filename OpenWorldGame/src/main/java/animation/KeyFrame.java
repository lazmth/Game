package animation;

import java.util.Map;

public class KeyFrame {

	private final float timeStamp;
	private final Map<String, JointTransform> pose;
	
	/**
	 * 
	 * @param timeStamp
	 * 		- The time (in seconds) that this keyframe occurs into an
	 * 		  animation.
	 * @param pose
	 * 		- The position and orientation of each joint during the keyframe,
	 * 		  given as a joint transformation. Indexed by the joint name to
	 * 		  which they apply.
	 */
	public KeyFrame(float timeStamp, Map<String, JointTransform> pose) {
		this.timeStamp = timeStamp;
		this.pose = pose;
	}

	public float getTimeStamp() {
		return timeStamp;
	}

	/**
	 * 
	 * @return - 
	 */
	public Map<String, JointTransform> getPose() {
		return pose;
	}

}
