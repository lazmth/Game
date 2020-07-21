package renderer;

import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import entities.Camera;
import models.RawModel;
import models.TexturedModel;
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
	
	public void render(List<AnimatedModel> entities, Camera camera) {
		prepare(camera);
		
		for (AnimatedModel entity : entities) {
			// All meshes have the same texture. Use that of the 1st for all.
			//TODO Will need to do some work because different meshes may have
			// different materials (different reflective properties etc).
			int texture = entity.getMeshes()[0].getTexture().getTextureID();
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
			GL20.glEnableVertexAttribArray(3);
			GL20.glEnableVertexAttribArray(4);
			
			//shader.loadJointTransforms(entity.getJointTransforms());
			
			for (int i=0; i < entity.getMeshes().length; i++) {
				TexturedModel mesh = entity.getMeshes()[i];
				RawModel rawModel = mesh.getRawModel();
				
				GL30.glBindVertexArray(rawModel.getVaoID());
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}

			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(2);
			GL20.glDisableVertexAttribArray(3);
			GL20.glDisableVertexAttribArray(4);
			GL30.glBindVertexArray(0);
			
			finish();
		}
		
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
	
	private void prepare(Camera camera) {
		shader.start();
		Matrix4f projectionViewMatrix = createProjectionViewMatrix(camera);
		shader.loadProjectionViewMatrix(projectionViewMatrix);
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
