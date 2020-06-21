package animation;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import toolbox.Quaternion;

public class JointTransform {
	
	private Vector3f position;
	private Quaternion rotation;
	
	/**
	 * 
	 * @param position
	 * 		- Position of the joint relative to the parent joint.
	 * @param rotation
	 * 		- Rotation of the joint relative to the parent.
	 */
	public JointTransform(Vector3f position, Quaternion rotation) {
		this.position = position;
		this.rotation = rotation;
	}
	
	/**
	 * 
	 * @return
	 * 		- The _matrix_ representation of this joint transformation.
	 * 		  Still local (bone-space).
	 */
	public Matrix4f getLocalTransform() {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(position);
		matrix.mul(rotation.toRotationMatrix());
		return matrix;
	}
	
	/**
	 * Interpolates between two transforms based on the progression value. The
	 * result is a new transform which is part way between the two original
	 * transforms. The translation can simply be linearly interpolated, but the
	 * rotation interpolation is slightly more complex, using a method called
	 * "SLERP" to spherically-linearly interpolate between 2 quaternions
	 * (rotations). This gives a much much better result than trying to linearly
	 * interpolate between Euler rotations.
	 * 
	 * @param frameA
	 *            - the previous transform
	 * @param frameB
	 *            - the next transform
	 * @param progression
	 *            - a number between 0 and 1 indicating how far between the two
	 *            transforms to interpolate. A progression value of 0 would
	 *            return a transform equal to "frameA", a value of 1 would
	 *            return a transform equal to "frameB". Everything else gives a
	 *            transform somewhere in-between the two.
	 * @return
	 */
	protected static JointTransform interpolate(JointTransform frameA, JointTransform frameB, float progression) {
		Vector3f pos = interpolate(frameA.position, frameB.position, progression);
		Quaternion rot = Quaternion.interpolate(frameA.rotation, frameB.rotation, progression);
		return new JointTransform(pos, rot);
	}

	/**
	 * Linearly interpolates between two translations based on a "progression"
	 * value.
	 * 
	 * @param start
	 *            - the start translation.
	 * @param end
	 *            - the end translation.
	 * @param progression
	 *            - a value between 0 and 1 indicating how far to interpolate
	 *            between the two translations.
	 * @return
	 */
	private static Vector3f interpolate(Vector3f start, Vector3f end, float progression) {
		float x = start.x + (end.x - start.x) * progression;
		float y = start.y + (end.y - start.y) * progression;
		float z = start.z + (end.z - start.z) * progression;
		return new Vector3f(x, y, z);
	}

}
