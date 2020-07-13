package game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import loader.Loader;
import loader.OBJLoader;
import loader.StaticMeshesLoader;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import particles.ParticleMaster;
import particles.ParticleTexture;
import renderer.MasterRenderer;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;
import window.Window;
import world.World;

public class GameLoop {
	
	private static boolean benchmarkActive = false;
	private static boolean benchmarkFinished = false;
	
	private static int[] previousGridPos = new int[2];
	private static int[] gridPos = new int[2];
	private static int[] gridDelta = new int[2];
	
	public static void main(String[] args) {
		Window.createDisplay();
		
		Loader loader = new Loader();
		TextMaster.init(loader);
		
		RawModel testModelRaw = OBJLoader.loadObjModel("dragon", loader);
		ModelTexture texture = new ModelTexture(loader.loadTexture("dragonTexture.png"));
		texture.setShineDamper(10);
		texture.setReflectivity(1);
		TexturedModel testModelTextured = new TexturedModel(testModelRaw, texture);
		Entity testEntity = new Entity(testModelTextured, new Vector3f(0,-4,0),0,0,0,1);
		
		RawModel fernRaw = OBJLoader.loadObjModel("fern", loader);
		ModelTexture fernTexture = new ModelTexture(loader.loadTexture("fern.png"));
		fernTexture.setNumberOfRows(2);
		fernTexture.setHasTransparency(true);
		TexturedModel fernTexModel = new TexturedModel(fernRaw, fernTexture);
		Entity fern1 = new Entity(fernTexModel, 1, new Vector3f(10, 0, 10),0,0,0,4);
		Entity fern2 = new Entity(fernTexModel, 3, new Vector3f(45, 0, 10),0,0,0,4);
		
		Light light = new Light(new Vector3f(200,20,100), new Vector3f(4,0,0), new Vector3f(0.25f,0.01f,0.002f));
		Light light2 = new Light(new Vector3f(0,20,40), new Vector3f(0,0,4), new Vector3f(0.25f,0.01f,0.002f));
		Light sun = new Light(new Vector3f(200, 100, 200), new Vector3f(1,1,1));
		List<Light> lights = new ArrayList<Light>();
		lights.add(light);
		lights.add(light2);
		lights.add(sun);
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2.png"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud.png"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers.png"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path.png"));
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap.png"));
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		Terrain terrain = new Terrain(0,0, loader, texturePack, blendMap, "heightMap");
		
		Terrain terrain2 = new Terrain(1,1, loader, texturePack, blendMap, "heightMap");
		
		RawModel bunnyRaw = OBJLoader.loadObjModel("bunny", loader);
		TexturedModel bunnyTextured = new TexturedModel(bunnyRaw, texture);
		
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader),
				new ModelTexture(loader.loadTexture("barrel.png")));
		barrelModel.getTexture().setShineDamper(10);
		barrelModel.getTexture().setReflectivity(0.5f);
		barrelModel.getTexture().setNormalMapID(loader.loadTexture("barrelNormal.png"));
		
		List<Terrain> terrains = new ArrayList<Terrain>();
		List<Entity> entites = new ArrayList<Entity>();
		List<Entity> normalMappedEntities = new ArrayList<Entity>();
		
		Player player = new Player(bunnyTextured, new Vector3f(0,0,0), 0, 0, 0, 1);
		
