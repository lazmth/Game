package renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import entities.AnimatedEntity;
import entities.Camera;
import entities.Entity;
import entities.Light;
import loader.Loader;
import models.TexturedModel;
import models.animatedModel.AnimatedModel;
import normalMappingRenderer.NormalMappingRenderer;
import shaders.BasicShader;
import shaders.TerrainShader;
import skybox.SkyboxRenderer;
import terrain.Terrain;

public class MasterRenderer {
	
	// For projection matrix.
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 4000;
	
	public static final float FOG_R = 0.5f;
	public static final float FOG_G = 0.5f;
	public static final float FOG_B = 0.725f;
	
	private Matrix4f projectionMatrix;

	private BasicShader basicShader = new BasicShader();
	private EntityRenderer entityRenderer;
	
	private TerrainShader terrainShader = new TerrainShader();
	private TerrainRenderer terrainRenderer;
	
	private SkyboxRenderer skyboxRenderer;
	private NormalMappingRenderer normalMapRenderer;
	
	private AnimatedModelRenderer animatedModelRenderer;
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	private Map<TexturedModel, List<Entity>> normalMappedEntities = new HashMap<TexturedModel, List<Entity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();
	
	private List<AnimatedEntity> animatedEntities = new ArrayList<AnimatedEntity>();
	
	public MasterRenderer(Loader loader) {
		enableCulling();
		
		createProjectionMatrix();
		entityRenderer = new EntityRenderer(basicShader, projectionMatrix); 
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		normalMapRenderer = new NormalMappingRenderer(projectionMatrix);
		animatedModelRenderer = new AnimatedModelRenderer(projectionMatrix);
	}
	
	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}
	
	/**
	 * Disables back-face culling. Useful for rendering transparent objects.
	 */
	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}
	
	public void renderScene(List<Entity> entities, List<AnimatedEntity> animatedEntities, List<Entity> normalMappedEntities, List<Terrain> terrains,
			List<Light> lightSources, Camera camera, Vector4f clipPlane) {
		
		for (Entity entity : entities) {
			processEntity(entity);
		}
		
		for (Entity entity : normalMappedEntities) {
			processNormalMappedEntity(entity);
		}
		
		for (Terrain terrain : terrains) {
			processTerrain(terrain);
		}
		
		for (AnimatedEntity entity : animatedEntities) {
			processAnimatedModel(entity);
		}
		
		render(lightSources, camera, clipPlane);
	}
	
	private void render(List<Light> lights, Camera camera, Vector4f clipPlane) {
		prepare();
		basicShader.start();
		// Loading sky colour each frame so we can change it dynamically later.
		basicShader.loadSkyColour(FOG_R, FOG_G, FOG_B);
		basicShader.loadClipPlane(clipPlane);
		basicShader.loadLights(lights);
		basicShader.loadViewMatrix(camera);
		entityRenderer.render(entities);
		basicShader.stop();
		
		animatedModelRenderer.render(animatedEntities, lights, camera);
		
		normalMapRenderer.render(normalMappedEntities, clipPlane, lights, camera);
		
		terrainShader.start();
		terrainShader.loadSkyColour(FOG_R, FOG_G, FOG_B);
		terrainShader.loadClipPlane(clipPlane);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		
		skyboxRenderer.render(camera, FOG_R, FOG_G, FOG_B);
		
		terrains.clear();
		entities.clear();
		normalMappedEntities.clear();
		animatedEntities.clear();
	}
	
	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if(batch!=null) {
			batch.add(entity);
		}else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	
	public void processAnimatedModel(AnimatedEntity entity) {
		this.animatedEntities.add(entity);
	}
	
	public void processNormalMappedEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = normalMappedEntities.get(entityModel);
		if(batch!=null) {
			batch.add(entity);
		}else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			normalMappedEntities.put(entityModel, newBatch);
		}
	}
	
	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		// Clearing the canvas.
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(FOG_R, FOG_G, FOG_B, 1.0f);
	}
	
	public void cleanUp() {
		basicShader.cleanUp();
		terrainShader.cleanUp();
		normalMapRenderer.cleanUp();
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	private void createProjectionMatrix() { 
		float aspectRatio = 1280f / 720f;
		float y_scale = (float) (1f / Math.tan(Math.toRadians((FOV / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustrum_length = FAR_PLANE - NEAR_PLANE;
		
		projectionMatrix = new Matrix4f();
		projectionMatrix.zero();
		projectionMatrix.set(0, 0, x_scale); 
		projectionMatrix.set(1, 1, y_scale);
		projectionMatrix.set(2, 2, -((FAR_PLANE + NEAR_PLANE) / frustrum_length));
		projectionMatrix.set(2, 3, -1);
		projectionMatrix.set(3, 2, -((2 * NEAR_PLANE * FAR_PLANE) / frustrum_length));
		projectionMatrix.set(3, 3, 0);
	}
	
}
