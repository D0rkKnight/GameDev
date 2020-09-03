package GameController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import Collision.HammerShapes.HammerShape;
import Debugging.Debug;
import Debugging.DebugVector;
import Entities.Framework.Entity;
import Tiles.Tile;
import Utility.Arithmetic;
import Utility.Vector;
import Wrappers.Color;
public class Map {
	public HashMap<String, Tile[][]> grids; //[x][y]
	public float w;
	public float h;
	
	public CompEdge[] compEdges;
	
	/*
	 * entrance coordinates, contains list of positions [topleft, topright, botleft, botright]
	 */
	private Vector2f[][] entrances; 
	/*
	 * entrance info, contains list of entrance info [entranceID, entrancethatitlinkstoID]
	 */
	private int[][] entranceInfo;
	/*
	 * list of entities in the room. does not include player, accessed by GameManager to determine collisions
	 */
	private ArrayList<Entity> entities;
	public Map(HashMap<String, Tile[][]> mapData, Vector2f[][] entrances, int[][] entranceInfo, ArrayList<Entity> entities) {
		//Init tiles
		for (Tile[][] g : mapData.values()) {
			for (int i=0; i<g.length; i++) for (int j=0; j<g[0].length; j++) {
				Tile t = g[i][j];
				if (t != null) {
					t.init(new Vector2f(i*GameManager.tileSize, j*GameManager.tileSize), new Vector2f(GameManager.tileSize, GameManager.tileSize));
				}
			}
		}
		
		
		grids = mapData;
		w = grids.get(GameManager.GRID_SET).length * GameManager.tileSize;
		h = grids.get(GameManager.GRID_SET)[0].length * GameManager.tileSize;
		
		this.setEntrances(entrances);
		this.setEntranceInfo(entranceInfo);
		this.setEntities(entities);
		
		generateEdges(grids.get(GameManager.GRID_COLL));
	}
	
