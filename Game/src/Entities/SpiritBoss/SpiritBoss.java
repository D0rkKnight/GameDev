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
import Entities.Laser;
import Entities.Framework.Boss;
import Entities.Framework.Entity;
import Entities.Framework.StateMachine.StateID;
import GameController.EntityData;
import GameController.GameManager;
import GameController.Time;
import GameController.World;
import Graphics.Drawer.DBEnum;
import Graphics.Elements.Texture;
import Graphics.Elements.TextureAtlas;
import Graphics.Rendering.BleedShader;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Tiles.Tile;
import Utility.Geometry;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;
import Wrappers.FrameData;
import Wrappers.FrameData.FrameSegment;
import Wrappers.Stats;

public class SpiritBoss extends Boss {

	ArrayList<SpiritFragment> frags = new ArrayList<>();
	int fragCount = 50;
	
	float ringRadius;
	float spin = 0;
	float spinSpeed; // Radians per second
	
	float wobble = 0;
	float wobbleSpeed;
	float wobbleAltitude;
	float wobbleRollDensity;
	
	ArrayList<Float> peaks = new ArrayList<>();
	float peakHeight = 200f;
	float peakNarrowness = 1f;
	
	// Eye (looks at you)
	private SpiritEye eye;
	private float eyeTrackDistance = 50f;
	

