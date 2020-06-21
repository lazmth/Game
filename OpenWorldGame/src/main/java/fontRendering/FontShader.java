package fontRendering;

import org.joml.Vector2f;
import org.joml.Vector3f;

import shaders.ShaderProgram;

public class FontShader extends ShaderProgram{

	private static final String VERTEX_FILE = "src/main/java/fontRendering/fontVertex.txt";
	private static final String FRAGMENT_FILE = "src/main/java/fontRendering/fontFragment.txt";
	
	private int location_colour;
	private int location_translation;
	private int location_outlineColour;
	
	public FontShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_colour = super.getUniformLocation("colour");
		location_translation = super.getUniformLocation("translation");
		location_outlineColour = super.getUniformLocation("outlineColour");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
	}
	
	protected void loadOutlineColour(Vector3f borderColour) {
		super.load3DVector(location_outlineColour, borderColour);
	}
	
	protected void loadColour(Vector3f colour) {
		super.load3DVector(location_colour, colour);
	}
	
	protected void loadTranslation(Vector2f translation) {
		super.load2DVector(location_translation, translation);
	}


}