	public Vector2f[][] getEntrances() {
		return entrances;
	}
	private void setEntrances(Vector2f[][] entrances) {
		this.entrances = entrances;
	}
	public int[][] getEntranceInfo() {
		return entranceInfo;
	}
	private void setEntranceInfo(int[][] entranceInfo) {
		this.entranceInfo = entranceInfo;
	}
	public ArrayList<Entity> getEntities() {
		return entities;
	}
	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
	}
	
	
	
	private void generateEdges(Tile[][] grid) {
		//-------------------- STEP 1: Find empty spaces -----------------------
		final int EDGE_SEARCH_BLOCKED = 1;
		final int EDGE_SEARCH_START = 2;
		
		int id = EDGE_SEARCH_START;
		
		int tilesW = grid.length;
		int tilesT = grid[0].length;
		
		ArrayList<Integer>[][] traversed = new ArrayList[tilesW+1][tilesT+1];
		ArrayList<ArrayList<GridAlignedEdge>> edgeArr = new ArrayList<>();
		ArrayList<Boolean> isLoop = new ArrayList<>();
		//Populate array
		for (int i=0; i<traversed.length; i++) for (int j=0; j<traversed[0].length; j++) {
			traversed[i][j] = new ArrayList<Integer>();
		}
		
		//Little area around the border where the lattice is not checked
		for (int i=1; i<tilesW-1; i++) {
			for (int j=1; j<tilesT-1; j++) {
				
				Tile t = grid[i][j];
				if (t == null) continue;
		
				
				if (!traversed[i][j].isEmpty()) continue; 
				
				ArrayList<GridAlignedEdge> edges = findValidConnections(new Vector2i(i, j), grid);
				
				//Choose the vector with a positive cross product with the normal
				GridAlignedEdge currEdge = null;
				for (GridAlignedEdge e : edges) {
					Vector2i ev = e.getEdgeV();
					float cross = new Vector3f(ev.x, ev.y, 0).cross(new Vector3f(e.normal.x, e.normal.y, 0)).z;
					
					if (cross > 0) {
						currEdge = e;
						break;
					}
				}
				if (currEdge == null) continue;
				
				traversed[currEdge.v1.x][currEdge.v1.y].add(id);
				ArrayList<GridAlignedEdge> compositeEdge = new ArrayList<>();
				
				//Begin searching if the current point has not been traversed before.
				boolean loop = false;
				while(true) {
					
					traversed[currEdge.v2.x][currEdge.v2.y].add(id);
					
					compositeEdge.add(currEdge);
					
					edges = findValidConnections(currEdge.v2, grid);
					
					//Reject the vector pointing back
					boolean backpathCleared = false;
					for (int a=0; a<edges.size(); a++) {
						GridAlignedEdge backE = edges.get(a);
						Vector2i dir1 = currEdge.getEdgeV();
						Vector2i dir2 = backE.getEdgeV();
						
						if (dir1.x == -dir2.x && dir1.y == -dir2.y) {
							edges.remove(a);
							backpathCleared = true;
							break;
						}
					}
					if (!backpathCleared) new Exception("Back path not cleared!").printStackTrace();
					
					if (!edges.isEmpty()) {
						//Check every point and find the closest one.
						GridAlignedEdge bestChoice = null;
						float bestDot = 0;
						float bestCross = 0;
						
						Vector2f baseEdge = new Vector2f(currEdge.v2.x - currEdge.v1.x, currEdge.v2.y - currEdge.v1.y); //Points into tip of exploration
						for (GridAlignedEdge e : edges) {
							Vector2f newEdge = new Vector2f(e.getEdgeV()).normalize();
							
							//Get cross product and dot product
							float dot = new Vector2f(newEdge).dot(baseEdge);
							float cross = new Vector3f(baseEdge, 0).cross(new Vector3f(newEdge, 0)).z;
							
							boolean doReplace = false;
							
							if (bestChoice == null) doReplace = true;
							else if (Arithmetic.sign(cross) > Arithmetic.sign(bestCross)) doReplace = true;
							else if (cross > 0 && dot < bestDot) doReplace = true;
							else if (bestCross < 0 && cross < 0 && dot > bestDot) doReplace = true;
							
							if (doReplace) {
								bestChoice = e;
								bestDot = dot;
								bestCross = cross;
							}
						}
						
						//If we've already been to where we're going next, stop.
						Vector2i choiceV = bestChoice.v2;
						if (traversed[choiceV.x][choiceV.y].contains(id)) { //Break through loop met
							compositeEdge.add(bestChoice); //finish the loop
							
							id ++;
							loop = true;
							break;
						}
						
						currEdge = bestChoice;
					}
					
					else { //Break through path end
						id ++;
						loop = false;
						break;
					}
				}
				
				edgeArr.add(compositeEdge);
				isLoop.add(loop);
			}
		}
		
//		//-------------------- KEEP FOR FUTURE DEBUGGING ----------------------------
//		float group = 1f;
//		for (ArrayList<GridAlignedEdge> compEdge : edgeArr) {
//			float counter = 1f;
//			for (GridAlignedEdge e : compEdge) {
//				Vector2f dir = new Vector2f(e.v2).sub(e.v1.x, e.v1.y);
//				Debug.enqueueElement(new DebugVector(new Vector2f(e.v1).mul(GameManager.tileSize), dir, GameManager.tileSize, 10000));
//				
//				//Draw normals too!
//				Vector2f nOrigin = Vector.lerp(new Vector2f(e.v1), new Vector2f(e.v2), 0.5f).mul(GameManager.tileSize);
//				Debug.enqueueElement(new DebugVector(nOrigin, e.normal, GameManager.tileSize, new Color(group, counter, 1-group, 1), 10000));
//				counter *= 0.990;
//			}
//			group *= 0.8;
//		}
		
		//Now stitch edges together and apply to tiles
		compEdges = new CompEdge[edgeArr.size()];
		for (int i=0; i<compEdges.length; i++) {
			ArrayList<GridAlignedEdge> edge = edgeArr.get(i);
			CompEdgeSegment[] edgeSegs = new CompEdgeSegment[edge.size()];
			
			//Load edges in
			for (int j=0; j<edgeSegs.length; j++) {
				edgeSegs[j] = new CompEdgeSegment(null, null, edge.get(j));
			}
			
			//Stitch edges (don't loop)
			for (int j=0; j<edgeSegs.length; j++) {
				CompEdgeSegment currEdge = edgeSegs[j];
				CompEdgeSegment nextEdge = null;
				
				if (j+1 < edgeSegs.length) nextEdge = edgeSegs[j+1];
				else if (isLoop.get(i)) nextEdge = edgeSegs[0];
				
				//Link edge segments together
				if (nextEdge != null) {
					currEdge.nextSeg = nextEdge;
					nextEdge.prevSeg = currEdge;
				}
				
				//While we're at it, store the current edge to a grid
				Vector2i v1 = currEdge.edge.v1;
				Vector2i v2 = currEdge.edge.v2;
				Vector2f n = currEdge.edge.normal;
				
				int x = Math.min(v1.x, v2.x);
				int y = Math.min(v1.y, v2.y);
				
				//The normal is enough to identify which side the segment is on.
				//Horizontal segment
				if (v1.y == v2.y) {
					
					if (n.y > 0) y --;
				}
				
				else if (v1.x == v2.x) {
					if (n.x > 0) x --;
				}
				
				//If it's slanted, the segment is on the right tile.
				
				Tile t = grid[x][y];
				if (t == null) {
					new Exception("Can't find tile to attach edge segment to.").printStackTrace();
					System.exit(1);
				}
				
				t.edgeSegs.add(currEdge);
			}
			
			//Store to a comp edge
			compEdges[i] = new CompEdge(edgeSegs);
		}
	}
	
	private ArrayList<GridAlignedEdge> findValidConnections(Vector2i p, Tile[][] grid) {
		//Go around each tile and find a valid edge. There are 4 tiles
		Vector2f pf = new Vector2f(p);
		ArrayList<GridAlignedEdge> connections = new ArrayList<>();
		ArrayList<GridAlignedEdge> duplicates = new ArrayList<>(); //Arraylist of all duplicate tiles.
		
		for (int i=p.x-1; i<=p.x; i++) {
			for (int j=p.y-1; j<=p.y; j++) {
				Tile t = grid[i][j];
				if (t==null) continue;
				
				HammerShape shape = t.getHammerState();
				Vector2f pGrid = new Vector2f(i, j); //Position of the shape in grid cords
				
				//Try to find point p in hammershape.
				Vector2f[] verts = shape.vertices;
				for (int k=0; k<verts.length; k++) {
					Vector2f v = verts[k];
					Vector2f vGrid = new Vector2f(v).add(pGrid); //Position of the vertex in grid cords
					
					if (vGrid.equals(pf, 0)) {
						int vCount = verts.length;
						int index1 = (k+vCount-1)%vCount;
						int index2 = (k+vCount+1)%vCount;
						
						Vector2f prevV = new Vector2f(verts[index1]);
						Vector2f nextV = new Vector2f(verts[index2]);
						
						Vector2f[] normals = shape.normals;
						Vector2f normal1 = new Vector2f(normals[(k+normals.length-1)%normals.length]); //Previous normal means -1
						Vector2f normal2 = new Vector2f(normals[k]); //Next normal is the one lined up with the central point
						
						//These vertices need to be translated into grid cords
						prevV.add(pGrid);
						nextV.add(pGrid);
						
						//Snap these to the grid
						if (!VectorIsInt(prevV) || !VectorIsInt(nextV)) {
							new Exception ("Vertex isn't grid aligned!").printStackTrace();
							System.exit(1);
						}
						
						Vector2i gp1 = new Vector2i((int) prevV.x, (int) prevV.y);
						Vector2i gp2 = new Vector2i((int) nextV.x, (int) nextV.y);
						
						GridAlignedEdge e1 = new GridAlignedEdge(p, gp1, normal1);
						GridAlignedEdge e2 = new GridAlignedEdge(p, gp2, normal2);
						
						//Reject duplicates
						logEdgeDuplicate(e1, connections, duplicates);
						logEdgeDuplicate(e2, connections, duplicates);
						
						connections.add(e1);
						connections.add(e2);
					}
				}
			}
		}
		
		//Clear connections of duplicates
		for (GridAlignedEdge d : duplicates) {
			for (int i=connections.size()-1; i>=0; i--) {
				GridAlignedEdge c = connections.get(i);
				if (d.equals(c)) connections.remove(i);
			}
		}
		
		return connections;
	}
	
	private void logEdgeDuplicate(GridAlignedEdge e, ArrayList<GridAlignedEdge> connections, ArrayList<GridAlignedEdge> duplicates) {
		for (GridAlignedEdge vi : connections) {
			if (e.equals(vi)) {
				boolean dupePresent = false;
				for (GridAlignedEdge d : duplicates) {
					if (e.equals(d)) dupePresent = true; //TODO: .equals may not work here
				}
				
				if (!dupePresent) duplicates.add(e);
			}
		}
	}
	
	private boolean VectorIsInt(Vector2f v) {
		if (v.x == (int)v.x && v.y == (int)v.y) return true;
		else return false;
	}
	
	//Use this in the Separate axis theorem too
	public static class GridAlignedEdge {
		public Vector2i v1;
		public Vector2i v2;
		public Vector2f normal;
		
		public GridAlignedEdge(Vector2i v1, Vector2i v2, Vector2f normal) {
			this.v1 = v1;
			this.v2 = v2;
			this.normal = normal;
		}
		
		@Override
		public boolean equals(Object o) {
			GridAlignedEdge e = (GridAlignedEdge) o;

			if (v1.equals(e.v1) && v2.equals(e.v2)) {
				return true;
			}
			
			return false;
		}
		
		public Vector2i getEdgeV() {return new Vector2i(v2).sub(v1);}
	}
	
	static class CompEdge {
		public CompEdgeSegment[] edgeSegs;
		
		public CompEdge(CompEdgeSegment[] edgeSegs) {
			this.edgeSegs = edgeSegs;
			
			//Link things up
			for (CompEdgeSegment es : edgeSegs) es.owner = this;
		}
	}
	
	public static class CompEdgeSegment {
		public CompEdgeSegment prevSeg;
		public CompEdgeSegment nextSeg;
		public GridAlignedEdge edge;
		public CompEdge owner;
		
		public CompEdgeSegment(CompEdgeSegment prevSeg, CompEdgeSegment nextSeg, GridAlignedEdge edge) {
			this.prevSeg = prevSeg;
			this.nextSeg = nextSeg;
			this.edge = edge;
		}
	}
}
