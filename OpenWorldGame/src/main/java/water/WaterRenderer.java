package water;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import entities.Camera;
import entities.Light;
import game.Scene;
import loader.Loader;
import models.RawModel;
import renderer.MasterRenderer;
import toolbox.Maths;
import window.Window;

public class WaterRenderer {
	
	private static final String DUDV_MAP = "waterDUDV.png";
	private static final String NORMAL_MAP = "matchingNormalMap.png";
	private static float WAVE_SPEED = 0.06f;

	private RawModel quad;
	private WaterShader shader;
	private WaterFrameBuffers fbos;
	
	private float moveFactor = 0;
	
	private int dudvTextureID;
	private int normalMapID;

	public WaterRenderer(Loader loader, Matrix4f projectionMatrix) {
		shader = new WaterShader();
		dudvTextureID = loader.loadTexture(DUDV_MAP);
		normalMapID = loader.loadTexture(NORMAL_MAP);
		fbos = new WaterFrameBuffers();
		shader.start();
		shader.connectTextureUnits();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
		setUpVAO(loader);
	}

	public void render(Scene scene, Camera camera, MasterRenderer masterRenderer) {
		renderToFBOs(scene, camera, masterRenderer);
		prepareRender(camera, scene.getLights().get(0));	
		for (WaterTile tile : scene.getWaters()) {
			Matrix4f modelMatrix = Maths.createTransformationMatrix(
					new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), 0, 0, 0,
					WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
		}
		unbind();
	}
	
	public void cleanUp() {
		fbos.cleanUp();
		shader.cleanUp();
	}
	
	private void renderToFBOs(Scene scene, Camera camera, MasterRenderer renderer) {
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		fbos.bindReflectionFrameBuffer();
		//TODO Distance is hardcoded for only the first water.
		float distance = 2 * (camera.getPosition().y - scene.getWaters().get(0).getHeight());
		camera.getPosition().y -= distance;
		camera.invertPitch();
		renderer.renderScene(
				scene,
				camera,
				new Vector4f(0, 1, 0, -scene.getWaters().get(0).getHeight() + 1f)
				);
		camera.getPosition().y += distance;
		camera.invertPitch();
		fbos.unbindCurrentFrameBuffer();
		
		fbos.bindRefractionFrameBuffer();
		renderer.renderScene(
				scene,
				camera,
				new Vector4f(0, -1, 0, scene.getWaters().get(0).getHeight())
				);
		
		fbos.unbindCurrentFrameBuffer();
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
	}
	
	private void prepareRender(Camera camera, Light sun){
		shader.start();
		shader.loadViewMatrix(camera);
		
		moveFactor += WAVE_SPEED * Window.getLastFrameTime();
		moveFactor %= 1;
		shader.loadMoveFactor(moveFactor);
		shader.loadLight(sun);
		
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getReflectionTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvTextureID);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMapID);
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void unbind(){
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	private void setUpVAO(Loader loader) {
		// Just x and z vectex positions here, y is set to 0 in v.shader
		float[] vertices = { -1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1 };
		quad = loader.loadToVAO(vertices, 2);
	}

}
