package Entities.SpiritBoss;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Collider;
import Collision.Collider.CODCircle;
import Collision.Collider.CODVertex;
import Collision.Hurtbox;
import Collision.Shapes.Shape;
import Debugging.Debug;
import Debugging.DebugPolygon;
import Entities.Framework.Boss;
import Entities.Framework.Entity;
import Entities.Framework.StateMachine.StateID;
import GameController.EntityData;
import GameController.GameManager;
import GameController.Time;
import Graphics.Drawer.DBEnum;
import Graphics.Rendering.BleedShader;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Utility.Geometry;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.Stats;

public class SpiritBoss extends Boss {

	ArrayList<SpiritFragment> frags = new ArrayList<>();
	int fragCount = 24;
	float dist = 100f;

	public SpiritBoss(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Rend
		rendDims = new Vector2f(192, 192);
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), rendDims, Shape.ShapeEnum.SQUARE, new Color());
		rend.spr = Debug.debugTex;

		this.renderer = rend;

		// Hitbox
		dim = new Vector2f(192, 192);
		Hurtbox hurtbox = new Hurtbox(this, new CODVertex(dim.x, dim.y));
		hurtbox.offset.set(-dim.x/2, 0);
		addColl(hurtbox);

		pData.hasKnockback = false;

		this.renderer.getOrigin().x = rendDims.x / 2;
		origin.x = dim.x / 2;

		setEntityFD(StateID.MOVE);

		// Generate spirit fragments
		float[] radBuff = new float[fragCount];
		Vector2f[] fragPos = Geometry.pointsFromCircle(getCenter(), dist, fragCount, radBuff);

		for (int i = 0; i < fragPos.length; i++) {
			SpiritFragment frag = new SpiritFragment(fragPos[i]);
			GameManager.subscribeEntity(frag);

			frag.localTrans.rot.setRotationXYZ(0, 0, radBuff[i]);

			frags.add(frag);
		}

		// Enqueue pulses
		calcPulses(0f);

		// Testing
		Vector2f[] verts = Geometry.pointsFromCircle(getCenter(), 200f, 20);
		Debug.enqueueElement(new DebugPolygon(verts, 1000, Color.WHITE));

		// More testing
		// TODO: Terrible process, please refine
		float r = 500;
		SpiritPulse pulse = new SpiritPulse("PULSE", new Vector2f(getCenter()), "Test pulse", r);
		Collider<CODCircle> coll = pulse.getColl().get(0);
		Vector2f[] pulseVerts = coll.getCOD().getData().genVerts(10);
		// Debug.enqueueElement(new DebugPolygon(pulseVerts, 1000, Color.WHITE));

		GameManager.subscribeEntity(pulse);
	}

	@Override
	public void calculate() {
		for (int i = 0; i < fragCount; i++) {
			float rad = (float) (i / (float) fragCount * 2 * Math.PI) + (Time.timeSinceStart() / 3000f);
			Vector2f dir = new Vector2f((float) Math.cos(rad), (float) Math.sin(rad));

			float nDist = dist + ((float) Math.sin(rad * 4 + (Time.timeSinceStart() / 1000f)) * 10);
			Vector2f delta = new Vector2f(dir).mul(nDist);
			Vector2f p = getCenter().add(delta);

			SpiritFragment frag = frags.get(i);
			frag.getPosition().set(p);
			frag.localTrans.rot.setRotationXYZ(0, 0, rad);
		}

		calcPulses((float) (Time.timeSinceStart() / 1000.0));
	}

	public static Entity createNew(EntityData vals, Vector2f pos, Vector2f dims) {
		return new SpiritBoss(vals.str("type"), pos, vals.str("name"), Stats.fromED(vals));
	}

	@Override
	public void render() {
		super.render();

		// Testing stuff
		BleedShader bleed = (BleedShader) DBEnum.BLEED.buff.rend.shader;
		bleed.trans.setTrans(getCenter());
	}

	private void calcPulses(float tSec) {
		// Enqueue pulses
		// TODO: figure out how to synchronize with hitboxes properly
		BleedShader bleed = (BleedShader) DBEnum.BLEED.buff.rend.shader;
		for (int i = 0; i < 5; i++) {
			float r = (i - 4) * 1f + tSec * 0.5f;
			bleed.pulses[i] = r;
		}

//		// Testing (makes first ring track player
//		Vector2f pPos = GameManager.player.getCenter();
//		Matrix4f mvp = new ProjectedTransform(new Vector2f()).genMVP();
//		Vector4f spPos = new Vector4f(pPos.x, pPos.y, 0f, 1f).mul(mvp);
//		Vector4f cPos = new Vector4f(getCenter().x, getCenter().y, 0f, 1f).mul(mvp);
//
//		// Correct for screen dims to put everything in width units
//		Vector2f vp = Camera.main.viewport;
//		spPos.y *= vp.y / vp.x;
//		cPos.y *= vp.y / vp.x;
//		float dist = spPos.distance(cPos);
//		bleed.pulses[0] = dist;
	}

}
