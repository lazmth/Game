package world;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import terrain.Terrain;

public class World {
	
	private int worldSize; // In chunks. Should be a multiple of 2.
	// How many chunks are visible in any given direction from a chunk.
	private int chunkViewDistance = 3;
	
	private Chunk[][] chunks;
	
	private List<Terrain> terrains = new ArrayList<Terrain>();
	
	private Vector3f initialPlayerPos = new Vector3f(0,0,0);
	
	public World(int worldSize) {
		this.worldSize = worldSize;
		chunks = new Chunk[worldSize][worldSize];
		connectChunks();
		initialLoad(chunkViewDistance);
	}
	
	// Will take a world XML file.
	// Will load all chunks with their XML files.
	public void connectChunks() {
		// Should unpack XML file and produce a map of coordinates to
		// chunk XML file name. Should then iterate through chunk array and
		// instantiate new chunks with their relevant filenames.
		// Then, we would be able to call chunk.load to load from its
		// xml file.
		
		for (int j=0; j<worldSize; j++) {
			for (int i=0; i<worldSize; i++) {
				chunks[j][i] = new Chunk(new int[] {i, j});
			}
		}
	}
	
	/**
	 * Loads in the terrains and entities around the player.
	 */
	public void initialLoad(int chunkViewDistance) {
		int[] initialGridPos = getGridPosition(initialPlayerPos);
		
		// Load a square of side 
		for (int j=initialGridPos[1] - chunkViewDistance; j <=initialGridPos[1] + chunkViewDistance; j++) {
			for (int i=initialGridPos[0] - chunkViewDistance; i <=initialGridPos[0] + chunkViewDistance; i++) {
				if (i >= 0 && j >= 0) {
					chunks[j][i].load(this);
				}
			}
		}
	}
	
	// Both parameters will have to be worked out in the main game loop and so
	// it is not worth working them out again.
	public void update(int[] initialPos, int[] newPos, int[] delta) {
		
		if (delta[0] != 0 && delta[1] != 0) {
			int removeIndexX = initialPos[0] - delta[0] * chunkViewDistance;
			int addIndexX = newPos[0] + delta[0] * chunkViewDistance;
			int removeIndexY = initialPos[1] - delta[1] * chunkViewDistance;
			int addIndexY = newPos[1] + delta[1] * chunkViewDistance;
			
			for (int i=newPos[0] - chunkViewDistance; i <= newPos[0] + chunkViewDistance; i++) {
				if (i>=0 && i<=worldSize) {
					if (removeIndexY>=0 && removeIndexY<=worldSize) {
						chunks[removeIndexY][i].unload(this);
					}
					if (addIndexY>=0 && addIndexY<=worldSize) {
						chunks[addIndexY][i].load(this);
					}
				}
			}
			
			// Saves repeating the actions for the same chunk on the corners of the 'square' around the player.
			for (int j=newPos[1] - chunkViewDistance + 1; j <= newPos[1] + chunkViewDistance - 1; j++) {
				if (j>=0 && j<=worldSize) {
					if (removeIndexX>=0 && removeIndexX<=worldSize) {
						chunks[j][removeIndexX].unload(this);
					}
					if (addIndexX>=0 && addIndexX<=worldSize) {
						chunks[j][addIndexX].load(this);
					}
				}
			}
		} 
		else if (delta[0] != 0) {
			int removeIndexX = initialPos[0] - delta[0] * chunkViewDistance;
			int addIndexX = newPos[0] + delta[0] * chunkViewDistance;
			
			for (int j=newPos[1] - chunkViewDistance; j <= newPos[1] + chunkViewDistance; j++) {
				if (j>=0 && j<=worldSize) {
					if (removeIndexX>=0 && removeIndexX<=worldSize) {
						chunks[j][removeIndexX].unload(this);
					}
					if (addIndexX>=0 && addIndexX<=worldSize) {
						chunks[j][addIndexX].load(this);
					}
				}
			}
		} 
		else if (delta[1] != 0) {
			int removeIndexY = initialPos[1] - delta[1] * chunkViewDistance;
			int addIndexY = newPos[1] + delta[1] * chunkViewDistance;
			
			for (int i=newPos[0] - chunkViewDistance; i <= newPos[0] + chunkViewDistance; i++) {
				if (i>=0 && i<=worldSize) {
					if (removeIndexY>=0 && removeIndexY<=worldSize) {
						chunks[removeIndexY][i].unload(this);
					}
					if (addIndexY>=0 && addIndexY<=worldSize) {
						chunks[addIndexY][i].load(this);
					}
				}
			}
		}
	}
	
	public Chunk getChunk(int x, int y) {
		return chunks[y][x];
	}
	
	public int getWorldSize() {
		return worldSize;
	}
	
	public int[] getGridPosition(Vector3f worldPosition) {
		return new int[] {(int)(worldPosition.x / Terrain.SIZE), (int)(worldPosition.z / Terrain.SIZE)};
	}
	
	public List<Terrain> getTerrains() {
		return terrains;
	}
	
	protected void addTerrain(Terrain terrain) {
		terrains.add(terrain);
	}
	
	protected void removeTerrain(Terrain terrain) {
		terrains.remove(terrain);
	}

}
