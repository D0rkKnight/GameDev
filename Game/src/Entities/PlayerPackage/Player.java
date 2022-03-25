package Entities.PlayerPackage;

import java.util.ArrayList;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import Entities.Framework.Entity;
import Entities.Framework.Melee;
import Entities.Framework.StateMachine.ECB;
import Entities.Framework.StateMachine.StateID;
import Entities.Framework.StateMachine.StateTag;
import GameController.EntityData;
import GameController.GameManager;
import GameController.Input;
import Utility.Arithmetic;
import Utility.Timers.Timer;
import Utility.Timers.TimerCallback;
import Wrappers.Color;
import Wrappers.FrameData;
import Wrappers.FrameData.Event;
import Wrappers.FrameData.FrameSegment;
import Wrappers.Stats;

public class Player extends PlayerFramework {

	public Player(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

	}

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

	@Override
	protected void assignFD() {
		super.assignFD();

		// Create forward attack for now, has 10 frames of windup, 50 frames of hitbox,
		// and 10 frames of windown.
		// Creates a melee attack with 50 frames of life on frame 10.

		addFD(StateID.M_A, genM_A());
		addFD(StateID.I, genI());
		addFD(StateID.DASH, genDASH());
		addFD(StateID.DECEL, genDECEL());
		addFD(StateID.DASH_ATK, genDASH_ATK());
		addFD(StateID.JAB1, genJAB1());
		addFD(StateID.JAB2, genJAB2());
		addFD(StateID.LUNGE, genLUNGE());
	}

	private static class MutIntWrap {
		int v;

		public MutIntWrap(int v) {
			this.v = v;
		}
	}

	@Override
	protected void genTags() {
		super.genTags();

		addTag(StateTag.DASHABLE, () -> {
			// Initiating dash
			if (Input.dashAction && (Input.moveX != 0 || Input.moveY != 0)) {
				anim.switchAnim(StateTag.DASHING);

				dash();
			}
		});

		addTag(StateTag.JUMPABLE, () -> {
			if (pData.grounded)
				jumpsLeft = maxJumps;
		});

		addTag(StateTag.CAN_MELEE, () -> {
			// Melee
			if (Input.meleeAction) {
				// Check ortho direction
				// TODO: Clean up this boilerplate
				Vector2f pos = new Vector2f(getPosition()).add(dim.x / 2, dim.y / 2);
				Vector2f orthoDir = orthoDirFromVector(new Vector2f(Input.mouseWorldPos).sub(pos));

				if (orthoDir.y == 0 && pData.grounded) {
					setEntityFD(StateID.JAB1);
				} else {
					setEntityFD(StateID.M_A);
				}
			}
		});

		addTag(StateTag.CAN_FIRE, () -> {
			// Shoot a gun
			if (Input.primaryButtonDown) {
				if (gunTimer == null) {
					// Configure firing
					gunTimer = new Timer(100, new TimerCallback() {

						@Override
						public void invoke(Timer timer) {
							fireGun(Input.mouseWorldPos);
						}

					});
				}
				gunTimer.update();
			}
		});

		addTag(StateTag.INVULNERABLE, () -> {
			invulnFrame();
		});
	}

	private FrameData genJAB1() {
		int dur = 15;
		MutIntWrap side = new MutIntWrap(1); // Set in on entry

		ArrayList<Event> evs = new ArrayList<>();

		Event atk = new FrameData.Event(() -> {
			meleeInDir(this, new Vector2f(side.v, 0), 5, 40, new Vector2f(45));
		}, 5);
		evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(dur, 0, getTagCB(StateTag.INACTABLE)));
		segs.add(new FrameSegment(dur, 0, getTagCB(StateTag.INVULNERABLE)));

		FrameSegment cancelable = new FrameSegment(5, 5, () -> {
			if (Input.meleeAction) {
				setEntityFD(StateID.JAB2);
			}
		});
		segs.add(cancelable);

		// Return to idle animation
		FrameData fd = new FrameData(segs, evs);

		// Slowdown during melee
		fd.cb = () -> {
			groundedSlowdown(20);
		};

		// Return to idle state
		fd.onEnd = () -> {
			setEntityFD(StateID.I);
		};

		fd.onEntry = () -> {
			anim.switchAnim(StateTag.JAB1);

			side.v = Arithmetic.sign(new Vector2f(Input.mouseWorldPos).sub(getPosition()).x);
			pData.velo.x = side.v * 2;
		};

