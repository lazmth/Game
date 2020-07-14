package loader;

import static loader.StaticMeshesLoader.processMesh;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

import models.RawModel;
import models.TexturedModel;
import models.animatedModel.AnimatedModel;
import textures.ModelTexture;
import toolbox.Utils;

public class AnimatedMeshesLoader extends StaticMeshesLoader {
	
	public static AnimatedModel loadAnimatedModel(
			String resourcePath,
			String texturesDir) throws Exception 
	{
		return loadAnimatedModel(
				resourcePath,
				texturesDir,
				// Some commonly used flags.
				Assimp.aiProcess_GenSmoothNormals |
				Assimp.aiProcess_JoinIdenticalVertices |
				Assimp.aiProcess_Triangulate |
				Assimp.aiProcess_FixInfacingNormals |
				Assimp.aiProcess_LimitBoneWeights);
	}
	
	public static AnimatedModel loadAnimatedModel(
			String resourcePath,
			String texturesDir,
			int flags) throws Exception
	{
		AIScene aiScene = Assimp.aiImportFile(resourcePath, flags);
		if (aiScene == null) {
			throw new Exception("Error loading model!");
		}
		
		// Extracting texture information
		int numMaterials = aiScene.mNumMaterials();
		PointerBuffer aiMaterials = aiScene.mMaterials();
		List<ModelTexture> textures = new ArrayList<>();
		
	    for (int i=0; i < numMaterials; i++) {
	    	AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
	    	StaticMeshesLoader.processTexture(aiMaterial, textures, texturesDir);
	    }
	    
	    // Extracting mesh information (including bone weight info this time)
	    List<Bone> boneList = new ArrayList<>();
	    int numMeshes = aiScene.mNumMeshes();
	    PointerBuffer aiMeshes = aiScene.mMeshes();
	    TexturedModel[] texturedModels = new TexturedModel[numMeshes];
	    
	    for (int i=0; i < numMeshes; i++) {
	    	AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
	    	TexturedModel mesh = processMesh(aiMesh, textures, boneList);
	    	texturedModels[i] = mesh;
	    }
	    
	    // Extracting bone hierarchy and animation information
	    AINode aiRootNode = aiScene.mRootNode();
	    // ...
	    
		return null;
	}
	

	private static TexturedModel processMesh(
			AIMesh aiMesh,
			List<ModelTexture> textures,
			List<Bone> boneList) throws Exception
	{
		List<Float> vertices = new ArrayList<>();
		List<Float> textureCoords = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		List<Integer> boneIDs = new ArrayList<>();
		List<Float> boneWeights = new ArrayList<>();
		
		// Similar to a static mesh but now we also load bone information.
		StaticMeshesLoader.processVertices(aiMesh, vertices);
		StaticMeshesLoader.processNormals(aiMesh, normals);
		StaticMeshesLoader.processTextureCoords(aiMesh, textureCoords);
		StaticMeshesLoader.processIndices(aiMesh, indices);
		processBones(aiMesh, boneList, boneIDs, boneWeights);
		
		RawModel model = loader.loadToVAO(
				Utils.floatListToArray(vertices),
				Utils.floatListToArray(textureCoords),
				Utils.floatListToArray(normals),
				Utils.integerListToArray(indices),
				Utils.integerListToArray(boneIDs),
				Utils.floatListToArray(boneWeights));
		
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

}
