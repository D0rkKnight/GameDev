package Collision;

import java.util.ArrayList;

import org.joml.Vector2f;

import Debug.Debug;
import Debug.DebugBox;
import Debug.DebugVector;
import Entities.PhysicsEntity;
import GameController.GameManager;
import Tiles.Tile;
import Wrappers.Arithmetic;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Vector;

public abstract class Physics {
	
	public static void calculateDeltas(Hitbox c, Tile[][] grid) {
		
		PhysicsEntity e = c.owner;
		
		//Presume to be free falling, until able to prove otherwise
		e.grounded = false;
		
		//If jumping, force a velocity change.
		if (e.isJumping) {
			//Pass in new diVector2fional axises (x axis perpendicular to the y axis)
			Vector2f yAxis = e.yDir;
			Vector2f xAxis = Vector.rightVector(yAxis); //This is 90 degrees clockwise
			float yVelo = e.velo.y;
			
			//This will modify both x and y velocities.
			e.recordVeloChange(xAxis, yAxis);
			e.resolveVeloChange();
			
			//The point is to retain the y velocity.
			e.velo.y = yVelo;
			e.isJumping = false;
		}
		
		//Grab projected movement
		Vector2f velo = new Vector2f(e.velo);
		
		//Calculate projected position
		Vector2f rawPos = e.getPosition();
		
		//Axises of movement, in order of movement done.
		//Components are stacked onto deltaTemp before being pushed fully to moveDelta.
		Vector2f[] axises = new Vector2f[2];
		axises[0] = e.xDir;
		
		//This is now definitely pointed in the right diVector2fion, 
		//	but I have no clue why it's pointed opposite to the velo deflection tangent.
		axises[1] = e.yDir;
		
		//This is pointed in the right diVector2fion now, now split deltaMove and cache it.
		Vector2f[] deltaComponents = new Vector2f[axises.length];
		float dt = GameManager.deltaT();
		deltaComponents[0] = new Vector2f(axises[0].x * velo.x, axises[0].y * velo.x);
		deltaComponents[1] = new Vector2f(axises[1].x * velo.y, axises[1].y * velo.y);
		
		//Scale against time
		for (Vector2f v : deltaComponents) {
			v.x *= dt;
			v.y *= dt;
		}
		
		//Debug elements
		for (Vector2f comp : deltaComponents) Debug.trackMovementVector(e.getPosition(), comp, 10f);
		
		
		//Holds data to be pushed later, when reasonable movement is found
		Vector2f deltaTemp = new Vector2f(0, 0);
		//And push the different deltas
		for (int i=0; i<axises.length; i++) {
			
			
			//Generate the vector along the axis of movement
			Vector2f dir = axises[i];
			Vector2f deltaAligned = deltaComponents[i];
			
			//TODO: Calculate steps properly
			int tilesTraversed = (int) Math.ceil(deltaAligned.length()/GameManager.tileSize);
			int cycles = Math.max(tilesTraversed, 1) * 2;
			
			Vector2f deltaInch = null;
			for (int j=0; j<cycles; j++) {
				//Now inch forwards with increasingly larger deltas
				float cycleCoef = ((float)j+1)/cycles;
				deltaInch = new Vector2f(deltaAligned.x * cycleCoef, deltaAligned.y * cycleCoef);
				
				//Get the right position that takes into account past movements
				Vector2f newPos = new Vector2f(rawPos.x + deltaTemp.x, rawPos.y + deltaTemp.y);
				
				Debug.enqueueElement(new DebugBox(new Vector2f(newPos).add(deltaInch), new Vector2f(e.dim.x, e.dim.y), 1));
				
				//Move (this modifies deltaInch)
				boolean isSuccess = Physics.moveTo(newPos, deltaInch, velo, e, grid, dir, axises);
				if (!isSuccess) break;
			}
			
			//Push results to buffer
			deltaTemp.x += deltaInch.x;
			deltaTemp.y += deltaInch.y;
		}
		
		//Debug
		Debug.trackMovementVector(new Vector2f(e.getPosition()).add(new Vector2f(0, 20)), deltaTemp, 20f);
		
		//Push buffer to delta
		
		//Push changes
		e.setMoveDelta(deltaTemp);
		e.velo = velo;
		
		e.wasGrounded = e.grounded;
		
		if (e.veloChangeQueued) {
			e.resolveVeloChange();
		}
	}
	
