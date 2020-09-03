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
	public HashMap<String, Tile[][]> grids; // [x][y]
	public float w;
	public float h;

	public ArrayList<CompEdge> compEdges;

	/*
	 * entrance coordinates, contains list of positions [topleft, topright, botleft,
	 * botright]
	 */
	private Vector2f[][] entrances;
	/*
	 * entrance info, contains list of entrance info [entranceID,
	 * entrancethatitlinkstoID]
	 */
	private int[][] entranceInfo;
	/*
	 * list of entities in the room. does not include player, accessed by
	 * GameManager to determine collisions
	 */
	private ArrayList<Entity> entities;

	public Map(HashMap<String, Tile[][]> mapData, Vector2f[][] entrances, int[][] entranceInfo,
			ArrayList<Entity> entities) {
		// Init tiles
		for (Tile[][] g : mapData.values()) {
			for (int i = 0; i < g.length; i++)
				for (int j = 0; j < g[0].length; j++) {
					Tile t = g[i][j];
					if (t != null) {
						t.init(new Vector2f(i * GameManager.tileSize, j * GameManager.tileSize),
								new Vector2f(GameManager.tileSize, GameManager.tileSize));
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
		// -------------------- STEP 1: Find empty spaces -----------------------
		int id = 0;

		int tilesW = grid.length;
		int tilesT = grid[0].length;

		@SuppressWarnings("unchecked")
		ArrayList<Integer>[][] traversed = new ArrayList[tilesW + 1][tilesT + 1];

		ArrayList<CompEdge> edgeArr = new ArrayList<>();

		// Populate array
		for (int i = 0; i < traversed.length; i++)
			for (int j = 0; j < traversed[0].length; j++) {
				traversed[i][j] = new ArrayList<Integer>();
			}

		// Little area around the border where the lattice is not checked
		for (int i = 1; i < tilesW; i++) {
			for (int j = 1; j < tilesT; j++) {
				if (!traversed[i][j].isEmpty())
					continue;

				populateEdgesFromPoint(i, j, grid, id, traversed, tilesW, tilesT, edgeArr);
				id++;
			}
		}

		compEdges = edgeArr;

		// -------------------- KEEP FOR FUTURE DEBUGGING ----------------------------
		float group = 1f;
		for (CompEdge ce : compEdges) {
			float counter = (float) Math.random();
			for (CompEdgeSegment e : ce.edgeSegs) {
				Vector2f dir = new Vector2f(e.v2).sub(e.v1.x, e.v1.y);
				Debug.enqueueElement(new DebugVector(new Vector2f(e.v1).mul(GameManager.tileSize), dir,
						GameManager.tileSize, 10000));

				// Draw normals too!
				Vector2f nOrigin = Vector.lerp(new Vector2f(e.v1), new Vector2f(e.v2), 0.5f).mul(GameManager.tileSize);
				Debug.enqueueElement(new DebugVector(nOrigin, e.normal, GameManager.tileSize,
						new Color(group, counter, 1 - group, 1), 10000));
				counter *= 0.990;
			}
			group = (float) Math.random();
		}
	}

	private void populateEdgesFromPoint(int i, int j, Tile[][] grid, int id, ArrayList<Integer>[][] traversed,
			int tilesW, int tilesT, ArrayList<CompEdge> edgeArr) {

		ArrayList<CompEdgeSegment> edges = findValidConnections(new Vector2i(i, j), grid);
		if (edges.size() > 2) {
			// Cannot populate with from an arbitrary setup like this
			if (Debug.logIssues)
				System.err.println("Cannot populate from point " + i + ", " + j + " due to too many options");
			return;
		}

		traversed[i][j].add(id); // The starting lattice point has been traversed by this id.

		LinkedList<CompEdgeSegment> compositeEdge = new LinkedList<>();
		LinkedList<CompEdgeSegment> edgeStack = new LinkedList<>();

		// Edges can be dealt with in whatever order
		for (CompEdgeSegment e : edges) {
			edgeStack.add(e);
		}

		if (edgeStack.isEmpty())
			return;

		// Begin searching if the current point has not been traversed before.
		boolean loop = false;

		while (!edgeStack.isEmpty()) {
			CompEdgeSegment currEdge = edgeStack.removeFirst();
			traversed[currEdge.v2.x][currEdge.v2.y].add(id);

			// Store the current edge different depending on its orientation.
			float cross = new Vector3f(currEdge.getEdgeV(), 0).cross(new Vector3f(currEdge.normal, 0)).z;
			if (cross > 0) // Regular orientation
				compositeEdge.addLast(currEdge);

			else { // Reverse orientation
				CompEdgeSegment revEdge = new CompEdgeSegment(currEdge.v2, currEdge.v1, currEdge.normal);
				compositeEdge.addFirst(revEdge);
			}

			edges = findValidConnections(currEdge.v2, grid);

			// Reject the vector pointing back
			boolean backpathCleared = false;

			for (int a = 0; a < edges.size(); a++) {
				CompEdgeSegment backE = edges.get(a);
				Vector2i dir1 = currEdge.getEdgeV();
				Vector2i dir2 = backE.getEdgeV();

				if (dir1.x == -dir2.x && dir1.y == -dir2.y) {
					edges.remove(a);
					backpathCleared = true;
					break;
				}
			}
			if (!backpathCleared)
				new Exception("Back path not cleared!").printStackTrace();

			// Clear out edges on the border
			for (int a = edges.size() - 1; a >= 0; a--) {
				Vector2i v2 = edges.get(a).v2;
				if (v2.x == 0 || v2.x == tilesW || v2.y == 0 || v2.y == tilesT) {
					edges.remove(a);
				}
			}

			if (!edges.isEmpty()) {

				// Check every point and find the closest one.
				CompEdgeSegment bestChoice = null;
				float bestDot = 0;
				float bestNDot = 0;

				// Points into tip of exploration
				Vector2f baseEdge = new Vector2f(currEdge.v2.x - currEdge.v1.x, currEdge.v2.y - currEdge.v1.y);

				for (CompEdgeSegment e : edges) {
					Vector2f newEdge = new Vector2f(e.getEdgeV()).normalize();

					// Get cross product and dot product
					float dot = new Vector2f(newEdge).dot(baseEdge);
					float nDot = new Vector2f(newEdge).dot(currEdge.normal);

					boolean doReplace = false;

					if (bestChoice == null)
						doReplace = true;

					// Cross product indicates which side of the vector should be preferred
					// TODO: Use a dot product with the normal instead.
					else if (Arithmetic.sign(nDot) - Arithmetic.sign(bestNDot) > 0)
						doReplace = true;
					else if (nDot > 0 && dot < bestDot)
						doReplace = true;
					else if (nDot < 0 && bestNDot < 0 && dot > bestDot)
						doReplace = true;

					if (doReplace) {
						bestChoice = e;
						bestDot = dot;
						bestNDot = nDot;
					}
				}

				// If we've already been to where we're going next, stop.
				Vector2i choiceV = bestChoice.v2;
				if (traversed[choiceV.x][choiceV.y].contains(id)) { // Break through loop met
					compositeEdge.add(bestChoice); // finish the loop
					loop = true;
					break;

					// TODO: Don't need to break here
				}

				edgeStack.addFirst(bestChoice);
			}
		}

		// Link segments
		for (int a = 0; a < compositeEdge.size(); a++) {
			CompEdgeSegment e1 = compositeEdge.get(a);
			CompEdgeSegment e2 = null;

			if (a + 1 < compositeEdge.size())
				e2 = compositeEdge.get(a + 1);
			else if (loop)
				e2 = compositeEdge.get(0);

			// Link edges
			if (e2 != null) {
				e1.nextSeg = e2;
				e2.prevSeg = e1;
			}

			// Attach to grid
			Vector2i v1 = e1.v1;
			Vector2i v2 = e1.v2;
			Vector2f n = e1.normal;

			int x = Math.min(v1.x, v2.x);
			int y = Math.min(v1.y, v2.y);

			// The normal is enough to identify which side the segment is on.
			// Horizontal segment
			if (v1.y == v2.y) {

				if (n.y > 0)
					y--;
			}

			else if (v1.x == v2.x) {
				if (n.x > 0)
					x--;
			}

			// If it's slanted, the segment is on the right tile.

			Tile t = grid[x][y];
			if (t == null) {
				new Exception("Can't find tile to attach edge segment to.").printStackTrace();
				System.exit(1);
			}

			t.edgeSegs.add(e1);
		}

		// Store the data now that processing is done.
		edgeArr.add(new CompEdge(compositeEdge));
	}

	private ArrayList<CompEdgeSegment> findValidConnections(Vector2i p, Tile[][] grid) {
		// Go around each tile and find a valid edge. There are 4 tiles
		Vector2f pf = new Vector2f(p);
		ArrayList<CompEdgeSegment> connections = new ArrayList<>();
		ArrayList<CompEdgeSegment> duplicates = new ArrayList<>(); // Arraylist of all duplicate tiles.

		for (int i = p.x - 1; i <= p.x; i++) {
			for (int j = p.y - 1; j <= p.y; j++) {
				Tile t = grid[i][j];
				if (t == null)
					continue;

				HammerShape shape = t.getHammerState();
				Vector2f pGrid = new Vector2f(i, j); // Position of the shape in grid cords

				// Try to find point p in hammershape.
				Vector2f[] verts = shape.vertices;
				for (int k = 0; k < verts.length; k++) {
					Vector2f v = verts[k];
					Vector2f vGrid = new Vector2f(v).add(pGrid); // Position of the vertex in grid cords

					if (vGrid.equals(pf, 0)) {
						int vCount = verts.length;
						int index1 = (k + vCount - 1) % vCount;
						int index2 = (k + vCount + 1) % vCount;

						Vector2f prevV = new Vector2f(verts[index1]);
						Vector2f nextV = new Vector2f(verts[index2]);

						Vector2f[] normals = shape.normals;
						Vector2f normal1 = new Vector2f(normals[(k + normals.length - 1) % normals.length]); // Previous
																												// normal
																												// means
																												// -1
						Vector2f normal2 = new Vector2f(normals[k]); // Next normal is the one lined up with the central
																		// point

						// These vertices need to be translated into grid cords
						prevV.add(pGrid);
						nextV.add(pGrid);

						// Snap these to the grid
						if (!Vector.isInt(prevV) || !Vector.isInt(nextV)) {
							new Exception("Vertex isn't grid aligned!").printStackTrace();
							System.exit(1);
						}

						Vector2i gp1 = new Vector2i((int) prevV.x, (int) prevV.y);
						Vector2i gp2 = new Vector2i((int) nextV.x, (int) nextV.y);

						CompEdgeSegment e1 = new CompEdgeSegment(p, gp1, normal1);
						CompEdgeSegment e2 = new CompEdgeSegment(p, gp2, normal2);

						// Reject duplicates
						logEdgeDuplicate(e1, connections, duplicates);
						logEdgeDuplicate(e2, connections, duplicates);

						connections.add(e1);
						connections.add(e2);
					}
				}
			}
		}

		// Clear connections of duplicates
		for (CompEdgeSegment d : duplicates) {
			for (int i = connections.size() - 1; i >= 0; i--) {
				CompEdgeSegment c = connections.get(i);
				if (d.edgesEqual(c))
					connections.remove(i);
			}
		}

		return connections;
	}

	private void logEdgeDuplicate(CompEdgeSegment e, ArrayList<CompEdgeSegment> connections,
			ArrayList<CompEdgeSegment> duplicates) {
		for (CompEdgeSegment vi : connections) {

			// If it is a duplicate
			if (e.edgesEqual(vi)) {

				// Then check for duplicates in duplicate array
				boolean dupePresent = false;
				for (CompEdgeSegment d : duplicates) {
					if (e.edgesEqual(d))
						dupePresent = true; // TODO: .equals may not work here
				}

				if (!dupePresent)
					duplicates.add(e);
			}
		}
	}

	static class CompEdge {
		public LinkedList<CompEdgeSegment> edgeSegs;

		public CompEdge(LinkedList<CompEdgeSegment> edgeSegs) {
			this.edgeSegs = edgeSegs;

			// Link things up
			for (CompEdgeSegment es : edgeSegs)
				es.owner = this;
		}
	}

	public static class CompEdgeSegment {
		public CompEdgeSegment prevSeg;
		public CompEdgeSegment nextSeg;
		public CompEdge owner;

		public Vector2i v1;
		public Vector2i v2;
		public Vector2f normal;

		public CompEdgeSegment(Vector2i v1, Vector2i v2, Vector2f normal) {
			this.v1 = v1;
			this.v2 = v2;
			this.normal = normal;
		}

		public boolean edgesEqual(CompEdgeSegment e) {

			if (v1.equals(e.v1) && v2.equals(e.v2)) {
				return true;
			}

			return false;
		}

		public Vector2i getEdgeV() {
			return new Vector2i(v2).sub(v1);
		}
	}
}
