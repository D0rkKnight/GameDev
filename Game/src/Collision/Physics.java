package Collision;

import java.util.ArrayList;

import Debug.Debug;
import Debug.DebugBox;
import Debug.DebugVector;
import Entities.PhysicsEntity;
import GameController.GameManager;
import Tiles.Tile;
import Wrappers.Arithmetic;
import Wrappers.Color;
import Wrappers.Hitbox;
import Wrappers.Vector2;

public abstract class Physics {
	
	public static void calculateDeltas(Hitbox c, Tile[][] grid) {
		
		PhysicsEntity e = c.owner;
		
		//Presume to be free falling, until able to prove otherwise
		e.grounded = false;
		
		//If jumping, force a velocity change.
		if (e.isJumping) {
			//Pass in new directional axises (x axis perpendicular to the y axis)
			Vector2 yAxis = e.yDir;
			Vector2 xAxis = yAxis.rightVector(); //This is 90 degrees clockwise
			float yVelo = e.velo.y;
			
			//This will modify both x and y velocities.
			e.recordVeloChange(xAxis, yAxis);
			e.resolveVeloChange();
			
			//The point is to retain the y velocity.
			e.velo.y = yVelo;
			e.isJumping = false;
		}
		
		//Grab projected movement
		Vector2 velo = new Vector2(e.velo);
		
		//Calculate projected position
		Vector2 rawPos = e.getPosition();
		
		//Axises of movement, in order of movement done.
		//Components are stacked onto deltaTemp before being pushed fully to moveDelta.
		Vector2[] axises = new Vector2[2];
		axises[0] = e.xDir;
		
		//This is now definitely pointed in the right direction, 
		//	but I have no clue why it's pointed opposite to the velo deflection tangent.
		axises[1] = e.yDir;
		
		//This is pointed in the right direction now, now split deltaMove and cache it.
		Vector2[] deltaComponents = new Vector2[axises.length];
		float dt = GameManager.deltaT();
		deltaComponents[0] = new Vector2(axises[0].x * velo.x, axises[0].y * velo.x);
		deltaComponents[1] = new Vector2(axises[1].x * velo.y, axises[1].y * velo.y);
		
		//Scale against time
		for (Vector2 v : deltaComponents) {
			v.x *= dt;
			v.y *= dt;
		}
		
		//Debug elements
		for (Vector2 comp : deltaComponents) Debug.trackMovementVector(e.getPosition(), comp, 10f);
		
		
		//Holds data to be pushed later, when reasonable movement is found
		Vector2 deltaTemp = new Vector2(0, 0);
		
		//And push the different deltas
		for (int i=0; i<axises.length; i++) {
			
			
			//Generate the vector along the axis of movement
			Vector2 dir = axises[i];
			Vector2 deltaAligned = deltaComponents[i];
			
			//TODO: Calculate steps properly
			int tilesTraversed = (int) Math.ceil(deltaAligned.magnitude()/GameManager.tileSize);
			int cycles = Math.max(tilesTraversed, 1) * 2;
			
			Vector2 deltaInch = null;
			for (int j=0; j<cycles; j++) {
				//Now inch forwards with increasingly larger deltas
				float cycleCoef = ((float)j+1)/cycles;
				deltaInch = new Vector2(deltaAligned.x * cycleCoef, deltaAligned.y * cycleCoef);
				
				//Get the right position that takes into account past movements
				Vector2 newPos = new Vector2(rawPos.x + deltaTemp.x, rawPos.y + deltaTemp.y);
				
				Debug.enqueueElement(new DebugBox(newPos.add(deltaInch), new Vector2(e.dim.w, e.dim.h), 1));
				
				//Move (this modifies deltaInch)
				boolean isSuccess = Physics.moveTo(newPos, deltaInch, velo, e, grid, dir, axises);
				if (!isSuccess) break;
			}
			
			//Push results to buffer
			deltaTemp.x += deltaInch.x;
			deltaTemp.y += deltaInch.y;
		}
		
		//Debug
		Debug.trackMovementVector(e.getPosition().add(new Vector2(0, 20)), deltaTemp, 20f);
		
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
	 * @param pos
	 * @param delta
	 * @param e
	 * @return Whether or not the entity collided when attempting to move
	 */
	public static boolean moveTo(Vector2 rawPos, Vector2 deltaMove, Vector2 velo, PhysicsEntity e, Tile[][] grid, Vector2 moveAxis, Vector2[] axises) {
		Vector2 bl = new Vector2(rawPos.x + deltaMove.x, rawPos.y + deltaMove.y);
		Vector2 ur = new Vector2(bl.x + e.dim.w, bl.y + e.dim.h);
		
		float dirSign = Arithmetic.sign(moveAxis.dot(deltaMove));
		Vector2 moveDir = new Vector2(moveAxis.x * dirSign, moveAxis.y * dirSign);
		//If movedir is 0, then there is no movement to be done.
		if (moveDir.x == 0 && moveDir.y == 0) return true;
		
		//return var
		boolean isSuccess = true;

		ArrayList<int[]> tilesHit = new ArrayList<>();
		boolean roughPass = isColliding(bl, ur, grid, tilesHit);
		
		//Rough pass
		float dist = 0;
		Vector2 normal = null;
		if (roughPass) {
			float maxMoveDist = 0;
			
			for (int[] p : tilesHit) {
				int x = p[0];
				int y = p[1];
				
				Tile t = grid[x][y];
				
				Vector2 tempNormal = new Vector2(0, 0);
				
				Float d = getIntersection(t.getHammerState(), bl, ur, x, y, moveDir, tempNormal);
				if (d!=null) {
					if (Math.abs(d) > Math.abs(maxMoveDist)) {
						maxMoveDist = d;
						normal = tempNormal;
					}
					if (GameManager.showCollisions) t.renderer.col = new Color(1, 0, 1);
					
					isSuccess = false;
				}
			}
			dist = maxMoveDist;
		}
		
		if (!isSuccess) {
			//Snap to the intended edge -----------------------------------------------------------------------------
			
			//Move in the right direction
			deltaMove.x -= moveDir.x * dist;
			deltaMove.y -= moveDir.y * dist;
			
			//Nudging should only move you as far as you can move.
			float nudge = Math.min(GameManager.NUDGE_CONSTANT, deltaMove.magnitude());
			deltaMove.y -= moveDir.y * nudge;
			deltaMove.x -= moveDir.x * nudge;
			
			/**
			 * Surface deflection
			 */
			
			//Rotate normal such that it is running along edge
			Vector2 tangent = new Vector2(-normal.y, normal.x);
			
			//No need to deflect if on the ground, just zero out y velo and x velo will naturally be aligned properly
			//If grounded:
			if (Math.abs(tangent.unit().y) < 0.8 && tangent.unit().x < 0) {
				//Dunno why this needs to be flipped but it does
				Vector2 newXDir = new Vector2(-tangent.x, -tangent.y);
				
				if (e.wasGrounded) {
					//Ground-ground transition
					//No need to queue whatever, just force the change.
					/**
					 * Redefining the x axis should only occur when moving along the x axis.
					 */
					if (moveAxis == e.xDir) {
						System.out.println("Grounded transition");
						e.forceDirectionalChange(newXDir, e.yDir);
					}
				} else {
					//Aerial landing
					/**
					 * Doesn't matter which axis of approach, any ground tangent is preferential to the aerial axises.
					 */
					System.out.println("Aerial landing");
					e.forceDirectionalChange(newXDir, e.yDir);
				}
				
				//Make sure you don't continue falling
				velo.y = 0;
				e.grounded = true;
				
				System.out.println("\nVelo: "+velo.toString());
				System.out.println("XDir: "+e.xDir.toString());
				System.out.println("YDir: "+e.yDir.toString());
			} 
			
			/**
			 * We don't want to deflect if attached to the ground: the player should not slide.
			 */
			else {
				//Project velocity onto tangent axis (Tangent points left)
				float tanSpeed = velo.dot(tangent);
				
				//This returns the relevant velocity in world space, but we must change it to use an angled coordinate system.
				Vector2 tangentVector = new Vector2(tangent.x * tanSpeed, tangent.y * tanSpeed);
				
				Debug.enqueueElement(new DebugVector(rawPos.add(new Vector2(0, 100)), tangentVector, 20));
				
				//First x, then y
				Vector2 axisA = axises[0];
				Vector2 axisB = axises[1];
				
				//Somehow the two implementations are different.
				Vector2 compA = new Vector2(0, 0);
				Vector2 compB = new Vector2(0, 0);
				float[] magBuff = new float[2];
				tangentVector.breakIntoComponents(axisA, axisB, compA, compB, magBuff);
				
				velo.x = magBuff[0];
				velo.y = magBuff[1];
			}
			
			System.out.println(e.xDir.toString());
			
			//Enqueue collision response (but store this for later since I want it batched)
			e.collidedWithTile = true;
		}
		
		return isSuccess;
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
	private static boolean isColliding(Vector2 bl, Vector2 ur, Tile[][] grid, ArrayList<int[]> hits) {
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
	public static Float getIntersection(HammerShape shape, Vector2 bl, Vector2 ur, int tileX, int tileY, Vector2 moveDir, Vector2 extNormal) {
		//----------------              Uses the Separating Axis Theorem             ----------------
		
		//					STEP 1: PREP DATA
		//Begin by generating a point set for the rectangle.
		Vector2[] rectPoints = new Vector2[] {
				new Vector2(bl.x, bl.y),
				new Vector2(ur.x, bl.y),
				new Vector2(ur.x, ur.y),
				new Vector2(bl.x, ur.y)
		};
		
		//This needs to use world space, not normalized tile space.
		Vector2[] shapePoints = new Vector2[shape.points.length];
		for (int i=0; i<shapePoints.length; i++) {
			//Translate and push in points
			Vector2 v = shape.points[i];
			float x = v.x;
			float y = v.y;
			
			x *= GameManager.tileSize;
			y *= GameManager.tileSize;
			
			x += tileX * GameManager.tileSize;
			y += tileY * GameManager.tileSize;
			
			shapePoints[i] = new Vector2(x, y);
		}
		
		//		STEP 2: ANALYZE
		int edgeCountRect = rectPoints.length;
		int edgeCountShape = shapePoints.length;
		
		float shortestDist = Float.POSITIVE_INFINITY;
		Vector2 shortestNormal = null;
		
		//Get normals
		//TODO: Potential error since I'm only counting the shape's normals, and not the rect's normals.
		for (int i=0; i<edgeCountShape; i++) {
			Vector2 p1, p2;
			
			p1 = shapePoints[i];
			p2 = null;
			//If it's the last one, loop to the first
			if (i == shapePoints.length-1) {
				p2 = shapePoints[0];
			} else {
				p2 = shapePoints[i + 1];
			}
			
			//Now get the edge
			Vector2 vec = new Vector2(p2.x - p1.x, p2.y - p1.y);
			
			//And get the normal
			//Note that the border must be going counterclockwise for the normals to be right.
			//The normal is clockwise of the edge vector.
			Vector2 normal = new Vector2(vec.y, -vec.x);
			
			//Project vectors and compare overlaps
			//Start with shape vectors
			
			//Grab unit vector of the normal for calculation purposes.
			Vector2 unitNormal = normal.unit();
			
			
			float[] shapeBounds = new float[2];
			float[] rectBounds = new float[2];
			
			Vector2.projectPointSet(shapePoints, unitNormal, shapeBounds);
			Vector2.projectPointSet(rectPoints, unitNormal, rectBounds);
			
			float[] distBuffer = new float[1];
			if (Arithmetic.isIntersecting(shapeBounds[0], shapeBounds[1], rectBounds[0], rectBounds[1], distBuffer)) {
				
				//Project along moveAxis
				float dist = distBuffer[0];
				float moveDist = 0;
				if (dist != 0) {
					Vector2 perpVec = new Vector2(unitNormal.x * dist, unitNormal.y * dist);

					Vector2 projAxis = moveDir.unit();
					
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
