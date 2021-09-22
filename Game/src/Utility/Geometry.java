package Utility;

import org.joml.Vector2f;

public abstract class Geometry {

	public static Vector2f[] pointsFromCorners(Vector2f bl, Vector2f ur) {
		// Begin by generating a point set for the rectangle.
		Vector2f[] rectPoints = new Vector2f[] { new Vector2f(bl.x, bl.y), new Vector2f(ur.x, bl.y),
				new Vector2f(ur.x, ur.y), new Vector2f(bl.x, ur.y) };

		return rectPoints;
	}

	public static Vector2f[] pointsFromRect(Vector2f bl, Vector2f dims) {
		Vector2f ur = new Vector2f(bl).add(dims);

		return pointsFromCorners(bl, ur);
	}

	public static Vector2f[] pointsFromCircle(Vector2f c, float r, int segs) {
		return pointsFromCircle(c, r, segs, null);
	}

	public static Vector2f[] pointsFromCircle(Vector2f c, float r, int segs, float[] radBuff) {
		Vector2f[] o = new Vector2f[segs];

		for (int i = 0; i < segs; i++) {
			float rad = (float) (i / (float) segs * 2 * Math.PI);
			Vector2f dir = new Vector2f((float) Math.cos(rad), (float) Math.sin(rad));

			Vector2f delta = new Vector2f(dir).mul(r);
			Vector2f p = new Vector2f(c).add(delta);

			o[i] = p;

			if (radBuff != null)
				radBuff[i] = rad;
		}

		return o;
	}

	/**
	 * Given some information, figure out if there is a collision and how far back
	 * to move the entity. TODO: Code this into a more general form
	 * 
	 * @param modPos
	 * @param shape
	 * @param corner
	 * @param sideEnteringFrom
	 * @return
	 */
	public static boolean separateAxisCheck(Vector2f[] pointsA, Vector2f[] pointsB, Vector2f moveDir,
			Vector2f extNormal, float[] dOut) {
		// STEP 2: ANALYZE
		// int edgeCountA = pointsA.length; //Not used
		int edgeCountB = pointsB.length;

		float shortestDist = Float.POSITIVE_INFINITY;
		Vector2f shortestNormal = null;

		boolean distAnalysis = (moveDir != null && extNormal != null && dOut != null);

		// Get normals
		// TODO: Potential error since I'm only counting the shape's normals, and not
		// the Vector2f's normals.
		for (int i = 0; i < edgeCountB; i++) {
			Vector2f p1, p2;

			p1 = pointsB[i];
			p2 = null;
			// If it's the last one, loop to the first
			if (i == pointsB.length - 1) {
				p2 = pointsB[0];
			} else {
				p2 = pointsB[i + 1];
			}

			// Now get the edge
			Vector2f vec = new Vector2f(p2.x - p1.x, p2.y - p1.y);

			// And get the normal
			// Note that the border must be going counterclockwise for the normals to be
			// right.
			// The normal is clockwise of the edge vector.
			Vector2f normal = new Vector2f(vec.y, -vec.x);

			// Project vectors and compare overlaps
			// Start with shape vectors

			// Grab unit vector of the normal for calculation purposes.
			Vector2f unitNormal = new Vector2f(normal).normalize();

			float[] boundsB = new float[2];
			float[] boundsA = new float[2];

			Vector.projectPointSet(pointsB, unitNormal, boundsB);
			Vector.projectPointSet(pointsA, unitNormal, boundsA);

			float[] distBuffer = new float[1];
			if (Arithmetic.isIntersecting(boundsB[0], boundsB[1], boundsA[0], boundsA[1], distBuffer)) {

				/**
				 * Only bother with this if containers supplied
				 */
				if (distAnalysis) {
					// Project along moveAxis
					float dist = distBuffer[0];
					float moveDist = 0;
					if (dist != 0) {
						Vector2f perpVec = new Vector2f(unitNormal.x * dist, unitNormal.y * dist);

						Vector2f projAxis = new Vector2f(moveDir).normalize();

						moveDist = (float) (Math.pow(dist, 2) / perpVec.dot(projAxis));
					}

					else {
						// This is like the same thing as not colliding
						return false;
					}

					float absDist = Math.abs(moveDist);
					if (absDist < shortestDist) {
						shortestDist = absDist;
						shortestNormal = unitNormal;
					}

					// If the distance is the same, this stands to reason that there may be an
					// interfereing parallel surface
					// Thus, if this new edge is more reasonable, use it instead.
					if (absDist == shortestDist && absDist != Float.POSITIVE_INFINITY) {
						if (unitNormal.dot(moveDir) < shortestNormal.dot(moveDir)) {
							shortestNormal = unitNormal;
						}
					}
				}
			} else {
				return false; // Not intersecting, leave.
			}
		}

		if (!distAnalysis)
			return true;

		// Return shortest distance out.
		extNormal.x = shortestNormal.x;
		extNormal.y = shortestNormal.y;

		dOut[0] = shortestDist;
		return true;
	}
}