	/**A utility function to be used by update. Given information, it configures deltaMove and velocity as to resolve collisions on the given axis.
	 * These values need to be later pushed to the entity in order for changes to appear.
	 * 
	 * IMPORTANT: Returned deltas may not be aligned with move axis.
	 * 
	 * @param pos
	 * @param delta
	 * @param e
	 * @return Whether or not the entity collided when attempting to move
	 */
	public static boolean moveTo(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid, Vector2f moveAxis, Vector2f[] axises) {
		
		float dirSign = Arithmetic.sign(moveAxis.dot(deltaMove));
		Vector2f moveDir = new Vector2f(moveAxis.x * dirSign, moveAxis.y * dirSign);
		//If movedir is 0, then there is no movement to be done.
		if (moveDir.x == 0 && moveDir.y == 0) return true;
		
		float[] dBuff = new float[1];
		Vector2f normal = new Vector2f();
		boolean isSuccess = !isColliding(rawPos, deltaMove, e, grid, moveDir, dBuff, normal);
		float dist = dBuff[0];
		
		if (!isSuccess) {
			/**
			 * TODO: Fix this horrible spaghetti
			 */
			
			Vector2f delta = new Vector2f();
			
			//Move out of tile
			delta.sub(new Vector2f (moveDir).mul(dist));
			
			//Nudging should only move you as far as you can move.
			float nudge = Math.min(GameManager.NUDGE_CONSTANT, deltaMove.length());
			delta.sub(new Vector2f (moveDir).mul(nudge));
			
			/**
			 * Surface deflection
			 */
			
			//Rotate normal such that it is running along edge
			Vector2f tangent = new Vector2f(-normal.y, normal.x);
			
			//No need to deflect if on the ground, just zero out y velo and x velo will naturally be aligned properly
			//If grounded:
			Vector2f tangentDir = new Vector2f(tangent).normalize();
			if (Math.abs(tangentDir.y) < 0.8 && tangentDir.x < 0) { //TODO: Make this work along any gravitational pull
				//Dunno why this needs to be flipped but it does
				Vector2f newXDir = new Vector2f(-tangent.x, -tangent.y);
				
				if (e.wasGrounded) {
					//Ground-ground transition
					//No need to queue whatever, just force the change.
					/**
					 * Redefining the x axis should only occur when moving along the x axis.
					 */
					if (moveAxis == e.xDir) {
						e.forceDirectionalChange(newXDir, e.yDir);
					}
				} else {
					//Aerial landing
					/**
					 * Doesn't matter which axis of approach, any ground tangent is preferential to the aerial axises.
					 */
					e.forceDirectionalChange(newXDir, e.yDir);
				}
				
				//Make sure you don't continue falling
				velo.y = 0;
				e.grounded = true;
			} 
			
			/**
			 * We don't want to deflect if attached to the ground: the player should not slide.
			 */
			else {
				/**
				 * Moving up 1 tile tall steps
				 */
				boolean tileBumpSuccess = false;
				if (e.wasGrounded && moveAxis == e.xDir) {
					Vector2f rightAxis = Vector.rightVector(axises[1]);
					float dot = rightAxis.dot(tangent);
					
					if (Math.abs(dot) == 0) {
						//System.out.println("perpcoll");
						
						float dy = GameManager.tileSize;
						
						//Simultaneously move in the x dir and up
						Vector2f deltaSnap = new Vector2f(0, dy);
						Vector2f moveSnapSum = new Vector2f(deltaSnap).add(deltaMove);
						tileBumpSuccess = !isColliding(rawPos, moveSnapSum, e, grid, moveDir, null, null);
						
						if (tileBumpSuccess) {
							delta = deltaSnap;
							
							//Slow you down
							velo.x *= 0.3;
						}
					}
				}
				
				if (!tileBumpSuccess) {
					//TODO: Maybe this should just force perpendicular axises?
					//Project velocity onto tangent axis (Tangent points left)
					float tanSpeed = velo.dot(tangent);
					
					//This returns the relevant velocity in world space, but we must change it to use an angled coordinate system.
					Vector2f tangentVector = new Vector2f(tangent.x * tanSpeed, tangent.y * tanSpeed);
					
					Debug.enqueueElement(new DebugVector(new Vector2f(rawPos).add(new Vector2f(0, 100)), tangentVector, 20));
					
					//First x, then y
					Vector2f axisA = axises[0];
					Vector2f axisB = axises[1];
					
					//Somehow the two implementations are different.
					Vector2f compA = new Vector2f(0, 0);
					Vector2f compB = new Vector2f(0, 0);
					float[] magBuff = new float[2];
					Vector.breakIntoComponents(tangentVector, axisA, axisB, compA, compB, magBuff);
					
					velo.x = magBuff[0];
					velo.y = magBuff[1];
				}
			}
			
			/**
			 * Pushing movement
			 */
			deltaMove.add(delta);
			
			//Enqueue collision response (but store this for later since I want it batched)
			e.collidedWithTile = true;
		}
		
		return isSuccess;
	}
	
