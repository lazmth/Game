package loader;

/**
 * Each vertex will have a bunch of these associated with it.
 * It describes the bones which affect the vertex and the weight with which
 * this happens.
 * @author Billy
 *
 */
public class VertexWeight {

	private int boneID;
	private int vertexID;
	private float weight;
	
	/**
	 * @param boneID
	 * 	- ID of the bone/joint affecting the vertex.
	 * @param vertexID
	 * 	- ID of the vertex affected by the bone/joint.
	 * @param weight
	 * 	- The weight with which the bone affects the vertex.
	 */
	public VertexWeight(
			int boneID, 
			int vertexID, 
			float weight)
	{
		this.boneID = boneID;
		this.vertexID = vertexID;
		this.weight = weight;
	}

	public int getBoneID() {
		return boneID;
	}

	public int getVertexID() {
		return vertexID;
	}

	public float getWeight() {
		return weight;
	}
	
}