//		terrains.add(terrain);
//		terrains.add(terrain2);
		entites.add(fern1);
		entites.add(fern2);
		entites.add(player);
		normalMappedEntities.add(new Entity(barrelModel, new Vector3f(160, 10, 160), 0, 0, 0, 1f));
		
		// STATIC MESH LOADER TESTING ///////////////
		
		ClassLoader classloader = GameLoop.class.getClassLoader();
        System.out.println(classloader.getResource("game/GameLoop.class"));
		
		File file = new File("src/main/resources/" + "cowboyObj.obj");
		File resourceFile = new File("src/main/resources/" + "cowboyObj.mtl");
		
		try {
			TexturedModel[] cowboyOBJ = StaticMeshesLoader.load(
					file.getAbsolutePath(),
					"src/main/resources");
			
			Entity testCowboy = new Entity(cowboyOBJ[0], 1, new Vector3f(-20, 0, -20),0,0,0,4);
			entites.add(testCowboy);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("whoops.");
		}
		
		
		//////////////////////////
		
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
//		GuiTexture gui = new GuiTexture(loader.loadTexture("brickSquare"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
//		guis.add(gui);
		
		// Fonts for GUI Text.
		FontType font = new FontType(loader.loadTexture("consolas.png"), new File("src/main/resources/" + "consolas.fnt"));
		GUIText text = new GUIText("This is a test :D", 6, font, new Vector2f(0.1f,0.1f), 0.75f, false);
		float outlineR = 0.6f;
		float outlineG = 0.0f;
		float outlineB = 0.4f;
		text.setOutlineColour(outlineR, outlineG, outlineB);
		text.setColour(0.7f, 0.7f, 0.7f);
		
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		Camera camera = new Camera(player);
		MasterRenderer renderer = new MasterRenderer(loader);
		
		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
		Vector3f terrainIntersection = new Vector3f(0.0f);
		
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("particleAtlas.png"), 4);
		
		// ********************* Water Setup ***************************
		
		WaterFrameBuffers fbos = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterTile water = new WaterTile(40, 40, 20);
		//waters.add(water);
		
		// ********************* *********** ***************************
		
		World world = new World(500);
		
		
		while (!Window.windowShouldClose()) {
			Window.updateMousePosition();
			previousGridPos = gridPos; 
			gridPos = world.getGridPosition(player.getPosition());
			
			if ((gridPos[0] != previousGridPos[0]) || (gridPos[1] != previousGridPos[1])) {
				gridDelta[0] = gridPos[0] - previousGridPos[0];
				gridDelta[1] = gridPos[1] - previousGridPos[1];
				world.update(previousGridPos, gridPos, gridDelta);
			}
			
			
			player.move(world.getChunk(gridPos[0], gridPos[1]).getTerrain());
			camera.move();
			
			if(Window.isKeyPressed(GLFW.GLFW_KEY_T)) {
				if (!benchmarkActive) {
					player.setPosition(new Vector3f(200, 0, 140));
					player.increaseRotation(0, -120, 0);
					System.out.println("Benchmark started.");
					benchmarkActive = true;
				} else {
					System.out.println("Benchmark is currently active.");
				}
			}
			
			if (benchmarkActive) {
				benchmarkFinished = Window.benchMark();
				if (benchmarkFinished) {
					benchmarkActive = false;
				}
			}
			
			ParticleMaster.update(camera);
			
			//picker.update();
			//terrainIntersection = picker.getCurrentTerrainPoint();
			
			// Rendering to FBOs for water textures.
			if (!waters.isEmpty()) {
				GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
				fbos.bindReflectionFrameBuffer();
				float distance = 2 * (camera.getPosition().y - water.getHeight());
				camera.getPosition().y -= distance;
				camera.invertPitch();
				renderer.renderScene(entites, normalMappedEntities, world.getTerrains(), lights, camera, new Vector4f(0, 1, 0, -water.getHeight() + 1f));
				camera.getPosition().y += distance;
				camera.invertPitch();
				fbos.unbindCurrentFrameBuffer();
				
				fbos.bindRefractionFrameBuffer();
				renderer.renderScene(entites, normalMappedEntities, world.getTerrains(), lights, camera, new Vector4f(0, -1, 0, water.getHeight()));
				fbos.unbindCurrentFrameBuffer();
				GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			}
			
			// Clip plane is set very high here just in case the request for clip disable above doesn't work.
			renderer.renderScene(entites, normalMappedEntities, world.getTerrains(), lights, camera, new Vector4f(0, 10000, 0, 1000));
			waterRenderer.render(waters, camera, sun);
			
			ParticleMaster.renderParticles(camera);
			
			guiRenderer.render(guis);
			TextMaster.render();
			
			text.setOutlineColour(outlineR, outlineG, outlineB);
			outlineR += 0.0001f;
			outlineR %= 1;
			
			outlineG += 0.001f;
			outlineG %= 0.6f;

			Window.updateDisplay();
			
//			System.out.println(Window.getFPS());
		}
		
		TextMaster.cleanUp();
		ParticleMaster.cleanUp();
		fbos.cleanUp();
		waterShader.cleanUp();
		renderer.cleanUp();
		guiRenderer.cleanUp();
		Window.destroyWindow();
		Window.stopGLFW();
		loader.cleanUp();
	}
	

}
