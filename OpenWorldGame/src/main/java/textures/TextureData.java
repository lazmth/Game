package textures;

import java.nio.ByteBuffer;

public class TextureData {
	
	private int width;
	private int height;
	// Holds the decoded byte data of a texture image.
	private ByteBuffer buffer;
	private int textureID;
	
	public TextureData(ByteBuffer buffer, int width, int height, int textureID) {
		super();
		this.width = width;
		this.height = height;
		this.buffer = buffer;
		this.textureID = textureID;
	}
	
	public TextureData(ByteBuffer buffer, int width, int height) {
		super();
		this.width = width;
		this.height = height;
		this.buffer = buffer;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	public int getTextureID() {
		if (textureID == 0) {
			System.err.println("Tried to access a null texture ID. Cubemaps don't have one!");
			return 0;
		} else {
			return textureID;
		}
	}
	

}
