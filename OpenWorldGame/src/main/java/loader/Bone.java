package loader;

import org.joml.Matrix4f;

public class Bone {
	
	private final int boneID;
	private final String boneName;
	
	private Matrix4f offsetMatrix;
	
	public Bone(int boneID, String boneName, Matrix4f offsetMatrix) {
		this.boneID = boneID;
		this.boneName = boneName;
		this.offsetMatrix = offsetMatrix; // ? bind matrix?
	}

	public int getBoneID() {
		return boneID;
	}

	public String getBoneName() {
		return boneName;
	}

	public Matrix4f getOffsetMatrix() {
		return offsetMatrix;
	}

}
