package Collision;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Behaviors.PhysicsCollisionBehavior;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Debugging.DebugPolygon;
import Entities.Framework.PhysicsEntity;
import GameController.GameManager;
import GameController.Time;
import Tiles.Tile;
import Utility.Arithmetic;
import Utility.Geometry;
import Utility.Vector;
import Wrappers.Color;

public abstract class Physics {

	public static final float NUDGE_CONSTANT = 0.1f;

	// Don't use enumerators since these ints also specify array indices (and there
	// are only 2 axises)
	public static final int X = 0;
	public static final int Y = 1;

	public static void calculateDeltas(PhysicsEntity e, Tile[][] grid) {

		// Presume to be free falling, until able to prove otherwise
		e.pData.wasGrounded = e.pData.grounded;
		e.pData.grounded = false;

		// If jumping, force a velocity change.
		if (e.pData.isJumping && e.pData.walksUpSlopes) {

			// Pass in new diVector2fional axises (x axis perpendicular to the y axis)
			Vector2f yAxis = e.pData.yDir;
			Vector2f xAxis = Vector.rightVector(yAxis); // This is 90 degrees clockwise
			float yVelo = e.pData.velo.y;

			// This will modify both x and y velocities.
			e.recordVeloChange(xAxis, yAxis);
			e.resolveVeloChange();

			// The point is to retain the y velocity.
			e.pData.velo.y = yVelo;
			e.pData.isJumping = false;
		}

		// Grab projected movement
		Vector2f velo = new Vector2f(e.pData.velo);

		// Calculate projected position
		Vector2f rawPos = e.getPosition();

		// Axises of movement, in order of movement done.
		// Components are stacked onto deltaTemp before being pushed fully to moveDelta.
		Vector2f[] axises = new Vector2f[2];
		axises[X] = e.pData.xDir;

		// This is now definitely pointed in the right diVector2fion,
		// but I have no clue why it's pointed opposite to the velo deflection tangent.
		axises[Y] = e.pData.yDir;

		// This is pointed in the right diVector2fion now, now split deltaMove and cache
		// it.
		Vector2f[] deltaComponents = new Vector2f[axises.length];
		float dt = Time.deltaT();
		deltaComponents[X] = new Vector2f(axises[X].x * velo.x, axises[X].y * velo.x);
		deltaComponents[Y] = new Vector2f(axises[Y].x * velo.y, axises[Y].y * velo.y);

		// Scale against time
		for (Vector2f v : deltaComponents) {
			v.x *= dt;
			v.y *= dt;
		}

		// Debug elements
		for (Vector2f comp : deltaComponents)
			Debug.trackMovementVector(e.getPosition(), comp, 10f);

		// Holds data to be pushed later, when reasonable movement is found
		Vector2f deltaTemp = new Vector2f(0, 0);
		// And push the different deltas
		for (int i = 0; i < axises.length; i++) {
			// Generate the vector along the axis of movement
			Vector2f dir = axises[i];
			Vector2f deltaAligned = deltaComponents[i];

			// TODO: Calculate steps properly
			int tilesTraversed = (int) Math.ceil(deltaAligned.length() / GameManager.tileSize);
			int cycles = Math.max(tilesTraversed, 1);

			Vector2f deltaInch = null;
			for (int j = 0; j < cycles; j++) {
				// Now inch forwards with increasingly larger deltas
				float cycleCoef = ((float) j + 1) / cycles;
				deltaInch = new Vector2f(deltaAligned.x * cycleCoef, deltaAligned.y * cycleCoef);

				// Get the right position that takes into account past movements
				Vector2f newPos = new Vector2f(rawPos.x + deltaTemp.x, rawPos.y + deltaTemp.y);

				// Debug.enqueueElement(new DebugBox(new Vector2f(newPos).add(deltaInch), new
				// Vector2f(e.dim.x, e.dim.y), 1));
				Debug.enqueueElement(new DebugPolygon(e.hitbox.genWorldVerts(), 1, new Color(1, 1, 1, 1)));

				// Move (this modifies deltaInch)
				boolean isSuccess = Physics.moveTo(newPos, deltaInch, velo, e, grid, dir, axises);
				if (!isSuccess)
					break;
			}

			// Push results to buffer
			deltaTemp.x += deltaInch.x;
			deltaTemp.y += deltaInch.y;
		}

		// Push buffer to delta

		// Push changes
		e.setMoveDelta(deltaTemp);
		e.pData.velo.set(velo);

		if (e.pData.veloChangeQueued) {
			e.resolveVeloChange();
		}
	}

