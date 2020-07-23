package game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;


import entities.Camera;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import loader.Loader;
import particles.ParticleMaster;
import particles.ParticleTexture;
import renderer.MasterRenderer;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import window.Window;

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
		
		Scene scene = new Scene(loader);
		
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		Camera camera = new Camera(scene.getPlayer());
		MasterRenderer renderer = new MasterRenderer(loader);
		
		MousePicker picker = new MousePicker(
				camera,
				renderer.getProjectionMatrix(),
				scene.getWorld().getTerrains().get(0)
				);
		
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("particleAtlas.png"), 4);
		
		// ********************* Water Setup ***************************
		
		WaterFrameBuffers fbos = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		
		
		// ********************* *********** ***************************
		
		while (!Window.windowShouldClose()) {
			Window.updateMousePosition();
			
			// Checking whether we have moved to a new world chunk.
			previousGridPos = gridPos; 
			gridPos = scene.getWorld().getGridPosition(scene.getPlayer().getPosition());
			
			if ((gridPos[0] != previousGridPos[0]) || (gridPos[1] != previousGridPos[1])) {
				gridDelta[0] = gridPos[0] - previousGridPos[0];
				gridDelta[1] = gridPos[1] - previousGridPos[1];
				scene.getWorld().update(previousGridPos, gridPos, gridDelta);
			}
			
			//////////////////////////////////////////////////////
			
			scene.getPlayer().move(scene.getWorld().getChunk(gridPos[0], gridPos[1]).getTerrain());
			camera.move();
			
			if (Window.isKeyPressed(GLFW.GLFW_KEY_T)) {
				if (!benchmarkActive) {
					scene.getPlayer().setPosition(new Vector3f(200, 0, 140));
					scene.getPlayer().increaseRotation(0, -120, 0);
					System.out.println("Benchmark started.");
					benchmarkActive = true;
				} else {
					System.out.println("Benchmark is currently active.");
				}
			} 
			
			if (Window.isKeyPressed(GLFW.GLFW_KEY_Y)) {
				if (scene.getPlayer().getModel().getCurrentAnimation().getName().equals("Armature|Star Jump")) {
					scene.getPlayer().setCurrentAnimation("Armature|Baked Walk");
				} else if (scene.getPlayer().getModel().getCurrentAnimation().getName().equals("Armature|Baked Walk")) { 
					scene.getPlayer().setCurrentAnimation("Armature|Star Jump");
				}
				
			}
			
			if (Window.isKeyPressed(GLFW.GLFW_KEY_U)) {
				scene.getAnimatedEntities().get(0).getModel().getCurrentAnimation().update();
			}
			
			if (benchmarkActive) {
				benchmarkFinished = Window.benchMark();
				if (benchmarkFinished) {
					benchmarkActive = false;
				}
			}
			
			ParticleMaster.update(camera);
			
			// Rendering to FBOs for water textures.
			if (!scene.getWaters().isEmpty()) {
				GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
				fbos.bindReflectionFrameBuffer();
				//TODO Distance is hardcoded for only the first water.
				float distance = 2 * (camera.getPosition().y - scene.getWaters().get(0).getHeight());
				camera.getPosition().y -= distance;
				camera.invertPitch();
				renderer.renderScene(
						scene.getEntites(),
						scene.getAnimatedEntities(),
						scene.getNormalMappedEntities(),
						scene.getWorld().getTerrains(),
						scene.getLights(),
						camera,
						new Vector4f(0, 1, 0, -scene.getWaters().get(0).getHeight() + 1f)
						);
				camera.getPosition().y += distance;
				camera.invertPitch();
				fbos.unbindCurrentFrameBuffer();
				
				fbos.bindRefractionFrameBuffer();
				renderer.renderScene(
						scene.getEntites(),
						scene.getAnimatedEntities(),
						scene.getNormalMappedEntities(),
						scene.getWorld().getTerrains(),
						scene.getLights(),
						camera,
						new Vector4f(0, -1, 0, scene.getWaters().get(0).getHeight())
						);
				
				fbos.unbindCurrentFrameBuffer();
				GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			}
			
			// Clip plane is set very high here just in case the request for clip disable above doesn't work.
			renderer.renderScene(
					scene.getEntites(),
					scene.getAnimatedEntities(),
					scene.getNormalMappedEntities(),
					scene.getWorld().getTerrains(),
					scene.getLights(),
					camera,
					new Vector4f(0, 10000, 0, 1000)
					);
			waterRenderer.render(scene.getWaters(), camera, scene.getLights().get(0));
			
			ParticleMaster.renderParticles(camera);
			
			guiRenderer.render(scene.getGUIs());
			//TextMaster.render();
			

			Window.updateDisplay();
			
			//System.out.println(Window.getFPS());
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
