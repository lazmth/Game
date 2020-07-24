package game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import animation.Animation;
import entities.AnimatedEntity;
import entities.Entity;
import entities.Light;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import guis.GuiTexture;
import loader.AnimatedMeshesLoader;
import loader.Loader;
import loader.OBJLoader;
import models.RawModel;
import models.TexturedModel;
import models.animatedModel.AnimatedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import terrain.Terrain;
import textures.ModelTexture;
import water.WaterTile;
import world.World;

public class Scene {
	
	private List<Terrain> terrains;
	private List<Entity> entites = new ArrayList<Entity>();
	private List<Entity> normalMappedEntities = new ArrayList<Entity>();
	private List<AnimatedEntity> animatedEntities = new ArrayList<>();
	private List<WaterTile> waters = new ArrayList<WaterTile>();
	private List<Light> lights = new ArrayList<Light>();
	private List<GuiTexture> guis = new ArrayList<GuiTexture>();
	
	private World world;
	private Player player;
	
	private Loader loader;
	
	public Scene(Loader loader) {
		this.loader = loader;
		this.world = new World(500);
		initEntities();
		initAnimatedEntities();
		//initGUI();
		initWaters();
		
		terrains = world.getTerrains();
	}
	
	public void initEntities() {
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
		lights.add(light);
		lights.add(light2);
		lights.add(sun);
		
		RawModel bunnyRaw = OBJLoader.loadObjModel("bunny", loader);
		TexturedModel bunnyTextured = new TexturedModel(bunnyRaw, texture);
		
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader),
				new ModelTexture(loader.loadTexture("barrel.png")));
		barrelModel.getTexture().setShineDamper(10);
		barrelModel.getTexture().setReflectivity(0.5f);
		barrelModel.getTexture().setNormalMapID(loader.loadTexture("barrelNormal.png"));
		
		
		entites.add(fern1);
		entites.add(fern2);
		normalMappedEntities.add(new Entity(barrelModel, new Vector3f(160, 10, 160), 0, 0, 0, 1f));
		
	}
	
	public void initAnimatedEntities() {
		ClassLoader classloader = GameLoop.class.getClassLoader();
		File file = new File("src/main/resources/" + "cowboyNEW.fbx");
		
		AnimatedModel testAnimatedModel = null;
		try {
			testAnimatedModel = AnimatedMeshesLoader.loadAnimatedModel(
					file.getAbsolutePath(),
					"src/main/resources");
		} catch (Exception e) {
			System.out.println("Tried to load file: " + file.toString() + "," +
		" but failed.");
			e.printStackTrace();
		}
		
		Animation bakedAnimation = testAnimatedModel.getAnimation("Armature|Star Jump");
		testAnimatedModel.setCurrentAnimation(bakedAnimation);
		
		player = new Player(testAnimatedModel, new Vector3f(0,0,0), 0, 0, 0, 0.04f);
		animatedEntities.add(player);

	}
	
	public void initGUI() {
		GuiTexture gui = new GuiTexture(loader.loadTexture("brickSquare.png"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
		guis.add(gui);
		
		// Fonts for GUI Text.
		FontType font = new FontType(loader.loadTexture("consolas.png"), new File("src/main/resources/" + "consolas.fnt"));
		GUIText text = new GUIText("This is a test :D", 6, font, new Vector2f(0.1f,0.1f), 0.75f, false);
		float outlineR = 0.6f;
		float outlineG = 0.0f;
		float outlineB = 0.4f;
		text.setOutlineColour(outlineR, outlineG, outlineB);
		text.setColour(0.7f, 0.7f, 0.7f);
	}
	
	public void initWaters() {
		WaterTile water = new WaterTile(40, 40, 20);
		waters.add(water);
	}
	
	public List<Terrain> getTerrains() {
		return terrains;
	}

	public List<Entity> getEntites() {
		return entites;
	}

	public List<Entity> getNormalMappedEntities() {
		return normalMappedEntities;
	}

	public List<AnimatedEntity> getAnimatedEntities() {
		return animatedEntities;
	}

	public List<Light> getLights() {
		return lights;
	}
	
	public List<WaterTile> getWaters() {
		return waters;
	}
	
	public List<GuiTexture> getGUIs() {
		return guis;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Player getPlayer() {
		return player;
	}
	


}
