package renderer;

import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import entities.AnimatedEntity;
import entities.Camera;
import entities.Light;
import models.RawModel;
import models.TexturedModel;
import shaders.AnimatedModelShader;
import toolbox.Maths;

public class AnimatedModelRenderer {
		
	private AnimatedModelShader shader;
	
	public AnimatedModelRenderer(Matrix4f projectionMatrix) {
		shader = new AnimatedModelShader();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}
	
	public void render(List<AnimatedEntity> entities, List<Light> lights, Camera camera) {
		prepare(camera);

		shader.loadModelMatrix(Maths.createTransformationMatrix(
				entities.get(0).getPosition(), 
				entities.get(0).getRotX(), 
				entities.get(0).getRotY(), 
				entities.get(0).getRotZ(), 
				entities.get(0).getScale()));
		
		shader.loadLights(lights);
		
		
		for (AnimatedEntity entity : entities) {
			if (entity.getModel().getCurrentAnimation() != null) {
				int currentFrame = entity.getModel().getCurrentAnimation().getCurrentFrame();
				shader.loadJointTransforms(entity.getModel().getCurrentAnimation().getKeyFrames().get(currentFrame).getJointMatrices());
			}
			
			// All meshes have the same texture. Use that of the 1st for all.
			//TODO Will need to do some work because different meshes may have
			// different materials (different reflective properties etc).
			int texture = entity.getModel().getMeshes()[0].getTexture().getTextureID();
			
			for (int i=0; i < entity.getModel().getMeshes().length; i++) {
				TexturedModel mesh = entity.getModel().getMeshes()[i];
				RawModel rawModel = mesh.getRawModel();
				
				GL30.glBindVertexArray(rawModel.getVaoID());
				
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				GL20.glEnableVertexAttribArray(2);
				GL20.glEnableVertexAttribArray(3);
				GL20.glEnableVertexAttribArray(4);
				
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
				
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
				
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL20.glDisableVertexAttribArray(2);
				GL20.glDisableVertexAttribArray(3);
				GL20.glDisableVertexAttribArray(4);
				GL30.glBindVertexArray(0);
			}
			
		}
		finish();
		
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
	
	private void prepare(Camera camera) {
		shader.start();
		shader.loadViewMatrix(camera);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	private void finish() {
		shader.stop();
	}

}