	private static boolean isColliding(Vector2f rawPos, Vector2f deltaMove, PhysicsEntity e, Tile[][] grid, Vector2f moveDir, float[] dBuff, Vector2f nBuff) {
		Vector2f bl = new Vector2f(rawPos.x + deltaMove.x, rawPos.y + deltaMove.y);
		Vector2f ur = new Vector2f(bl.x + e.dim.x, bl.y + e.dim.y);
		
		//return var
		boolean isColl = false;

		ArrayList<int[]> tilesHit = new ArrayList<>();
		boolean roughPass = roughPass(bl, ur, grid, tilesHit);
		
		//Rough pass
		float dist = 0;
		Vector2f normal = null;
		if (roughPass) {
			float maxMoveDist = 0;
			
			for (int[] p : tilesHit) {
				int x = p[0];
				int y = p[1];
				
				Tile t = grid[x][y];
				
				Vector2f tempNormal = new Vector2f(0, 0);
				
				Float d = getIntersection(t.getHammerState(), bl, ur, x, y, moveDir, tempNormal);
				if (d!=null) {
					if (Math.abs(d) > Math.abs(maxMoveDist)) {
						maxMoveDist = d;
						normal = tempNormal;
					}
					if (GameManager.showCollisions) t.renderer.col = new Color(1, 0, 1);
					
					isColl = true;
				}
			}
			dist = maxMoveDist;
		}
		
		if (dBuff != null) dBuff[0] = dist;
		
		if (normal != null && nBuff != null) {
			nBuff.x = normal.x;
			nBuff.y = normal.y;
		}
		
		return isColl;
	}
	
	/**
	 * Gets tiles that being collided with. Note that it presumes every tile is a unique entity.
	 * Remember that entities should never be inside the terrain.
	 * If an entity falls into terrain, it will eject depending on axisDelta. Even if immobile, axisDelta will default to ejecting to the bottom and left.
	 * @param bl
	 * @param ur
	 * @param moveAxis
	 * @param moveDir
	 * @param grid
	 * @param e
	 * @return
	 */
	private static boolean roughPass(Vector2f bl, Vector2f ur, Tile[][] grid, ArrayList<int[]> hits) {
		boolean tileHit = false;
		
		int boundL = (int) bl.x/GameManager.tileSize;
		int boundR = (int) ur.x/GameManager.tileSize;
		int boundT = (int) ur.y/GameManager.tileSize;
		int boundB = (int) bl.y/GameManager.tileSize;
		
		int gw = grid.length;
		int gh = grid[0].length;
		
		//Honestly should just be doing a complete sweep for all tiles it's in, no need to optimize yet.
		for (int y=boundB; y<=boundT; y++) {
			for (int x=boundL; x<=boundR; x++) {
				if (x < 0 || x >= gw) break;
				if (y < 0 || y >= gh) continue;
				
				Tile t = grid[x][y];
				if (t != null) {
					int[] o = new int[] {x, y};
					hits.add(o);
					
					tileHit = true;
				}
			}
		}
		return tileHit;
	}
	