	public SpiritBoss(String ID, Vector2f position, String name, Stats stats) {
		super(ID, position, name, stats);

		// Rend
		rendDims = new Vector2f(192, 192);
		TextureAtlas texAtlas = new TextureAtlas(Texture.getTex("Assets/Sprites/spirit_boss.png"), 32, 32);
		
		GeneralRenderer rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), rendDims, Shape.ShapeEnum.SQUARE, 
				new Color(), texAtlas.genSubTex(0, 0, 6, 6));
		rend.spr = texAtlas.tex;

		this.renderer = rend;

		// Hitbox
		dim = new Vector2f(192, 192);
		Hurtbox hurtbox = new Hurtbox(this, new CODVertex(dim.x, dim.y));
		hurtbox.offset.set(-dim.x/2, -dim.y/2);
		addColl(hurtbox);

		pData.hasKnockback = false;

		this.renderer.getOrigin().x = rendDims.x / 2;
		this.renderer.getOrigin().y = rendDims.y / 2;
		offset.x = dim.x / 2;
		offset.y = dim.y / 2;
		
		// Generate spirit fragments
		float[] radBuff = new float[fragCount];
		Vector2f[] fragPos = Geometry.pointsFromCircle(getCenter(), ringRadius, fragCount, radBuff);

		for (int i = 0; i < fragPos.length; i++) {
			SpiritFragment frag = new SpiritFragment(fragPos[i], this);
			GameManager.subscribeEntity(frag);

			frag.localTrans.rot.setRotationXYZ(0, 0, radBuff[i]);

			frags.add(frag);
		}
		
		// Generate Eye
		eye = new SpiritEye("EYE", new Vector2f(), "Spirit Eye", new Stats());
		setAsChild(eye);
		GameManager.subscribeEntity(eye);

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
		
		
		peaks.add(0f);
		peaks.add((float) Math.PI);
		
		setEntityFD(StateID.I);
	}

	@Override
	public void calculate() {
		super.calculate();
		
		if (target == null) findTarget();
		
		spin += spinSpeed * Time.deltaT() / 1000f;
		wobble += wobbleSpeed * Time.deltaT() / 1000f;
		
		// Fragment projection
		for (int i = 0; i < fragCount; i++) {
			float rad = (float) (i / (float) fragCount * 2 * Math.PI) + spin;
			rad %= 2*Math.PI;
			Vector2f dir = new Vector2f((float) Math.cos(rad), (float) Math.sin(rad));

			float nDist = ringRadius + ((float) Math.sin(rad * wobbleRollDensity + wobble) * wobbleAltitude);
			
			// Add peak height
			for (float pk : peaks) {
				float unmodDFP = rad-pk;
				float distFromPeak = Math.abs(unmodDFP);
				
				// Measure going the other way
				if (distFromPeak > Math.PI) distFromPeak = 2*(float)Math.PI - distFromPeak;
				
				nDist += peakHeight / (distFromPeak * peakNarrowness + 1);
			}
			
			Vector2f delta = new Vector2f(dir).mul(nDist);
			Vector2f p = getCenter().add(delta);

			SpiritFragment frag = frags.get(i);
			frag.getPosition().set(p);
			frag.localTrans.rot.setRotationXYZ(0, 0, rad);
		}

		calcPulses((float) (Time.timeSinceStart() / 1000.0));
		
		// Track the eye
		Vector2f ray2Target = new Vector2f(target.getCenter()).sub(getCenter())
				.normalize().mul(eyeTrackDistance);
		eye.getPosition().set(ray2Target);
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
	
	@Override
	protected void genTags() {
		super.genTags();
	}
	
	@Override
	protected void assignFD() {
		super.assignFD();
		
		addFD(StateID.I, genIDLE());
		addFD(StateID.SPIKE, genSPIKE());
		addFD(StateID.LASER, genLASER());
	}
	
	private FrameData genIDLE() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		segs.add(new FrameSegment(100, 0)); // Lasts for a bit
		
		FrameData fd = new FrameData(segs, null, false);
		fd.onEntry = () -> {
			//peaks.clear();
			// Deactivate all fragment hitboxes
			for (SpiritFragment fr : frags) fr.hitbox.isActive = false;
		};
		
		// Lerp to stable orbit
		fd.cb = () -> {
			ringRadius += (100f-ringRadius) * 0.1;
			spinSpeed += (1f-spinSpeed) * 0.1;
			wobbleSpeed += (2f-wobbleSpeed) * 0.1;
			wobbleAltitude += (10f-wobbleAltitude) * 0.1;
			wobbleRollDensity += (7f-wobbleRollDensity) * 0.1;
			peakHeight *= 0.7;
			peakNarrowness += (4f-peakNarrowness) * 0.1;
			
			// Wipe peaks when zeroed
			if (peakHeight <= 1f) peaks.clear();
		};
		
		fd.onEnd = () -> {
			setEntityFD(StateID.LASER);
		};
		
		return fd;
	}
	
	private FrameData genSPIKE() {
		int stopTerm = 70;
		int wait = 20;
		int spikeHold = 30;
		
		ArrayList<FrameSegment> segs = new ArrayList<>();
		ArrayList<FrameData.Event> evs = new ArrayList<>();
		FrameSegment seg1 = new FrameSegment(stopTerm, 0);
		seg1.addCB(() -> {
			spinSpeed *= 0.95f; // lerp towards a stop
			wobbleSpeed *= 0.95f;
			peakHeight += (200f - peakHeight) * 0.1;
			
			// Point a peak at the player
			Vector2f d2p = new Vector2f(target.getPosition()).sub(position);
			
			float rad = new Vector2f(1, 0).angle(d2p);
			if (rad < 0) rad += 2*Math.PI;
			peaks.set(0, rad);
		});
		segs.add(seg1);
		
		FrameData.Event ev1 = new FrameData.Event(() -> {
			spinSpeed = 0;
		}, stopTerm);
		evs.add(ev1);
		
		FrameSegment seg2 = new FrameSegment(wait, stopTerm);
		seg2.addCB(() -> {
			float peakR = peaks.get(0);
			
			// Roll the spin to align with the spike
			float normSpin = (peakR-spin);
			while (normSpin < 0) normSpin += 2*Math.PI;
			normSpin %= (float)(2*Math.PI);
			normSpin += (float)(2*Math.PI/fragCount/2); // Center fragment 
			
			int closestFrag = (int) (fragCount*(normSpin/2/Math.PI)) % fragCount; // Frag range mapped to normalized spin
			
			float fragRad = (float) ((float) closestFrag / fragCount * 2 * Math.PI + spin) % (float) (2*Math.PI);
			float fragDiff = peakR - fragRad; // How ahead the closest fragment the player is

			spin += fragDiff * 0.1; // Align the spin as the natural spin speed approaches zero
			
			SpiritFragment fragEnt = frags.get(closestFrag);
			fragEnt.hitbox.isActive = true;
		});
		
		segs.add(seg2);
		
		FrameSegment seg3 = new FrameSegment(spikeHold, stopTerm + wait);
		seg3.addCB(() -> {
			
			// Spiking effect
			peakHeight += (500f - peakHeight) * 0.5;
			peakNarrowness += (10 - peakNarrowness) * 0.1;
		});
		segs.add(seg3);
		
		FrameData fd = new FrameData(segs, evs, false);
		
		fd.onEntry = () -> {
			spinSpeed = 10f;
			peakHeight = 0f;
			
			peaks.clear();
			peaks.add(0f);
		};
		
		fd.onEnd = () -> {
			for (SpiritFragment fr : frags) fr.localTrans.scale.identity();
			setEntityFD(StateID.I);
		};
		
		return fd;
	}

	private FrameData genLASER() {
		ArrayList<FrameSegment> segs = new ArrayList<>();
		FrameSegment seg1 = new FrameSegment(100, 0);
		
		segs.add(seg1);
		
		FrameData fd = new FrameData(segs, null, false);
		
		fd.onEntry = () -> {
			// Spawn laser
			Vector2f ray2target = new Vector2f(target.getCenter()).sub(getCenter()).normalize();
			
			// Pull grid and march in the given direction
			Tile[][] grid = World.currmap.grids.get("collision");
			
			Vector2f raySearchLoc = new Vector2f(getCenter());
			int mapH = GameManager.tileSize * grid[0].length;
			int mapW = GameManager.tileSize * grid.length;
			
			// Raymarch to find the ending location
			while (true) {
				// Check if in bounds
				if (raySearchLoc.x < 0 || raySearchLoc.x >= mapW) break;
				if (raySearchLoc.y < 0 || raySearchLoc.y >= mapH) break;
				
				// Check if clashing with tile
				int tx = (int) (raySearchLoc.x / GameManager.tileSize);
				int ty = (int) (raySearchLoc.y / GameManager.tileSize);
				
				if (grid[tx][ty] != null) break;
				
				// Advance vector
				Vector2f rayDelta = new Vector2f(ray2target).normalize().mul(GameManager.tileSize);
				raySearchLoc.add(rayDelta);
			}
			
			// Create the laser block
			Vector2f rayDelta = new Vector2f(raySearchLoc).sub(getCenter());
			Laser las = new Laser(new Vector2f(), rayDelta, this, 1200);
			setAsChild(las);
			GameManager.subscribeEntity(las);
		};
		
		fd.onEnd = () -> setEntityFD(StateID.I);
		
		return fd;
	}
}
