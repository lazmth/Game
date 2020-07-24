package game;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import entities.Camera;
import fontRendering.TextMaster;
import loader.Loader;
import particles.ParticleMaster;
import renderer.MasterRenderer;
import toolbox.MousePicker;
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
		Scene scene = new Scene(loader);
		Camera camera = new Camera(scene.getPlayer());
		MasterRenderer renderer = new MasterRenderer(loader);
		TextMaster.init(loader);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		
		// *************** MAIN GAME LOOP *********************
		
		while (!Window.windowShouldClose()) {
			Window.updateMousePosition();
			
			ParticleMaster.update(camera);
			camera.move();
			scene.getAnimatedEntities().get(0).getModel().getCurrentAnimation().update();
			
			// Checking whether we have moved to a new world chunk.
			previousGridPos = gridPos; 
			gridPos = scene.getWorld().getGridPosition(scene.getPlayer().getPosition());
			
			if ((gridPos[0] != previousGridPos[0]) || (gridPos[1] != previousGridPos[1])) {
				gridDelta[0] = gridPos[0] - previousGridPos[0];
				gridDelta[1] = gridPos[1] - previousGridPos[1];
				scene.getWorld().update(previousGridPos, gridPos, gridDelta);
			}
			
			scene.getPlayer().move(scene.getWorld().getChunk(gridPos[0], gridPos[1]).getTerrain());
			//////////////////////////////////////////////////////

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
			
			if (benchmarkActive) {
				benchmarkFinished = Window.benchMark();
				if (benchmarkFinished) {
					benchmarkActive = false;
				}
			}

			// Clip plane is set very high here just in case the request for clip disable above doesn't work.
			renderer.renderGame(
					scene,
					camera,
					new Vector4f(0, 10000, 0, 1000)
					);
			
			Window.updateDisplay();
			
		}
		
		TextMaster.cleanUp();
		ParticleMaster.cleanUp();
		Window.destroyWindow();
		Window.stopGLFW();
		renderer.cleanUp();
		loader.cleanUp();
	}

}