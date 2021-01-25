package GameController.procedural;

import org.joml.Vector2i;

public class WorldGate {
	public Vector2i localPos;
	public WorldRoom ownerRoom; // Not set while owned by tetromino

	public static enum GateDir {
		UP, DOWN, LEFT, RIGHT, NONE;

		public GateDir getOpposing() {
			switch (this) {
			case UP:
				return DOWN;
			case DOWN:
				return UP;
			case LEFT:
				return RIGHT;
			case RIGHT:
				return LEFT;
			default:
				System.err.println("What?");
				return null;
			}
		}

		public Vector2i getFaceDelta() {
			switch (this) {
			case UP:
				return new Vector2i(0, -1); // 4th quadrant
			case DOWN:
				return new Vector2i(0, 1);
			case LEFT:
				return new Vector2i(-1, 0);
			case RIGHT:
				return new Vector2i(1, 0);
			default:
				System.err.println("What?");
				return null;
			}
		}
	}

	GateDir dir;
	WorldGate linkedGate;

	public WorldGate(int x, int y, GateDir dir) {
		localPos = new Vector2i(x, y);
		this.dir = dir;
	}

	public WorldGate(WorldGate worldGate, WorldRoom ownerRoom) {
		this(worldGate.localPos.x, worldGate.localPos.y, worldGate.dir);

		this.ownerRoom = ownerRoom;
	}

	public Vector2i getOpposingLoc() {
		return new Vector2i(localPos).add(ownerRoom.pos).add(dir.getFaceDelta());
	}
}
