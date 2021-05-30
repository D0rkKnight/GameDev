package Entities.PlayerPackage;

import java.util.ArrayList;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import Entities.Framework.Melee;
import GameController.GameManager;
import GameController.Input;
import Wrappers.Color;
import Wrappers.FrameData;
import Wrappers.FrameData.Event;
import Wrappers.FrameData.FrameSegment;
import Wrappers.FrameData.FrameTag;

public class PlayerCombatController {

	public static enum MeleeDir {
		N(0, 1), NE(1, 1), E(1, 0), SE(1, -1), S(0, -1), SW(-1, -1), W(-1, 0), NW(-1, 1);

		public Vector2f v;

		MeleeDir(float x, float y) {
			this(new Vector2f(x, y).normalize());
		}

		MeleeDir(Vector2f dir) {
			this.v = dir;
		}
	}

	public static enum Attack {
		M_U, M_FU, M_FD, M_D, M_A, I, DASH, DECEL;

		public FrameData fd;
	}

	public static void createAttacks() {
		// Create forward attack for now, has 10 frames of windup, 50 frames of hitbox,
		// and 10 frames of windown.
		// Creates a melee attack with 50 frames of life on frame 10.

		genF_A();
		genI();
		genDASH();
		genDECEL();
	}

	private static void genF_A() {
		// NEVERMIND this is just a generic attack command with framedata attached.
		FrameData.Event cma = new FrameData.Event((player) -> {
			melee(player, Input.mouseWorldPos, 30);
		}, 5);

		// Return to idle animation
		FrameData.Event retI = new FrameData.Event((player) -> {
			player.setCurrAtk(Attack.I);
		}, 45);

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(cma);
		evs.add(retI);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(35, 0, FrameTag.INACTABLE));
		segs.add(new FrameSegment(10, 35, FrameTag.DASH_CANCELLABLE));

		Attack.M_A.fd = new FrameData(segs, evs);

		// IDK if this should still be calling controlledMovement :/
		Attack.M_A.fd.cb = (player) -> {
			player.controlledMovement();
		};

		Attack.M_A.fd.onEntry = (player) -> {
			player.baseCol = new Color(0, 1, 0, 1);
		};
	}

	private static void genI() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment idle = new FrameSegment(5, 0, FrameTag.MOVEABLE);

		segs.add(idle);

		Attack.I.fd = new FrameData(segs, new ArrayList<Event>(), true);

		Attack.I.fd.cb = (player) -> {
			player.controlledMovement();
			// TODO: Pass in player by reference instead of using static reference
		};

		Attack.I.fd.onEntry = (player) -> {
			player.baseCol = new Color(0, 0, 0, 1);
		};
	}

	private static void genDASH() {
		FrameData.Event retI = new Event((player) -> {
			player.setCurrAtk(Attack.I);
		}, 10);

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(retI);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment dash = new FrameSegment(10, 0, FrameTag.INACTABLE);

		segs.add(dash);

		Attack.DASH.fd = new FrameData(segs, evs, false);

		Attack.DASH.fd.cb = (player) -> {
			player.dashingMovement();
		};

		Attack.DASH.fd.onEntry = (player) -> {
			player.baseCol = new Color(1, 1, 1, 1);
		};
	}

	private static void genDECEL() {
		FrameSegment main = new FrameSegment(1, 0, FrameTag.INACTABLE);
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(main);

		Attack.DECEL.fd = new FrameData(segs, new ArrayList<>(), true);
		Attack.DECEL.fd.cb = (player) -> {
			player.decelMovement();
		};
		Attack.DECEL.fd.onEntry = (player) -> {
			player.baseCol = new Color(0, 0, 1, 1);
		};
	}

	private static void melee(Player p, Vector2f mousePos, int fLife) {
		int meleedis = 50;// hardcode TODO
		Vector2f kbDir = new Vector2f(p.sideFacing, 0);
		Vector2f pos = new Vector2f(p.getPosition()).add(new Vector2f(8, 32)); // Hardcoded fun
		pos.sub(16, 16); // TODO: Fix hardcode

		// Determine which direction is closest to the current mouse position (lock to
		// cardinal direction)
		Vector2f nmp = new Vector2f(mousePos).sub(pos).normalize();
		System.out.println(nmp);

		MeleeDir dir = MeleeDir.E;
		float minDist = dir.v.distance(nmp);

		for (MeleeDir md : MeleeDir.values()) {
			float compDist = md.v.distance(nmp);
			if (compDist < minDist) {
				minDist = compDist;
				dir = md;
			}
		}

		// Generate data for melee hitbox object
		Vector2f dist = new Vector2f(dir.v).mul(meleedis);
		Vector2f mPos = new Vector2f(p.getPosition()).add(dist);

		// TODO: Check
		long tLife = FrameData.frameToTDelta(fLife);
		System.out.println(tLife);

		Melee meleeEntity = new Melee("MELEE", mPos, "Melee", p, kbDir, tLife);
		GameManager.subscribeEntity(meleeEntity);

		float angle = Math.atan2(dir.v.y, dir.v.x);
		Matrix4f rot = meleeEntity.transform.rot;

		rot.translate(meleeEntity.dim.x / 2, meleeEntity.dim.y / 2, 0);
		rot.rotateZ(angle);
		rot.translate(-meleeEntity.dim.x / 2, -meleeEntity.dim.y / 2, 0);
	}
}
