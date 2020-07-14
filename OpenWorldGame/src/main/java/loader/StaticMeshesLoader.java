package loader;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import models.RawModel;
import models.TexturedModel;
import textures.ModelTexture;
import toolbox.Utils;

/**
 * Note that the load methods currently return textured models.
 * The naming could be better to fit with 'static mesh loader'. The tutorial
 * implementation loads meshes with attached 'materials' which may or may not
 * contain textures.
 *  
 * @author Billy
 *
 */
public class StaticMeshesLoader {
	
	// For loading to VAOs.
	private static Loader loader = new Loader();
	
	/**
	 * A default call to load a model file with some commonly used flags.
	 * @param resourcePath
	 * 	- Absolute path to the model file.
	 * @param texturesDir
	 * 	- CLASSPATH relative path to the resource folder (which holds textures
	 *    etc for the model).
	 * @return
	 */
	public static TexturedModel[] load(
			String resourcePath, 
			String texturesDir) throws Exception 
	{
	    return load(
	    		resourcePath, 
	    		texturesDir, 
	    		// Some commonly used flags.
	    		// FlipUVs could cause problems with some models.
	    		Assimp.aiProcess_FlipUVs |
	    		Assimp.aiProcess_Triangulate | 
	    		Assimp.aiProcess_FixInfacingNormals);   
	}
	
	public static TexturedModel[] load(
			String resourcePath, 
			String texturesDir, 
			int flags) throws Exception 
	{
	    AIScene aiScene = Assimp.aiImportFile(resourcePath, flags);
	    if (aiScene == null) {
	    	throw new Exception("Error loading model!");
	    }
	    
	    // Texture loading.
	    // Assimp uses the notion of 'materials' rather than just textures.
	    int numMaterials = aiScene.mNumMaterials();
	    // PointerBuffer pointing to the location of the materials in memory.
	    PointerBuffer aiMaterials = aiScene.mMaterials();
	    List<ModelTexture> textures = new ArrayList<>();
	    
	    for (int i=0; i < numMaterials; i++) {
	    	AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
	    	processTexture(aiMaterial, textures, texturesDir);
	    }
	    
	    // Mesh loading.
	    // A scene can contain more than one mesh, i.e. for different components
	    // of a model.
	    int numMeshes = aiScene.mNumMeshes();
	    PointerBuffer aiMeshes = aiScene.mMeshes();
	    TexturedModel[] texturedModels = new TexturedModel[numMeshes];
	    
	    for (int i=0; i < numMeshes; i++) {
	    	AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
	    	TexturedModel mesh = processMesh(aiMesh, textures);
	    	texturedModels[i] = mesh;
	    }
	    
	    //TODO Textured models doesn't make sense - could be multiple components
	    // of a model.
	    return texturedModels;
	}

	private static void processTexture(
			AIMaterial aiMaterial,
			List<ModelTexture> textures,
			String texturesDir) throws Exception
	{	
		//TODO Also load other material properties, not just texture.
		// An ASSIMP type for a string. This will hold the path to the 
		// texture after it has been read.
		AIString path = AIString.calloc();

		int result = Assimp.aiGetMaterialTexture(
				aiMaterial, 
				Assimp.aiTextureType_DIFFUSE, 
				0, 
				path, 
				(IntBuffer) null, 
				null, 
				null, 
				null, 
				null, 
				null);
		
		// 0: success. -1: failure. -3: out of memory.
		System.out.println("Tried to load a texture for material with result: " + result);
		
		String texturePath = path.dataString();
		ModelTexture texture = null;
		
		if (texturePath != null && texturePath.length() > 0) {
			texture = new ModelTexture(
					loader.loadTexture(texturePath));
		} 
		
		textures.add(texture);
	}
	
	private static TexturedModel processMesh(
			AIMesh aiMesh,
			List<ModelTexture> textures) throws Exception
	{
		List<Float> vertices = new ArrayList<>();
		List<Float> textureCoords = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		
		processVertices(aiMesh, vertices);
		processNormals(aiMesh, normals);
		processTextureCoords(aiMesh, textureCoords);
		processIndices(aiMesh, indices);
		
		RawModel model = loader.loadToVAO(
				Utils.floatListToArray(vertices),
				Utils.floatListToArray(textureCoords),
				Utils.floatListToArray(normals),
				Utils.integerListToArray(indices));
		
		ModelTexture texture;
		int textureIndex = aiMesh.mMaterialIndex();
		
		if (textureIndex >= 0 && textureIndex < textures.size()) {
			texture = textures.get(textureIndex);
		} else {
			texture = new ModelTexture(loader.loadTexture("dragonTexture"));
			System.out.println("Couldn't load file texture. Defaulting.");
		}
		
		return new TexturedModel(model, texture);
	}
	
	/**
	 * Simply extracts the vertex information from the mesh.
	 * @param aiMesh
	 * 	- The mesh containing the vertex information.
	 * @param vertices
	 * 	- A list of vertices, used to store the output vertex information.
	 */
	private static void processVertices(AIMesh aiMesh, List<Float> vertices) {
		AIVector3D.Buffer aiVertices = aiMesh.mVertices();
		
		while (aiVertices.remaining() > 0) {
			AIVector3D aiVertex = aiVertices.get();
			vertices.add(aiVertex.x());
			vertices.add(aiVertex.y());
			vertices.add(aiVertex.z());
		}
	}
	
	private static void processNormals(AIMesh aiMesh, List<Float> normals) {
		AIVector3D.Buffer aiNormals = aiMesh.mNormals();
		
		while (aiNormals != null && aiNormals.remaining() > 0) {
			AIVector3D aiNormal = aiNormals.get();
			normals.add(aiNormal.x());
			normals.add(aiNormal.y());
			normals.add(aiNormal.z());
		}
	}
	
	private static void processTextureCoords(AIMesh aiMesh, List<Float> textureCoords) {
		// A model can have multiple sets of texture coordinates, and so here
		// we get the texture coords in a slightly different way to the normals etc.
		AIVector3D.Buffer aiTextureCoords = aiMesh.mTextureCoords(0);
		
		// There doesn't seem to be equivalent to the while loop. Unsure why this was used.
		int numTexCoords = (aiTextureCoords != null ? aiTextureCoords.remaining() : 0);
		
		for (int i=0; i < numTexCoords; i++) {
			AIVector3D aiTextureCoord = aiTextureCoords.get();
			textureCoords.add(aiTextureCoord.x());
			// Flipping the y coordinate is dealt with a flag when importing.
			textureCoords.add(aiTextureCoord.y());
		}
	}
	
	private static void processIndices(AIMesh aiMesh, List<Integer> indices) {
		int numFaces = aiMesh.mNumFaces();
		AIFace.Buffer aiFaces = aiMesh.mFaces();
		
		for (int i=0; i < numFaces; i++) {
			AIFace aiFace = aiFaces.get(i);
			IntBuffer buffer = aiFace.mIndices();
			
			// The buffer _could_ be passed directly to OpenGL for more efficiency.
			while (buffer.remaining() > 0) {
				indices.add(buffer.get());
			}
		}
	}
	

}
