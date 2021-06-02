package Entities.PlayerPackage;

import java.util.ArrayList;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import Entities.Framework.Melee;
import GameController.GameManager;
import GameController.Input;
import Graphics.Animation.PlayerAnimator;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;
import Wrappers.FrameData;
import Wrappers.FrameData.Event;
import Wrappers.FrameData.FrameSegment;

public class PlayerStateController {

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

	public static enum PlayerState {
		//@formatter:off
		I, DASH, DECEL, 
		M_A, JAB1, JAB2, LUNGE, DASH_ATK;
		//@formatter:on

		public FrameData fd;
	}

	// Containers for callbacks, I guess. Pretty dumb.
	public static enum PlayerTag {
		INACTABLE, DASHABLE, MOVE_CANCELLABLE, MOVEABLE, KNOCKED, JUMPABLE, CAN_MELEE, CAN_FIRE;

		public EntityCB cb;
	}

	public static void init() {
		genTags();
		genStates();
	}

	private static void genTags() {
		PlayerTag.DASHABLE.cb = wrapPCB((p) -> {
			// Initiating dash
			if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0)) {
				p.dash();
			}
		});

		PlayerTag.JUMPABLE.cb = wrapPCB((e) -> {
			if (e.pData.grounded)
				e.canJump = true;
		});

		PlayerTag.CAN_MELEE.cb = wrapPCB((p) -> {
			// Melee
			// TODO: Have combat controllers handle this, since it's like an attack cancel.
			if (Input.meleeAction) {
				p.setPlayerState(PlayerState.M_A);
			}
		});

		PlayerTag.CAN_FIRE.cb = wrapPCB((p) -> {
			// Shoot a gun
			if (Input.primaryButtonDown) {
				if (p.gunTimer == null) {
					// Configure firing
					p.gunTimer = new Timer(100, new TimerCallback() {

						@Override
						public void invoke(Timer timer) {
							p.fireGun(Input.mouseWorldPos);
						}

					});
				}
				p.gunTimer.update();
			}
		});
	}

	private static void genStates() {
		// Create forward attack for now, has 10 frames of windup, 50 frames of hitbox,
		// and 10 frames of windown.
		// Creates a melee attack with 50 frames of life on frame 10.

		PlayerState.M_A.fd = genM_A();
		PlayerState.I.fd = genI();
		PlayerState.DASH.fd = genDASH();
		PlayerState.DECEL.fd = genDECEL();
		PlayerState.DASH_ATK.fd = genDASH_ATK();
	}

	private static FrameData genM_A() {
		// NEVERMIND this is just a generic attack command with framedata attached.
		FrameData.Event cma = new FrameData.Event(wrapPCB((player) -> {
			melee(player, Input.mouseWorldPos, 30);
		}), 5);

		// Return to idle animation
		FrameData.Event retI = new FrameData.Event(wrapPCB((player) -> {
			player.setPlayerState(PlayerState.I);
		}), 45);

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(cma);
		evs.add(retI);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(35, 0, PlayerTag.INACTABLE.cb));
		segs.add(new FrameSegment(10, 35, PlayerTag.DASHABLE.cb));

		FrameData fd = new FrameData(segs, evs);

		// IDK if this should still be calling controlledMovement :/
		fd.cb = wrapPCB((p) -> {
			p.controlledMovement();
		});

		fd.onEntry = wrapPCB((e) -> {
			e.baseCol = new Color(0, 1, 0, 1);
		});

		return fd;
	}

	private static FrameData genI() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment idle = new FrameSegment(5, 0, new EntityCB[] { PlayerTag.MOVEABLE.cb, PlayerTag.DASHABLE.cb,
				PlayerTag.JUMPABLE.cb, PlayerTag.CAN_FIRE.cb, PlayerTag.CAN_MELEE.cb });

		segs.add(idle);

		FrameData fd = new FrameData(segs, new ArrayList<Event>(), true);

		fd.cb = wrapPCB((p) -> {
			p.controlledMovement();

			if (Input.knockbackTest) {
				p.knockback(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
			}
		});

		fd.onEntry = wrapPCB((p) -> {
			p.baseCol = new Color(0, 0, 0, 1);
		});

		return fd;
	}

	private static FrameData genDASH() {
		FrameData.Event retI = new Event(wrapPCB((p) -> {
			p.pData.velo.y *= 0.4; // TODO hardcode for dash deacc
			p.pData.velo.x *= 0.8;
			p.decelMode(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);

			p.setPlayerState(PlayerState.I);
		}), 10);

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(retI);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment dash = new FrameSegment(10, 0, PlayerTag.DASHABLE.cb);
		FrameSegment dashAttack = new FrameSegment(10, 0, wrapPCB((p) -> {
			if (Input.meleeAction) {
				p.setPlayerState(PlayerState.DASH_ATK);
			}
		}));

		segs.add(dash);
		segs.add(dashAttack);

		FrameData fd = new FrameData(segs, evs, false);

		fd.cb = wrapPCB((p) -> {
			p.dashingMovement();
		});

		fd.onEntry = wrapPCB((p) -> {
			p.baseCol = new Color(0.5f, 0.5f, 0.5f, 1);
		});

		// Applies to forced exits by knockback and etc. too
		fd.onExit = wrapPCB((p) -> {
			// Update pSys
			p.pSys.pauseParticleGeneration();
			p.anim.switchAnim(PlayerAnimator.ANIM_MOVING);
		});

		return fd;
	}

	private static FrameData genDASH_ATK() {
		int dur = 5;

		FrameData.Event spawnAtk = new FrameData.Event(wrapPCB((p) -> {
			System.out.println("Spawning attack!");
			Melee mEnt = new Melee("MELEE", new Vector2f(p.getPosition()), "Melee", p,
					new Vector2f(p.pData.velo).normalize(), FrameData.frameToTDelta(dur));
			mEnt.transform.scale.scale(3);

			GameManager.subscribeEntity(mEnt);

			System.out.println(mEnt);

		}), 0);

		FrameData.Event retI = new FrameData.Event(wrapPCB((p) -> {
			p.setPlayerState(PlayerState.I);
		}), dur);
		ArrayList<FrameData.Event> evs = new ArrayList<>();
		evs.add(spawnAtk);
		evs.add(retI);

		FrameSegment seg1 = new FrameSegment(dur, 0);
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(seg1);

		FrameData fd = new FrameData(segs, evs);
		return fd;

	}

	private static FrameData genDECEL() {
		FrameSegment main = new FrameSegment(1, 0, PlayerTag.KNOCKED.cb);
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(main);

		FrameData fd = new FrameData(segs, new ArrayList<>(), true);
		fd.cb = wrapPCB((p) -> {
			p.decelMovement();
		});
		fd.onEntry = wrapPCB((p) -> {
			p.baseCol = new Color(0, 0, 1, 1);
			p.knocked = true;
		});

		fd.onExit = wrapPCB((p) -> {
			p.knocked = false;
		});

		return fd;
	}

	private static void melee(Player p, Vector2f mousePos, int fLife) {
		int meleedis = 50;// hardcode TODO
		Vector2f kbDir = new Vector2f(p.sideFacing, 0);
		Vector2f pos = new Vector2f(p.getPosition()).add(new Vector2f(8, 32)); // Hardcoded fun
		pos.sub(16, 16); // TODO: Fix hardcode

		Vector2f dir = orthoDirFromVector(new Vector2f(mousePos).sub(pos));

		// Generate data for melee hitbox object
		Vector2f dist = new Vector2f(dir).mul(meleedis);
		Vector2f mPos = new Vector2f(p.getPosition()).add(dist);

		// TODO: Check
		long tLife = FrameData.frameToTDelta(fLife);

		Melee meleeEntity = new Melee("MELEE", mPos, "Melee", p, kbDir, tLife);
		GameManager.subscribeEntity(meleeEntity);

		float angle = Math.atan2(dir.y, dir.x);
		Matrix4f rot = meleeEntity.transform.rot;

		rot.translate(meleeEntity.dim.x / 2, meleeEntity.dim.y / 2, 0);
		rot.rotateZ(angle);
		rot.translate(-meleeEntity.dim.x / 2, -meleeEntity.dim.y / 2, 0);
	}

	private static Vector2f orthoDirFromVector(Vector2f v) {
		// Determine which direction is closest to the current mouse position (lock to
		// cardinal direction)
		Vector2f nmp = v.normalize();

		MeleeDir dir = MeleeDir.E;
		float minDist = dir.v.distance(nmp);

		for (MeleeDir md : MeleeDir.values()) {
			float compDist = md.v.distance(nmp);
			if (compDist < minDist) {
				minDist = compDist;
				dir = md;
			}
		}

		return dir.v;
	}

	private static EntityCB wrapPCB(PlayerCB pcb) {
		return (e) -> {
			pcb.invoke((Player) e);
		};
	}
}
