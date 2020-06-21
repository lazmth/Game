package renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import entities.Camera;
import models.animatedModel.AnimatedModel;
import shaders.AnimatedModelShader;
import toolbox.Maths;

public class AnimatedModelRenderer {
	
	// For projection matrix.
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 4000;
		
	private AnimatedModelShader shader;
	
	private static Matrix4f projectionMatrix = null;
	
	public AnimatedModelRenderer() {
		this.shader = new AnimatedModelShader();
		createProjectionMatrix();
	}
	
	public void render(AnimatedModel entity, Camera camera, Vector3f lightPosition) {
		prepare(camera, lightPosition);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, entity.getTexturedModel().getTexture().getTextureID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		
		shader.loadJointTransforms(entity.getJointTransforms());
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL30.glBindVertexArray(0);
		
		finish();
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
	
	private void prepare(Camera camera, Vector3f lightPosition) {
		shader.start();
		Matrix4f projectionViewMatrix = createProjectionViewMatrix(camera);
		shader.loadProjectionViewMatrix(projectionViewMatrix);
		shader.loadLight(lightPosition);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	private void finish() {
		shader.stop();
	}
	
	private Matrix4f createProjectionViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		Matrix4f projectionViewMatrix = new Matrix4f();
		projectionMatrix.mul(viewMatrix, projectionViewMatrix);
		return projectionViewMatrix;
	}
	
	private static void createProjectionMatrix() { 
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