	/**
	 * Given some information, figure out if there is a collision and how far back to move the entity.
	 * TODO: Code this into a more general form
	 * @param modPos
	 * @param shape
	 * @param corner
	 * @param sideEnteringFrom
	 * @return
	 */
	public static Float getIntersection(HammerShape shape, Vector2f bl, Vector2f ur, int tileX, int tileY, Vector2f moveDir, Vector2f extNormal) {
		//----------------              Uses the Separating Axis Theorem             ----------------
		
		//					STEP 1: PREP DATA
		//Begin by generating a point set for the Vector2fangle.
		Vector2f[] Vector2fPoints = new Vector2f[] {
				new Vector2f(bl.x, bl.y),
				new Vector2f(ur.x, bl.y),
				new Vector2f(ur.x, ur.y),
				new Vector2f(bl.x, ur.y)
		};
		
		//This needs to use world space, not normalized tile space.
		Vector2f[] shapePoints = new Vector2f[shape.points.length];
		for (int i=0; i<shapePoints.length; i++) {
			//Translate and push in points
			Vector2f v = shape.points[i];
			float x = v.x;
			float y = v.y;
			
			x *= GameManager.tileSize;
			y *= GameManager.tileSize;
			
			x += tileX * GameManager.tileSize;
			y += tileY * GameManager.tileSize;
			
			shapePoints[i] = new Vector2f(x, y);
		}
		
		//		STEP 2: ANALYZE
		int edgeCountVector2f = Vector2fPoints.length;
		int edgeCountShape = shapePoints.length;
		
		float shortestDist = Float.POSITIVE_INFINITY;
		Vector2f shortestNormal = null;
		
		//Get normals
		//TODO: Potential error since I'm only counting the shape's normals, and not the Vector2f's normals.
		for (int i=0; i<edgeCountShape; i++) {
			Vector2f p1, p2;
			
			p1 = shapePoints[i];
			p2 = null;
			//If it's the last one, loop to the first
			if (i == shapePoints.length-1) {
				p2 = shapePoints[0];
			} else {
				p2 = shapePoints[i + 1];
			}
			
			//Now get the edge
			Vector2f vec = new Vector2f(p2.x - p1.x, p2.y - p1.y);
			
			//And get the normal
			//Note that the border must be going counterclockwise for the normals to be right.
			//The normal is clockwise of the edge vector.
			Vector2f normal = new Vector2f(vec.y, -vec.x);
			
			//Project vectors and compare overlaps
			//Start with shape vectors
			
			//Grab unit vector of the normal for calculation purposes.
			Vector2f unitNormal = new Vector2f(normal).normalize();
			
			
			float[] shapeBounds = new float[2];
			float[] Vector2fBounds = new float[2];
			
			Vector.projectPointSet(shapePoints, unitNormal, shapeBounds);
			Vector.projectPointSet(Vector2fPoints, unitNormal, Vector2fBounds);
			
			float[] distBuffer = new float[1];
			if (Arithmetic.isIntersecting(shapeBounds[0], shapeBounds[1], Vector2fBounds[0], Vector2fBounds[1], distBuffer)) {
				
				//Project along moveAxis
				float dist = distBuffer[0];
				float moveDist = 0;
				if (dist != 0) {
					Vector2f perpVec = new Vector2f(unitNormal.x * dist, unitNormal.y * dist);

					Vector2f projAxis = new Vector2f(moveDir).normalize();
					
					moveDist = (float) (Math.pow(dist, 2) / perpVec.dot(projAxis));
				} 
				
				else {
					System.err.println("ERROR: distance is zero");
					
					//This is like the same thing as not colliding
					return null;
				}
				
				float absDist = Math.abs(moveDist);
				if (absDist < shortestDist) {
					shortestDist = absDist;
					shortestNormal = unitNormal;
				} 
				
				//If the distance is the same, this stands to reason that there may be an interfereing parallel surface
				//Thus, if this new edge is more reasonable, use it instead.
				if (absDist == shortestDist && absDist != Float.POSITIVE_INFINITY) {
					if (unitNormal.dot(moveDir) < shortestNormal.dot(moveDir)) {
						shortestNormal = unitNormal;
					}
				}
			} else {
				return null;
			}
		}
		
		//Return shortest distance out.
		extNormal.x = shortestNormal.x;
		extNormal.y = shortestNormal.y;
		
		return shortestDist;
	}
}