	/**
	 * A utility function to be used by update. Given information, it configures
	 * deltaMove and velocity as to resolve collisions on the given axis. These
	 * values need to be later pushed to the entity in order for changes to appear.
	 * 
	 * IMPORTANT: Returned deltas may not be aligned with move axis.
	 * 
	 * @param relPos
	 * @param delta
	 * @param e
	 * @return Whether or not the entity collided when attempting to move
	 */
	public static boolean moveTo(Vector2f rawPos, Vector2f deltaMove, Vector2f velo, PhysicsEntity e, Tile[][] grid,
			Vector2f moveAxis, Vector2f[] axises) {

		float dirSign = Arithmetic.sign(moveAxis.dot(deltaMove));
		Vector2f moveDir = new Vector2f(moveAxis.x * dirSign, moveAxis.y * dirSign);
		// If movedir is 0, then there is no movement to be done.
		if (moveDir.x == 0 && moveDir.y == 0)
			return true;

		float[] dBuff = new float[1];
		Vector2f normal = new Vector2f();
		boolean isSuccess = !isColliding(rawPos, deltaMove, e, grid, moveDir, dBuff, normal);
		float dist = dBuff[0];

		if (!isSuccess) {
			Vector2f delta = new Vector2f();

			// Move out of tile
			delta.sub(new Vector2f(moveDir).mul(dist));

			// Nudging should only move you as far as you can move.
			float nudge = Math.min(NUDGE_CONSTANT, deltaMove.length());
			delta.sub(new Vector2f(moveDir).mul(nudge));

			// Rotate normal such that it is running along edge
			Vector2f tangent = new Vector2f(-normal.y, normal.x);

			// Execute behaviors
			for (PhysicsCollisionBehavior behavior : e.collBehaviorList) {
				behavior.onColl(rawPos, deltaMove, velo, e, grid, moveAxis, axises, moveDir, tangent, delta);
			}

			/**
			 * Pushing movement
			 */
			deltaMove.add(delta);

			// Enqueue collision response (but store this for later since I want it batched)
			e.pData.collidedWithTile = true;
		}

		return isSuccess;
	}

	public static boolean isColliding(Vector2f rawPos, Vector2f deltaMove, PhysicsEntity e, Tile[][] grid,
			Vector2f moveDir, float[] dBuff, Vector2f nBuff) {
		Vector2f bl = new Vector2f(rawPos.x + deltaMove.x, rawPos.y + deltaMove.y);
		Vector2f ur = new Vector2f(bl.x + e.dim.x, bl.y + e.dim.y);

		// return var
		boolean isColl = false;

		ArrayList<int[]> tilesHit = new ArrayList<>();
		boolean roughPass = roughPass(bl, ur, grid, tilesHit);

		// Rough pass
		float dist = 0;
		Vector2f normal = null;
		if (roughPass) {
			float maxMoveDist = 0;

			for (int[] p : tilesHit) {
				int x = p[X];
				int y = p[Y];

				Tile t = grid[x][y];

				Vector2f tempNormal = new Vector2f(0, 0);

				Float d = getIntersection(t.getHammerState().v, bl, ur, x, y, moveDir, tempNormal);
				if (d != null) {
					if (Math.abs(d) > Math.abs(maxMoveDist)) {
						maxMoveDist = d;
						normal = tempNormal;
					}
					if (Debug.showCollisions) {
						Vector2f pos = new Vector2f(x, y).mul(GameManager.tileSize);
						Debug.highlightRect(pos, new Vector2f(GameManager.tileSize, GameManager.tileSize),
								new Color(1, 0, 1));
					}

					isColl = true;
				}
			}
			dist = maxMoveDist;
		}

		if (dBuff != null)
			dBuff[0] = dist;

		if (normal != null && nBuff != null) {
			nBuff.x = normal.x;
			nBuff.y = normal.y;
		}

		return isColl;
	}

	/**
	 * Gets tiles that being collided with. Note that it presumes every tile is a
	 * unique entity. Remember that entities should never be inside the terrain. If
	 * an entity falls into terrain, it will eject depending on axisDelta. Even if
	 * immobile, axisDelta will default to ejecting to the bottom and left.
	 * 
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

		int boundL = (int) bl.x / GameManager.tileSize;
		int boundR = (int) ur.x / GameManager.tileSize;
		int boundT = (int) ur.y / GameManager.tileSize;
		int boundB = (int) bl.y / GameManager.tileSize;

		int gw = grid.length;
		int gh = grid[0].length;

		// Honestly should just be doing a complete sweep for all tiles it's in, no need
		// to optimize yet.
		for (int y = boundB; y <= boundT; y++) {
			for (int x = boundL; x <= boundR; x++) {
				if (x < 0 || x >= gw)
					break;
				if (y < 0 || y >= gh)
					continue;

				Tile t = grid[x][y];
				if (t != null) {
					int[] o = new int[2];
					o[X] = x;
					o[Y] = y;
					hits.add(o);

					tileHit = true;
				}
			}
		}
		return tileHit;
	}

	public static Float getIntersection(Shape shape, Vector2f bl, Vector2f ur, int tileX, int tileY, Vector2f moveDir,
			Vector2f extNormal) {
		// ---------------- Uses the Separating Axis Theorem ----------------

		// STEP 1: PREP DATA
		// Begin by generating a point set for the Vector2fangle.
		Vector2f[] rectPoints = Geometry.pointsFromCorners(bl, ur);

		// This needs to use world space, not normalized tile space.
		Vector2f[] shapePoints = new Vector2f[shape.vertices.length];
		for (int i = 0; i < shapePoints.length; i++) {
			// Translate and push in points
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
		boolean hasColl = Geometry.separateAxisCheck(rectPoints, shapePoints, moveDir, extNormal, out);

		if (hasColl)
			return out[0];
		else
			return null;
	}

	public static void checkEntityCollision(Hitbox c1, Hitbox c2) {
		// Do separate axis theorem on them
		Vector2f[] c1Points = c1.genWorldVerts();
		Vector2f[] c2Points = c2.genWorldVerts();

		// TODO: Inch this. But this'll need some projection shenanigans. Oh, here we
		// can just use the start and end point sets. SAO will need to be extended to
		// accept that. That optimization will be usable in the player loop too.

		if (Geometry.separateAxisCheck(c1Points, c2Points, null, null, null)) {
			c1.hitBy(c2);
			c2.hitBy(c1);
		}
	}
}
