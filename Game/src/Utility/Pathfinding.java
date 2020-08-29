package Utility;

import java.util.ArrayList;

import org.joml.Vector2f;

import Debug.Debug;
import Debug.DebugVector;
import Tiles.Tile;

/**
 * Does pathfinding stuff
 * @author Hanzen Shou
 *
 */

public class Pathfinding {
	private ArrayList<Vector2f> nodes;
	
	public Pathfinding() {
		nodes = new ArrayList<>();
	}
	
	public Vector2f nextNode() {
		//Take it off the front
		return nodes.get(0);
	}
	
	public void calculatePath(Vector2f start, Vector2f end, Tile[][] collGrid) {
		nodes.clear();
		
//		//For now, be not subtle about it
		nodes.add(end);
//		
		Debug.enqueueElement(new DebugVector(start, new Vector2f(end).sub(start), 1));
		
		//-------------------- A* ALGORITHM IMPLEMENTATION ---------------------------------
		
		//TODO: Do this, some later time
	}
}