		return fd;
	}

	/**
	 * Same thing as jab1, for now
	 * 
	 * @return
	 */
	private FrameData genJAB2() {
		int dur = 15;
		MutIntWrap side = new MutIntWrap(1); // Set in on entry

		ArrayList<Event> evs = new ArrayList<>();

		Event atk = new FrameData.Event(() -> {
			meleeInDir(this, new Vector2f(side.v, 0), 5, 40, new Vector2f(45));
		}, 5);
		evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(dur, 0, getTagCB(StateTag.INACTABLE)));
		segs.add(new FrameSegment(dur, 0, getTagCB(StateTag.INVULNERABLE)));

		// Cancelable into a lunge
		FrameSegment cancelable = new FrameSegment(5, 5, () -> {
			if (Input.meleeAction) {
				setEntityFD(StateID.LUNGE);
			}
		});
		segs.add(cancelable);

		// Return to idle animation
		FrameData fd = new FrameData(segs, evs);

		// Slowdown during melee
		fd.cb = () -> {
			groundedSlowdown(20);
		};

		// Return to idle state
		fd.onEnd = () -> {
			setEntityFD(StateID.I);
		};

		fd.onEntry = () -> {
			anim.switchAnim(StateTag.JAB2);

			side.v = Arithmetic.sign(new Vector2f(Input.mouseWorldPos).sub(getPosition()).x);
			pData.velo.x = side.v * 2;
		};

		return fd;
	}

	/**
	 * Final jab/lunge
	 * 
	 * @return
	 */
	private FrameData genLUNGE() {
		int dur = 15;
		MutIntWrap side = new MutIntWrap(1); // Set in on entry

		ArrayList<Event> evs = new ArrayList<>();

		Event atk = new FrameData.Event(() -> {
			meleeInDir(this, new Vector2f(side.v, 0), 5, 40, new Vector2f(90, 45));
		}, 5);
		evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(dur, 0, getTagCB(StateTag.INACTABLE)));
		segs.add(new FrameSegment(dur, 0, getTagCB(StateTag.INVULNERABLE)));

		// Return to idle animation
		FrameData fd = new FrameData(segs, evs);

		// Slowdown during melee
		fd.cb = () -> {
			groundedSlowdown(20);
		};

		// Return to idle state
		fd.onEnd = () -> {
			setEntityFD(StateID.I);
		};

		fd.onEntry = () -> {
			anim.switchAnim(StateTag.LUNGE);

			side.v = Arithmetic.sign(new Vector2f(Input.mouseWorldPos).sub(getPosition()).x);
			pData.velo.x = side.v * 4;
		};

		return fd;
	}

	/**
	 * Aerial spin attack
	 * 
	 * @return
	 */
	private FrameData genM_A() {
		// Launches multiple hitboxes
		FrameData.Event[] atks = new FrameData.Event[4];
		int start = 5;
		int advance = 7;
		int life = 4;

		for (int i = 0; i < atks.length; i++) {
			atks[i] = new FrameData.Event(() -> {
				meleeInDir(this, new Vector2f(flip.sideFacing, 0), life, 0, new Vector2f(70));
			}, start + advance * i);
		}

		ArrayList<Event> evs = new ArrayList<>();
		for (FrameData.Event atk : atks)
			evs.add(atk);

		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(35, 0, getTagCB(StateTag.INACTABLE)));
		segs.add(new FrameSegment(10, 35, getTagCB(StateTag.DASHABLE)));
		segs.add(new FrameSegment(20, 5, getTagCB(StateTag.INVULNERABLE)));

		FrameData fd = new FrameData(segs, evs);

		// IDK if this should still be calling controlledMovement :/
		fd.cb = () -> {
			controlledMovement();
		};

		fd.onEntry = () -> {
			baseCol = new Color(0, 1, 0, 1);
		};

		// Return to idle state
		fd.onEnd = () -> {
			setEntityFD(StateID.I);
		};

		return fd;
	}

	private FrameData genI() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment idle = new FrameSegment(5, 0, new ECB[] { getTagCB(StateTag.MOVEABLE), getTagCB(StateTag.DASHABLE),
				getTagCB(StateTag.JUMPABLE), getTagCB(StateTag.CAN_FIRE), getTagCB(StateTag.CAN_MELEE) });

		segs.add(idle);

		FrameData fd = new FrameData(segs, new ArrayList<Event>(), true);

		fd.cb = () -> {
			controlledMovement();

			// Animation
			if (Math.abs(pData.velo.x) > 0 && anim.getAnimID().equals(StateTag.IDLE)) {
				anim.switchAnim(StateTag.ACCEL);
			} else if (Math.abs(pData.velo.x) == 0 && anim.getAnimID() != StateTag.IDLE) {
				anim.switchAnim(StateTag.IDLE);
			}

			if (Input.knockbackTest) {
				knockback(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);
			}
		};

		fd.onEntry = () -> {
			baseCol = new Color(0, 0, 0, 1);

			anim.switchAnim(StateTag.MOVING);
		};

		return fd;
	}

	private FrameData genDASH() {
		int dur = 8;

		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment dash = new FrameSegment(dur, 0, getTagCB(StateTag.DASHABLE));
		FrameSegment dashAttack = new FrameSegment(dur, 0, () -> {
			if (Input.meleeAction) {
				setEntityFD(StateID.DASH_ATK);
			}
		});

		segs.add(dash);
		segs.add(dashAttack);

		FrameData fd = new FrameData(segs, null, false);

		fd.onEntry = () -> {
			baseCol = new Color(0.5f, 0.5f, 0.5f, 1);

			// Deactivate hitbox
			mainHurtbox.isActive = false;
		};

		// Applies to forced exits by knockback and etc. too
		fd.onExit = () -> {
			// Update pSys
			pSys.pauseParticleGeneration();
			anim.switchAnim(StateTag.MOVING);

			mainHurtbox.isActive = true;
		};

		fd.onEnd = () -> {
			pData.velo.y *= 0.4; // TODO hardcode for dash deacc
			pData.velo.x *= 0.8;
			decelMode(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);

			setEntityFD(StateID.I);
		};

		return fd;
	}

	/**
	 * Dash attack may slightly extend a dash's distance.
	 * 
	 * @return
	 */
	private FrameData genDASH_ATK() {
		int dur = 10;

		FrameData.Event spawnAtk = new FrameData.Event(() -> {
			Vector2f moveDir = new Vector2f(pData.velo).normalize();
			float dist = 60;
			Vector2f newP = new Vector2f(getPosition()).add(new Vector2f(moveDir).mul(dist));
			newP.add(dim.x / 2, dim.y / 2); // Center on player

			melee(this, newP, moveDir, dur, new Vector2f(90, 45));

		}, 0);

		FrameData.Event retI = new FrameData.Event(() -> {
			// Same decel routine as dash
			pData.velo.y *= 0.4; // TODO hardcode for dash deacc
			pData.velo.x *= 0.8;
			decelMode(new Vector2f(Input.knockbackVectorTest), 0.5f, 1f);

			setEntityFD(StateID.I);
		}, dur);
		ArrayList<FrameData.Event> evs = new ArrayList<>();
		evs.add(spawnAtk);
		evs.add(retI);

		FrameSegment seg1 = new FrameSegment(dur, 0);
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(seg1);

		FrameData fd = new FrameData(segs, evs);

		fd.onEntry = () -> {
			anim.switchAnim(StateTag.DASH_ATK);
		};

		return fd;

	}

	private FrameData genDECEL() {
		FrameSegment main = new FrameSegment(1, 0, getTagCB(StateTag.KNOCKED));
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(main);

		FrameData fd = new FrameData(segs, new ArrayList<>(), true);
		fd.cb = () -> {
			decelMovement();
		};
		fd.onEntry = () -> {
			anim.switchAnim(StateTag.TUMBLE);
			knocked = true;
		};

		fd.onExit = () -> {
			knocked = false;
		};

		return fd;
	}

	private void meleeAtPoint(PlayerFramework p, Vector2f targetPos, int fLife, float meleeDis, Vector2f dims) {
		Vector2f pos = new Vector2f(p.getPosition()).add(p.dim.x / 2, p.dim.y / 2);

		Vector2f dir = orthoDirFromVector(new Vector2f(targetPos).sub(pos));

		// Generate data for melee hitbox object
		Vector2f delta = new Vector2f(dir).mul(meleeDis);

		melee(p, delta, dir, fLife, dims);
	}

	// Hacky solution, please fix
	private void meleeInDir(PlayerFramework p, Vector2f dir, int fLife, float meleeDis, Vector2f dims) {
		// Generate data for melee hitbox object
		Vector2f delta = new Vector2f(dir).mul(meleeDis);

		melee(p, delta, dir, fLife, dims);
	}

	private void melee(PlayerFramework p, Vector2f delta, Vector2f dir, int fLife, Vector2f dims) {
		Vector2f nDir = new Vector2f(dir).normalize();

		// TODO: Check
		long tLife = FrameData.frameToTDelta(fLife);
		
		Vector2f tDelt = new Vector2f(delta).add(meleeOrigin);
		Melee meleeEntity = new Melee("MELEE", tDelt, "Melee", p, nDir, 2, tLife, dims);
		GameManager.subscribeEntity(meleeEntity);

		float angle = Math.atan2(dir.y, dir.x);
		Matrix4f rot = meleeEntity.localTrans.rot;
		
		// Rotate around center
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

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new Player(vals.str("ID"), pos, vals.str("name"), Stats.fromED(vals));
	}
}
