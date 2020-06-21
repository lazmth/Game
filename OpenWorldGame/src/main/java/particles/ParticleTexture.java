package particles;

public class ParticleTexture {
	
	private int textureID;
	private int numerOfRows;
	
	public ParticleTexture(int textureID, int numerOfRows) {
		super();
		this.textureID = textureID;
		this.numerOfRows = numerOfRows;
	}

	protected int getTextureID() {
		return textureID;
	}

	protected int getNumerOfRows() {
		return numerOfRows;
	}
	
}
