package animation;

import java.util.Arrays;

import org.joml.Matrix4f;

public class KeyFrame {
	
	public static final int MAX_JOINTS = 150;
	
	private final Matrix4f[] jointMatrices;
	
	public KeyFrame() {
		jointMatrices = new Matrix4f[MAX_JOINTS];
		Arrays.fill(jointMatrices, new Matrix4f());
	}
	
	public Matrix4f[] getJointMatrices() {
		return jointMatrices;
	}
	
	public void setMatrix(int pos, Matrix4f jointMatrix) {
		jointMatrices[pos] = jointMatrix;
	}

}
