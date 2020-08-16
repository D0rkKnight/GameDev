package Wrappers;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.PhysicsCollisionBehavior;

public class PhysicsData {
	public boolean grounded;
	public boolean wasGrounded;//something something about tampering
	public boolean isJumping;
	public boolean collidedWithTile;
	public Vector2f xDir;
	public Vector2f yDir;
	public Vector2f newXDir;
	public Vector2f newYDir;
	public Vector2f velo;
	public boolean veloChangeQueued;
	public float height;
	public float width;
	public boolean canBeGrounded;
	public Vector2f queuedTangent;
	public float yAcceleration;
	public Vector2f moveDelta;
	public ArrayList<PhysicsCollisionBehavior> groundedCollBehaviorList;
	public ArrayList<PhysicsCollisionBehavior> nonGroundedCollBehaviorList;
	
}

