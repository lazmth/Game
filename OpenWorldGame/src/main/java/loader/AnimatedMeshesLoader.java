package loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.Assimp;

import models.RawModel;
import models.TexturedModel;
import models.animatedModel.AnimatedModel;
import textures.ModelTexture;
import toolbox.Utils;

public class AnimatedMeshesLoader extends StaticMeshesLoader {
	
	// Max weights which can affect a vertex.
	private static final int MAX_WEIGHTS = 4;
	
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
	    // BoneList is used later when calculating the bone transforms for
	    // each animation frame.
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
	
	/** 
	 * Extracts and processes the bones from an ASSIMP aiMesh.
	 */
	private static void processBones(
			AIMesh aiMesh, 
			List<Bone> boneList, 
			List<Integer> boneIDs, 
			List<Float> boneWeights) 
	{
		// Vertex IDs along with the bones which affect the indexed vertices
		// (and the weight with which they do so).
		Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
		int numBones = aiMesh.mNumBones();
		PointerBuffer aiBones = aiMesh.mBones();
		
		for (int i=0; i < numBones; i++) {
			AIBone aiBone = AIBone.create(aiBones.get(i));
			int boneID = boneList.size();
			Bone bone = new Bone(
					boneID, 
					aiBone.mName().dataString(), 
					toMatrix(aiBone.mOffsetMatrix()));
			boneList.add(bone);
			
			// Now, information about which vertices the bone affects.
			// Note an AIVertexWeight also contains the vertex ID it affects -
			// it is not just the stand-alone weight information.
			int numWeights = aiBone.mNumWeights();
			AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
			
			for (int j=0; i < numWeights; j++) {
				AIVertexWeight aiWeight = aiWeights.get(j);
				VertexWeight vw = new VertexWeight(
						bone.getBoneID(),
						aiWeight.mVertexId(),
						aiWeight.mWeight());
				
				List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexID());
				if (vertexWeightList == null) {
					vertexWeightList = new ArrayList<>();
					weightSet.put(vw.getVertexID(), vertexWeightList);
				}
				// Remember vertexWeightList is just a pointer to the list in
				// the weight-set - this is not a local copy that we are adding to.
				vertexWeightList.add(vw);
			}
		}
		
		int numVertices = aiMesh.mNumVertices();
		for (int i=0; i < numVertices; i++) {
			List<VertexWeight> vertexWeightList = weightSet.get(i);
			int weightsForVertexI = 
					(vertexWeightList != null ? vertexWeightList.size() : 0);
			
			for (int j=0; j < MAX_WEIGHTS; j++) {
				if (j < weightsForVertexI) { 
					VertexWeight vw = vertexWeightList.get(j);
					boneWeights.add(vw.getWeight());
					boneIDs.add(vw.getBoneID());
				} else {
					// The arrays must be padded so the stride is MAX_WEIGHTS.
					boneWeights.add(0.0f);
					boneIDs.add(0);
				}
			}
		}
		
	}
	
	/**
	 * Converts an ASSIMP 4x4 matrix into one usable by our engine.
	 */
	private static Matrix4f toMatrix(
			AIMatrix4x4 aiMatrix4x4) 
	{
        Matrix4f result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }

}
