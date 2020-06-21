package shaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class ShaderProgram {
	
	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;
	
	// 16 floats because we are using 4x4 matrices.
	// We need a float buffer for loading into matrix uniform variables.
	private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	public ShaderProgram(String vertexFile, String fragmentFile) {
		vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		getAllUniformLocations();
	}
	
	protected abstract void getAllUniformLocations();
	
	/*
	 * uniformName should be the exact name as it appears in the shader code.
	 */
	protected int getUniformLocation(String uniformName) {
		return GL20.glGetUniformLocation(programID, uniformName);
	}
	
	public void start() {
		GL20.glUseProgram(programID);
	}
	
	public void stop() {
		GL20.glUseProgram(0);
	}
	
	public void cleanUp() {
		stop();
		// Memory management. Removing our data from memory.
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}
	
	// Links up inputs to shader programs to one of attributes of a VAO we pass in.
	protected abstract void bindAttributes();
	
	protected void bindAttribute(int attribute, String variableName) {
		// Takes in the number of the attribute list in VAO we want to bind.
		// Also the variable name in the shader code we want to bind to.
		GL20.glBindAttribLocation(programID, attribute, variableName);
	}
	
	// Methods for loading various types of value into uniform variables.
	
	protected void loadInt(int location, int value) {
		GL20.glUniform1i(location, value);
	}
	
	protected void loadFloat(int location, float value) {
		GL20.glUniform1f(location, value);
	}
	
	protected void load2DVector(int location, Vector2f vector) {
		GL20.glUniform2f(location, vector.x, vector.y);
	}
	
	protected void load3DVector(int location, Vector3f vector) {
		// Loading a 3-float uniform variable.
		GL20.glUniform3f(location, vector.x, vector.y, vector.z);
	}
	
	protected void load4DVector(int location, Vector4f vector) {
		// This is the correct parameter order from a Vector4f.
		GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
	}
	
	protected void loadBoolean(int location, boolean value) {
		// Shader code doesn't have an explicit boolean type. We will use 0 or 1.
		float toLoad = 0;
		
		if(value) {
			toLoad = 1;
		}
		GL20.glUniform1f(location, toLoad);
	}
	
	protected void loadMatrix(int location, Matrix4f matrix) {
		// Storing the matrix into the given float buffer.
		matrix.get(matrixBuffer);
		GL20.glUniformMatrix4fv(location, false, matrixBuffer);
	}
	
	/////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("deprecation")
	private static int loadShader(String file, int type) {
		// Type is fragment or vertex shader.
		
		StringBuilder shaderSource = new StringBuilder();
		
		try {
			File openedFile = new File(file);
			System.out.println(openedFile.getAbsolutePath());
			FileReader fr = new FileReader(openedFile);
			BufferedReader reader = new BufferedReader(fr);
			String line;
			
			while((line = reader.readLine())!=null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch(IOException e) {
			System.err.println("Could not read file!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Creating space in memory for the shader of given type.
		int shaderID = GL20.glCreateShader(type);
		// Loading source into the shader and compiling.
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID,  GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader.");
			System.exit(-1);
		}
		return shaderID;
	}

}
