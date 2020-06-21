package guis;

import org.joml.Vector2f;

public class GuiTexture {

	private int textureID;
	private Vector2f positon;
	private Vector2f scale;
	
	public GuiTexture(int textureID, Vector2f positon, Vector2f scale) {
		super();
		this.textureID = textureID;
		this.positon = positon;
		this.scale = scale;
	}
	
	public int getTextureID() {
		return textureID;
	}
	public Vector2f getPositon() {
		return positon;
	}
	public Vector2f getScale() {
		return scale;
	}
	
	
}
