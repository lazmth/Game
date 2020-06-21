package loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import models.RawModel;
import textures.TextureData;

/**
 * For loading various objects into video memory (into a VAO).
 * @author Billy
 *
 */
public class Loader {
	
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();
	
	/**
	 * For loading 2D vertex information. Barebones with no lighting normals etc.
	 * @param positions
	 * @return
	 */
	public RawModel loadToVAO(float[] positions, int dimensions) {
		int vaoID = createVAO();
		this.storeDatainAttributeList(0, dimensions, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length/2);
	}
	
	/*
	 * For use with font rendering.
	 */
	public int loadToVAO(float[] positions, float[] textureCoords) {
		int vaoID = createVAO();
		// Added to our list to keep track of it.
		vaos.add(vaoID);
		storeDatainAttributeList(0, 2, positions);
		storeDatainAttributeList(1, 2, textureCoords);
		unbindVAO();
		
		return vaoID;
	}
	
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDatainAttributeList(0, 3, positions);
		storeDatainAttributeList(1, 2, textureCoords);
		storeDatainAttributeList(2, 3, normals);
		unbindVAO();
		return new RawModel(vaoID, indices.length);
	}
	
	/**
	 * Also loads tangent information. Useful for normal mapping.
	 */
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, float[] tangents,
			int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDatainAttributeList(0, 3, positions);
		storeDatainAttributeList(1, 2, textureCoords);
		storeDatainAttributeList(2, 3, normals);
		storeDatainAttributeList(3, 3, tangents);
		unbindVAO();
		return new RawModel(vaoID, indices.length);
	}
	
	public int loadTexture(String fileName) {
		TextureData texture = unpackTextureFile(fileName);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_2D, texture.getTextureID());

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texture.getWidth(),
				texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texture.getBuffer());
		// mipmap generation.
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		// turning mipmaps on for this texture.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		// setting level-of-detail bias. a negative value makes the textures render at a higher resolution.
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -1);
		
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}
	
	//TODO combine this with the other method.
	public int loadCubeMap(String[] textureFiles) {
		// Creating an empty texture and storing its ID.
		int textureID = GL11.glGenTextures();
		// Activating texture unit 0.
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		// Binding the created texture to texture unit 0.
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureID);
		
		for (int i=0; i<textureFiles.length; i++) {
			TextureData data = unpackCubeMapTextureFile(textureFiles[i]);
			
			// Loading texture data into the cubemap.
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(),
					0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
		}
		
		// Makes the textures a bit smoother.
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		// Used to fix visible seams between the skybox cube faces.
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		textures.add(textureID);
		return textureID;
	}
	
	public void cleanUp() {
		for(int vao:vaos) {
			GL30.glDeleteVertexArrays(vao);
		}
		for(int vbo:vbos) {
			GL30.glDeleteBuffers(vbo);
		}
		for(int texture:textures) {
			GL11.glDeleteTextures(texture);
		}
	}
	
	private int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	};
	
	private void storeDatainAttributeList(int attributeNumber, int coordSize, float[] data) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Loading an indices buffer. This can be added to our VAOs, where it can act as
	 * a list of indices when joining up the vertices in our models.
	 * @param indices
	 */
	private void bindIndicesBuffer(int[] indices) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		// This array type tells OpenGL that its elements are to be used as indices.
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		// We don't need to point this to a vertex attribute. Each VAO has a special slot for
		// a single index buffer to use.
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	private void unbindVAO() {
		GL30.glBindVertexArray(0);
	}
	
	private TextureData unpackCubeMapTextureFile(String fileName) {
		ByteBuffer imageData = fileToBuffer(fileName);
		
		// Buffers for holding the named values after being extracted in the decoding process.
		IntBuffer x = BufferUtils.createIntBuffer(1); 
		IntBuffer y = BufferUtils.createIntBuffer(1); 
		IntBuffer channels_in_file = BufferUtils.createIntBuffer(1); 
		
		// Decoding the image data and loading the pixel data into a byte buffer.
		// Desired channels in 4. We are expecting RGBA.
		ByteBuffer decodedImageData = STBImage.stbi_load_from_memory(imageData, x, y, channels_in_file, 4);
		
		int width = x.get();
		int height = y.get();
		
		return new TextureData(decodedImageData, width, height);
	}
	
	private TextureData unpackTextureFile(String fileName) {
		ByteBuffer imageData = fileToBuffer(fileName);
		
		// Buffers for holding the named values after being extracted in the decoding process.
		IntBuffer x = BufferUtils.createIntBuffer(1); 
		IntBuffer y = BufferUtils.createIntBuffer(1); 
		IntBuffer channels_in_file = BufferUtils.createIntBuffer(1); 
		
		// Decoding the image data and loading the pixel data into a byte buffer.
		// Desired channels in 4. We are expecting RGBA.
		ByteBuffer decodedImageData = STBImage.stbi_load_from_memory(imageData, x, y, channels_in_file, 4);
		
		int width = x.get();
		int height = y.get();
		
		// Each new texture will come with a place in memory in which we can later load it.
		int textureID = GL11.glGenTextures();
		
		return new TextureData(decodedImageData, width, height, textureID);
	}
	
	/**
	 * Reads data from a file into a byte buffer.
	 * @param fileName
	 * @return
	 */
	private ByteBuffer fileToBuffer(String fileName) {
		RandomAccessFile textureFile = null;
		try {
			textureFile = new RandomAccessFile("src/main/resources/"+fileName+".png", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Tried open file but failed.");
		}
		
		FileChannel fileChannel = textureFile.getChannel();
		
		ByteBuffer byteBuffer = null;
		try {
			byteBuffer = MemoryUtil.memAlloc((int) textureFile.length());
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Failed to allocate byte buffer.");
		}
		
		int bytesRead = 0;
		try {
			bytesRead = fileChannel.read(byteBuffer);
			while (bytesRead != 0) {
				bytesRead = fileChannel.read(byteBuffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to read from fileChannel");
		}
		byteBuffer.flip();
		
		try {
			fileChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("fileChannel failed to close");
		}
		return byteBuffer;
	}
	
}
