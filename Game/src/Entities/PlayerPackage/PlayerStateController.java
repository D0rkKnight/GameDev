package Entities.PlayerPackage;

import java.util.ArrayList;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import Entities.Framework.ECP;
import Entities.Framework.Melee;
import GameController.GameManager;
import GameController.Input;
import Graphics.Animation.Animator;
import Graphics.Animation.Animator.ID;
import Utility.Arithmetic;
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

		public static void assignFD() {
			// Create forward attack for now, has 10 frames of windup, 50 frames of hitbox,
			// and 10 frames of windown.
			// Creates a melee attack with 50 frames of life on frame 10.

			M_A.fd = genM_A();
			I.fd = genI();
			DASH.fd = genDASH();
			DECEL.fd = genDECEL();
			DASH_ATK.fd = genDASH_ATK();
			JAB1.fd = genJAB1();
			JAB2.fd = genJAB2();
			LUNGE.fd = genLUNGE();
		}
	}

	// Containers for callbacks, I guess. Pretty dumb.
	public static enum PlayerTag {
		INACTABLE, DASHABLE, MOVE_CANCELLABLE, MOVEABLE, KNOCKED, JUMPABLE, CAN_MELEE, CAN_FIRE, INVULNERABLE;

		public ECP<Player> cb;
	}

	public static void init() {
		genTags();
		PlayerState.assignFD();
	}

	private static class MutIntWrap {
		int v;

		public MutIntWrap(int v) {
			this.v = v;
		}
	}

	private static void genTags() {
		PlayerTag.DASHABLE.cb = (p) -> {
			// Initiating dash
			if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0)) {
				p.anim.switchAnim(Animator.ID.DASHING);

				p.dash();
			}
		};

		PlayerTag.JUMPABLE.cb = (e) -> {
			if (e.pData.grounded)
				e.canJump = true;
		};

		PlayerTag.CAN_MELEE.cb = (p) -> {
			// Melee
			if (Input.meleeAction) {
				// Check ortho direction
				// TODO: Clean up this boilerplate
				Vector2f pos = new Vector2f(p.getPosition()).add(p.dim.x / 2, p.dim.y / 2);
				Vector2f orthoDir = orthoDirFromVector(new Vector2f(Input.mouseWorldPos).sub(pos));

				if (orthoDir.y == 0 && p.pData.grounded) {
					p.setPlayerState(PlayerState.JAB1);
				} else {
					p.setPlayerState(PlayerState.M_A);
				}
			}
		};

		PlayerTag.CAN_FIRE.cb = (p) -> {
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
		};

		PlayerTag.INVULNERABLE.cb = (p) -> {
			p.invulnFrame();
		};
	}

	private static FrameData genJAB1() {
		int dur = 15;
		MutIntWrap side = new MutIntWrap(1); // Set in on entry

		ArrayList<Event> evs = new ArrayList<>();

		Event atk = new FrameData.Event((ECP<Player>) (p) -> {
			meleeInDir(p, new Vector2f(side.v, 0), 5, 40, new Vector2f(45));
		}, 5);
		evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(dur, 0, PlayerTag.INACTABLE.cb));
		segs.add(new FrameSegment(dur, 0, PlayerTag.INVULNERABLE.cb));

		FrameSegment cancelable = new FrameSegment(5, 5, (ECP<Player>) (p) -> {
			if (Input.meleeAction) {
				p.setPlayerState(PlayerState.JAB2);
			}
		});
		segs.add(cancelable);

		// Return to idle animation
		FrameData fd = new FrameData(segs, evs);

		// Slowdown during melee
		fd.cb = (ECP<Player>) (p) -> {
			p.groundedSlowdown(20);
		};

		// Return to idle state
		fd.onEnd = (ECP<Player>) (p) -> {
			p.setPlayerState(PlayerState.I);
		};

		fd.onEntry = (ECP<Player>) (p) -> {
			p.anim.switchAnim(Animator.ID.JAB1);

			side.v = Arithmetic.sign(new Vector2f(Input.mouseWorldPos).sub(p.getPosition()).x);
			p.pData.velo.x = side.v * 2;
		};

		return fd;
	}

	/**
	 * Same thing as jab1, for now
	 * 
	 * @return
	 */
	private static FrameData genJAB2() {
		int dur = 15;
		MutIntWrap side = new MutIntWrap(1); // Set in on entry

		ArrayList<Event> evs = new ArrayList<>();

		Event atk = new FrameData.Event((ECP<Player>) (p) -> {
			meleeInDir(p, new Vector2f(side.v, 0), 5, 40, new Vector2f(45));
		}, 5);
		evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(dur, 0, PlayerTag.INACTABLE.cb));
		segs.add(new FrameSegment(dur, 0, PlayerTag.INVULNERABLE.cb));

		// Cancelable into a lunge
		FrameSegment cancelable = new FrameSegment(5, 5, (ECP<Player>) (p) -> {
			if (Input.meleeAction) {
				p.setPlayerState(PlayerState.LUNGE);
			}
		});
		segs.add(cancelable);

		// Return to idle animation
		FrameData fd = new FrameData(segs, evs);

		// Slowdown during melee
		fd.cb = (ECP<Player>) (p) -> {
			p.groundedSlowdown(20);
		};

		// Return to idle state
		fd.onEnd = (ECP<Player>) (p) -> {
			p.setPlayerState(PlayerState.I);
		};

		fd.onEntry = (ECP<Player>) (p) -> {
			p.anim.switchAnim(Animator.ID.JAB2);

			side.v = Arithmetic.sign(new Vector2f(Input.mouseWorldPos).sub(p.getPosition()).x);
			p.pData.velo.x = side.v * 2;
		};

		return fd;
	}

	/**
	 * Final jab/lunge
	 * 
	 * @return
	 */
	private static FrameData genLUNGE() {
		int dur = 15;
		MutIntWrap side = new MutIntWrap(1); // Set in on entry

		ArrayList<Event> evs = new ArrayList<>();

		Event atk = new FrameData.Event((ECP<Player>) (p) -> {
			meleeInDir(p, new Vector2f(side.v, 0), 5, 40, new Vector2f(90, 45));
		}, 5);
		evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(dur, 0, PlayerTag.INACTABLE.cb));
		segs.add(new FrameSegment(dur, 0, PlayerTag.INVULNERABLE.cb));

		// Return to idle animation
		FrameData fd = new FrameData(segs, evs);

		// Slowdown during melee
		fd.cb = (ECP<Player>) (p) -> {
			p.groundedSlowdown(20);
		};

		// Return to idle state
		fd.onEnd = (ECP<Player>) (p) -> {
			p.setPlayerState(PlayerState.I);
		};

		fd.onEntry = (ECP<Player>) (p) -> {
			p.anim.switchAnim(Animator.ID.LUNGE);

			side.v = Arithmetic.sign(new Vector2f(Input.mouseWorldPos).sub(p.getPosition()).x);
			p.pData.velo.x = side.v * 4;
		};

		return fd;
	}

	/**
	 * Aerial spin attack
	 * 
	 * @return
	 */
	private static FrameData genM_A() {
		// Launches multiple hitboxes
		FrameData.Event[] atks = new FrameData.Event[4];
		int start = 5;
		int advance = 7;
		int life = 4;

		for (int i = 0; i < atks.length; i++) {
			atks[i] = new FrameData.Event((ECP<Player>) (p) -> {
				meleeInDir(p, new Vector2f(p.sideFacing, 0), life, 0, new Vector2f(70));
			}, start + advance * i);
		}

		ArrayList<Event> evs = new ArrayList<>();
		for (FrameData.Event atk : atks)
			evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(35, 0, PlayerTag.INACTABLE.cb));
		segs.add(new FrameSegment(10, 35, PlayerTag.DASHABLE.cb));
		segs.add(new FrameSegment(20, 5, PlayerTag.INVULNERABLE.cb));

		FrameData fd = new FrameData(segs, evs);

		// IDK if this should still be calling controlledMovement :/
		fd.cb = (ECP<Player>) (p) -> {
			p.controlledMovement();
		};

		fd.onEntry = (ECP<Player>) (p) -> {
			p.baseCol = new Color(0, 1, 0, 1);
		};

		// Return to idle state
		fd.onEnd = (ECP<Player>) (p) -> {
			p.setPlayerState(PlayerState.I);
		};

		return fd;
	}

	private static FrameData genI() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment idle = new FrameSegment(5, 0, new ECP[] { PlayerTag.MOVEABLE.cb, PlayerTag.DASHABLE.cb,
				PlayerTag.JUMPABLE.cb, PlayerTag.CAN_FIRE.cb, PlayerTag.CAN_MELEE.cb });

		segs.add(idle);

		FrameData fd = new FrameData(segs, new ArrayList<Event>(), true);

		fd.cb = (ECP<Player>) (p) -> {
			p.controlledMovement();

			// Animation
			if (Math.abs(p.pData.velo.x) > 0 && p.anim.getAnimID().equals(Animator.ID.IDLE)) {
				p.anim.switchAnim(ID.ACCEL);
			} else if (Math.abs(p.pData.velo.x) == 0 && p.anim.getAnimID() != Animator.ID.IDLE) {
				p.anim.switchAnim(Animator.ID.IDLE);
			}

			if (Input.knockbackTest) {
				p.knockback(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
			}
		};

		fd.onEntry = (ECP<Player>) (p) -> {
			p.baseCol = new Color(0, 0, 0, 1);

			p.anim.switchAnim(Animator.ID.MOVING);
		};

		return fd;
	}

	private static FrameData genDASH() {
		FrameData.Event retI = new Event((ECP<Player>) (p) -> {
			p.pData.velo.y *= 0.4; // TODO hardcode for dash deacc
			p.pData.velo.x *= 0.8;
			p.decelMode(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);

			p.setPlayerState(PlayerState.I);
		}, 10);

		ArrayList<Event> evs = new ArrayList<>();
		evs.add(retI);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment dash = new FrameSegment(10, 0, PlayerTag.DASHABLE.cb);
		FrameSegment dashAttack = new FrameSegment(10, 0, (ECP<Player>) (p) -> {
			if (Input.meleeAction) {
				p.setPlayerState(PlayerState.DASH_ATK);
			}
		});

		segs.add(dash);
		segs.add(dashAttack);

		FrameData fd = new FrameData(segs, evs, false);

		fd.onEntry = (ECP<Player>) (p) -> {
			p.baseCol = new Color(0.5f, 0.5f, 0.5f, 1);
		};

		// Applies to forced exits by knockback and etc. too
		fd.onExit = (ECP<Player>) (p) -> {
			// Update pSys
			p.pSys.pauseParticleGeneration();
			p.anim.switchAnim(Animator.ID.MOVING);
		};

		return fd;
	}

	/**
	 * Dash attack may slightly extend a dash's distance.
	 * 
	 * @return
	 */
	private static FrameData genDASH_ATK() {
		int dur = 10;

		FrameData.Event spawnAtk = new FrameData.Event((ECP<Player>) (p) -> {
			Vector2f moveDir = new Vector2f(p.pData.velo).normalize();
			float dist = 60;
			Vector2f newP = new Vector2f(p.getPosition()).add(new Vector2f(moveDir).mul(dist));
			newP.add(p.dim.x / 2, p.dim.y / 2); // Center on player

			melee(p, newP, moveDir, dur, new Vector2f(90, 45));

		}, 0);

		FrameData.Event retI = new FrameData.Event((ECP<Player>) (p) -> {
			// Same decel routine as dash
			p.pData.velo.y *= 0.4; // TODO hardcode for dash deacc
			p.pData.velo.x *= 0.8;
			p.decelMode(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);

			p.setPlayerState(PlayerState.I);
		}, dur);
		ArrayList<FrameData.Event> evs = new ArrayList<>();
		evs.add(spawnAtk);
		evs.add(retI);

		FrameSegment seg1 = new FrameSegment(dur, 0);
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(seg1);

		FrameData fd = new FrameData(segs, evs);

		fd.onEntry = (ECP<Player>) (p) -> {
			p.anim.switchAnim(Animator.ID.DASH_ATK);
		};

		return fd;

	}

	private static FrameData genDECEL() {
		FrameSegment main = new FrameSegment(1, 0, PlayerTag.KNOCKED.cb);
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(main);

		FrameData fd = new FrameData(segs, new ArrayList<>(), true);
		fd.cb = (ECP<Player>) (p) -> {
			p.decelMovement();
		};
		fd.onEntry = (ECP<Player>) (p) -> {
			p.anim.switchAnim(Animator.ID.TUMBLE);
			p.knocked = true;
		};

		fd.onExit = (ECP<Player>) (p) -> {
			p.knocked = false;
		};

		return fd;
	}

	private static void meleeAtPoint(Player p, Vector2f targetPos, int fLife, float meleeDis, Vector2f dims) {
		Vector2f pos = new Vector2f(p.getPosition()).add(p.dim.x / 2, p.dim.y / 2);

		Vector2f dir = orthoDirFromVector(new Vector2f(targetPos).sub(pos));

		// Generate data for melee hitbox object
		Vector2f dist = new Vector2f(dir).mul(meleeDis);
		Vector2f mPos = new Vector2f(pos).add(dist);

		melee(p, mPos, dir, fLife, dims);
	}

	// Hacky solution, please fix
	private static void meleeInDir(Player p, Vector2f dir, int fLife, float meleeDis, Vector2f dims) {
		Vector2f pos = new Vector2f(p.getPosition()).add(p.dim.x / 2, p.dim.y / 2);

		// Generate data for melee hitbox object
		Vector2f dist = new Vector2f(dir).mul(meleeDis);
		Vector2f mPos = new Vector2f(pos).add(dist);

		melee(p, mPos, dir, fLife, dims);
	}

	private static void melee(Player p, Vector2f pos, Vector2f dir, int fLife, Vector2f dims) {
		Vector2f nDir = new Vector2f(dir).normalize();

		// TODO: Check
		long tLife = FrameData.frameToTDelta(fLife);

		Melee meleeEntity = new Melee("MELEE", pos, "Melee", p, nDir, tLife, dims);
		GameManager.subscribeEntity(meleeEntity);

		float angle = Math.atan2(dir.y, dir.x);
		Matrix4f rot = meleeEntity.localTrans.rot;

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
}
