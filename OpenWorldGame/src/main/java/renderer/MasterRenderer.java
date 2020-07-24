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
import fontRendering.TextMaster;
import game.Scene;
import guis.GuiRenderer;
import loader.Loader;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import particles.ParticleMaster;
import shaders.BasicShader;
import shaders.TerrainShader;
import skybox.SkyboxRenderer;
import terrain.Terrain;
import water.WaterRenderer;

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
	private TerrainShader terrainShader = new TerrainShader();
	
	private EntityRenderer entityRenderer;
	private TerrainRenderer terrainRenderer;
	private SkyboxRenderer skyboxRenderer;
	private NormalMappingRenderer normalMapRenderer;
	private WaterRenderer waterRenderer;
	private AnimatedModelRenderer animatedModelRenderer;
	private GuiRenderer guiRenderer;
	
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
		waterRenderer = new WaterRenderer(loader, projectionMatrix);
		guiRenderer = new GuiRenderer(loader);
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
	
	public void renderGame(Scene scene, Camera camera, Vector4f clipPlane) {
		renderScene(scene, camera, clipPlane);
		renderWater(scene, camera);
		renderGUI(scene);
	}
	
	public void renderScene(Scene scene, Camera camera, Vector4f clipPlane) {
		
		for (Entity entity : scene.getEntites()) {
			processEntity(entity);
		}
		
		for (Entity entity : scene.getNormalMappedEntities()) {
			processNormalMappedEntity(entity);
		}
		
		for (Terrain terrain : scene.getTerrains()) {
			processTerrain(terrain);
		}
		
		for (AnimatedEntity entity : scene.getAnimatedEntities()) {
			processAnimatedModel(entity);
		}
		
		render(scene, camera, clipPlane);
	}
	
	private void render(Scene scene, Camera camera, Vector4f clipPlane) {
		prepare();
		basicShader.start();
		// Loading sky colour each frame so we can change it dynamically later.
		basicShader.loadSkyColour(FOG_R, FOG_G, FOG_B);
		basicShader.loadClipPlane(clipPlane);
		basicShader.loadLights(scene.getLights());
		basicShader.loadViewMatrix(camera);
		entityRenderer.render(entities);
		basicShader.stop();
		
		animatedModelRenderer.render(animatedEntities, scene.getLights(), camera);
		
		normalMapRenderer.render(normalMappedEntities, clipPlane, scene.getLights(), camera);
		
		terrainShader.start();
		terrainShader.loadSkyColour(FOG_R, FOG_G, FOG_B);
		terrainShader.loadClipPlane(clipPlane);
		terrainShader.loadLights(scene.getLights());
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		
		skyboxRenderer.render(camera, FOG_R, FOG_G, FOG_B);
		
		ParticleMaster.renderParticles(camera);
		TextMaster.render();
		
		terrains.clear();
		entities.clear();
		normalMappedEntities.clear();
		animatedEntities.clear();
	}
	
	/**
	 * Has to be called separately to render scene as rendering the water involves
	 * rendering the scene to FBOs. Have to be separate in order to avoid an
	 * infinite loop.
	 * @param scene
	 * @param camera
	 */
	private void renderWater(Scene scene, Camera camera) {
		waterRenderer.render(scene, camera, this);
	}
	
	private void renderGUI(Scene scene) {
		guiRenderer.render(scene.getGUIs());
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
		waterRenderer.cleanUp();
		guiRenderer.cleanUp();
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
