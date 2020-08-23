package Collision;

import java.util.ArrayList;

import org.joml.Vector2f;

import Debug.Debug;
import Debug.DebugBox;
import Entities.Entity;
import Entities.PhysicsEntity;
import GameController.GameManager;
import Math.Arithmetic;
import Math.Geometry;
import Math.Vector;
import Tiles.Tile;
import Wrappers.Color;

public abstract class Physics {
	
	public static void calculateDeltas(PhysicsEntity e, Tile[][] grid) {
		
		//Presume to be free falling, until able to prove otherwise
		e.pData.wasGrounded = e.pData.grounded;
		e.pData.grounded = false;
		
		//If jumping, force a velocity change.
		if (e.pData.isJumping && e.pData.walksUpSlopes) {
			//Pass in new diVector2fional axises (x axis perpendicular to the y axis)
			Vector2f yAxis = e.pData.yDir;
			Vector2f xAxis = Vector.rightVector(yAxis); //This is 90 degrees clockwise
			float yVelo = e.pData.velo.y;
			
			//This will modify both x and y velocities.
			e.recordVeloChange(xAxis, yAxis);
			e.resolveVeloChange();
			
			//The point is to retain the y velocity.
			e.pData.velo.y = yVelo;
			e.pData.isJumping = false;
		}
		
		//Grab projected movement
		Vector2f velo = new Vector2f(e.pData.velo);
		
		//Calculate projected position
		Vector2f rawPos = e.getPosition();
		
		//Axises of movement, in order of movement done.
		//Components are stacked onto deltaTemp before being pushed fully to moveDelta.
		Vector2f[] axises = new Vector2f[2];
		axises[0] = e.pData.xDir;
		
		//This is now definitely pointed in the right diVector2fion, 
		//	but I have no clue why it's pointed opposite to the velo deflection tangent.
		axises[1] = e.pData.yDir;
		
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
		e.pData.velo = velo;
		
		if (e.pData.veloChangeQueued) {
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
			
			/**
			 * Determine what behavior to use
			 */
			ArrayList<PhysicsCollisionBehavior> behaviors = null;

			if (Math.abs(tangentDir.y) < 0.8 && tangentDir.x < 0 && e.pData.canBeGrounded) { //TODO: Make this work along any gravitational pull
				//Now, set the behavior list that should be executed.
				e.pData.grounded = true;
				behaviors = e.groundedCollBehaviorList;
			}
			else {
				behaviors = e.nonGroundedCollBehaviorList;
			}
			
			/**
			 * Execute behaviors
			 */
			for (PhysicsCollisionBehavior behavior : behaviors) {
				boolean shouldContinue = behavior.onColl(rawPos, deltaMove, velo, e, grid, moveAxis, axises, moveDir, tangent, delta);
				
				if (!shouldContinue) break;
			}
			
			//Execute common behavior after?
			for (PhysicsCollisionBehavior behavior : e.commonCollBehaviorList) {
				boolean shouldContinue = behavior.onColl(rawPos, deltaMove, velo, e, grid, moveAxis, axises, moveDir, tangent, delta);
				
				if (!shouldContinue) break;
			}
			
			/**
			 * Pushing movement
			 */
			deltaMove.add(delta);
			
			//Enqueue collision response (but store this for later since I want it batched)
			e.pData.collidedWithTile = true;
		}
		
		return isSuccess;
	}
	
	public static boolean isColliding(Vector2f rawPos, Vector2f deltaMove, PhysicsEntity e, Tile[][] grid, Vector2f moveDir, float[] dBuff, Vector2f nBuff) {
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
					if (GameManager.showCollisions) {
						Vector2f pos = new Vector2f(x, y).mul(GameManager.tileSize);
						Debug.highlightRect(pos, new Vector2f(GameManager.tileSize, GameManager.tileSize), new Color(1, 0, 1));
					}
					
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
	
	public static Float getIntersection(HammerShape shape, Vector2f bl, Vector2f ur, int tileX, int tileY, Vector2f moveDir, Vector2f extNormal) {
		//----------------              Uses the Separating Axis Theorem             ----------------
		
		//					STEP 1: PREP DATA
		//Begin by generating a point set for the Vector2fangle.
		Vector2f[] rectPoints = Geometry.pointsFromCorners(bl, ur);
		
		//This needs to use world space, not normalized tile space.
		Vector2f[] shapePoints = new Vector2f[shape.vertices.length];
		for (int i=0; i<shapePoints.length; i++) {
			//Translate and push in points
			Vector2f v = shape.vertices[i];
			float x = v.x;
			float y = v.y;
			
			x *= GameManager.tileSize;
			y *= GameManager.tileSize;
			
			x += tileX * GameManager.tileSize;
			y += tileY * GameManager.tileSize;
			
			shapePoints[i] = new Vector2f(x, y);
		}
		
		float[] out = new float[1];
		boolean hasColl = Geometry.separateAxisCheck(rectPoints, shapePoints,  moveDir,  extNormal, out);
		
		if (hasColl) return out[0];
		else return null;
	}
	

	public static void checkEntityCollision(Hitbox c1, Hitbox c2) {
		//Do separate axis theorem on them
		Entity e1 = (Entity) c1.owner;
		Vector2f[] c1Points = Geometry.pointsFromRect(e1.getPosition(), e1.dim);
		
		Entity e2 = (Entity) c2.owner;
		Vector2f[] c2Points = Geometry.pointsFromRect(e2.getPosition(), e2.dim);
		
		//Todo: inch this
		if (Geometry.separateAxisCheck(c1Points, c2Points, null, null, null)) {
			c1.hitBy(c2);
			c2.hitBy(c1);
		}
	}
}
