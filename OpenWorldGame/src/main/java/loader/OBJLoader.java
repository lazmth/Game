package loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import models.RawModel;
import loader.Loader;

public class OBJLoader {

	public static RawModel loadObjModel(String fileName, Loader loader) {
		FileReader fr = null;
		try {
			fr = new FileReader(new File("src/main/resources/"+fileName+".obj"));
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file!");
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(fr);
		String line;
		List<Vector3f> vertices = new ArrayList<Vector3f>();
		List<Vector2f> textures = new ArrayList<Vector2f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Integer> indices = new ArrayList<Integer>();
		// We will have to convert into an array when we know the size. VAOs need this format.
		float[] verticesArray = null;
		float[] normalsArray = null;
		float[] textureArray = null;
		int[] indicesArray = null;
		
		try {
			while(true) {
				line = reader.readLine();
						String[] currentLine = line.split(" ");
						if(line.startsWith("v ") ) {
							// vertex position
							Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
									Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
							vertices.add(vertex);
						} else if(line.startsWith("vt ")) {
							// vertex texture coordinate
							Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]),
									Float.parseFloat(currentLine[2]));
							textures.add(texture);
						} else if(line.startsWith("vn ")) {
							// vertex normal
							Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
									Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
							normals.add(normal);
						} else if(line.startsWith("f ")) {
							// We have now reached the face values and can start making the index array.
							textureArray = new float[vertices.size()*2];
							normalsArray = new float[vertices.size()*3];
							break;
						}
			}
			
			while(line!=null) {
				// Going through the face values.
				// A face line gives information about which vertices are grouped to make a triangle.
				if (!line.startsWith("f ")) {
					// Just making sure the line actually starts with f. If not, skill until we get to them.
					line = reader.readLine();
					continue;
				}
				// Breaking the line into three parts - one for each vertex.
				// 0th element of this string array is the 'f '. The other parts are the vertices.
				String[] currentLine = line.split(" ");
				// We now have each vertex's position, texture and normal ordinates broken up.
				String[] vertex1 = currentLine[1].split("/");
				String[] vertex2 = currentLine[2].split("/");
				String[] vertex3 = currentLine[3].split("/");
				
				processVertex(vertex1,indices,textures,normals,textureArray,normalsArray);
				processVertex(vertex2,indices,textures,normals,textureArray,normalsArray);
				processVertex(vertex3,indices,textures,normals,textureArray,normalsArray);
				
				line = reader.readLine();
			}
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Adding them as floats, not 3D vectors as in 'vertices'. Hence *3.
		verticesArray = new float[vertices.size()*3];
		indicesArray = new int[indices.size()];
		
		int vertexPointer = 0;
		for(Vector3f vertex : vertices) {
			verticesArray[vertexPointer++] = vertex.x;
			verticesArray[vertexPointer++] = vertex.y;
			verticesArray[vertexPointer++] = vertex.z;
		}
		
		for(int i=0; i<indices.size(); i++) {
			indicesArray[i] = indices.get(i);
		}
		
		return loader.loadToVAO(verticesArray, textureArray, normalsArray, indicesArray);
	}
	
	/*
	 * Used to put the vertex data in the right order according to the indices.
	 * i.e. first element of the normal array should be the normal for the first vertex and so on.
	 */
	private static void processVertex(String[] vertexData, List<Integer> indices, 
			List<Vector2f> textures, List<Vector3f> normals, float[] textureArray,
			float[] normalsArray) {
		
		// Blender's coordinate system starts at 1. We need our indexes to work from origin 0.
		int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
		// The indices for each triangle will be added in the right order.
		indices.add(currentVertexPointer);
		
		Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) -1);
		// Each texture has two ordinates.
		textureArray[currentVertexPointer*2] = currentTex.x;
		// OpenGL starts from the top left of the texture for its y origin. Blender starts
		// bottom left.
		textureArray[currentVertexPointer*2+1] = 1 - currentTex.y;
		
		Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
		normalsArray[currentVertexPointer*3] = currentNorm.x;
		normalsArray[currentVertexPointer*3+1] = currentNorm.y;
		normalsArray[currentVertexPointer*3+2] = currentNorm.z;
	}
}
