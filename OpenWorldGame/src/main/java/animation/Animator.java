package animation;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;

import models.animatedModel.AnimatedModel;
import models.animatedModel.Joint;
import window.Window;

public class Animator {
	
	// An animator instance works to animate just one entity.
	private final AnimatedModel entity;
	
	private Animation currentAnimation;
	private float animationTimeProgress = 0;
	
	public Animator(AnimatedModel entity) {
		this.entity = entity;
	}
	
	/**
	 * There will always be an animation running. This method then resets the
	 * animation timer and starts the given animation.
	 * @param animation
	 * 		- The animation to be run.
	 */
	public void doAnimation(Animation animation) {
		animationTimeProgress = 0;
		currentAnimation = animation;
	}
	
	public void update() {
		if (currentAnimation == null) {
			return;
		}
		increaseAnimationTime();
		Map<String, Matrix4f> currentPose = calculateCurrentAnimationPose();
		// The last parameter here is the parent joint's transformation matrix. As the 
		// root joint has no parent, we use the identity matrix here.
		applyPoseToJoints(currentPose, entity.getRootJoint(), new Matrix4f());
	}
	
	private void increaseAnimationTime() {
		animationTimeProgress += Window.getLastFrameTime();
		// The animation will loop.
		if (animationTimeProgress > currentAnimation.getLength()) {
			this.animationTimeProgress %= currentAnimation.getLength();
		}
	}
	
	/**
	 * Watch the tutorial video to understand the maths here.
	 */
	private void applyPoseToJoints(Map<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
		Matrix4f currentLocalTransform = currentPose.get(joint.name);
		// Current transform ends up in model space.
		Matrix4f currentTransform = new Matrix4f();
		//TODO maybe wrong way around.
		parentTransform.mul(currentLocalTransform, currentTransform);
		for (Joint childJoint : joint.children) {
			applyPoseToJoints(currentPose, childJoint, currentTransform);
		}
		// Currently the transform is from the origin to the pose position. By multiplying it by the 
		// inverse bind transform, it is changed so it then goes from the bind position to the pose position.
		currentTransform.mul(joint.getInverseBindTransform());
		
		joint.setAnimationTransform(currentTransform);
	}
	
	/**
	 * Carries out the interpolation based on time through the animation.
	 */
	private Map<String, Matrix4f> calculateCurrentAnimationPose() {
		KeyFrame[] frames = getPreviousAndNextFrames();
		float progression = calculateProgression(frames[0], frames[1]);
		return interpolatePoses(frames[0], frames[1], progression);
	}
	
	private KeyFrame[] getPreviousAndNextFrames() {
		KeyFrame[] allFrames = currentAnimation.getKeyFrames();
		KeyFrame previousFrame = allFrames[0];
		KeyFrame nextFrame = allFrames[0];
		for (int i = 1; i < allFrames.length; i++) {
			nextFrame = allFrames[i];
			if (nextFrame.getTimeStamp() > animationTimeProgress) {
				break;
			}
			previousFrame = allFrames[i];
		}
		return new KeyFrame[] { previousFrame, nextFrame };
	}
	
	private float calculateProgression(KeyFrame previousFrame, KeyFrame nextFrame) {
		float totalTime = nextFrame.getTimeStamp() - previousFrame.getTimeStamp();
		float currentTime = animationTimeProgress - previousFrame.getTimeStamp();
		return currentTime / totalTime;
	}
	
	private Map<String, Matrix4f> interpolatePoses(KeyFrame previousFrame, KeyFrame nextFrame, float progression) {
		Map<String, Matrix4f> currentPose = new HashMap<String, Matrix4f>();
		for (String jointName : previousFrame.getPose().keySet()) {
			JointTransform previousTransform = previousFrame.getPose().get(jointName);
			JointTransform nextTransform = nextFrame.getPose().get(jointName);
			JointTransform currentTransform = JointTransform.interpolate(previousTransform, nextTransform, progression);
			currentPose.put(jointName, currentTransform.getLocalTransform());
		}
		return currentPose;
	}

}
