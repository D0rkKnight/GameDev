package Graphics.particles;

import java.util.ArrayList;

import org.joml.Vector2f;

import Debugging.Debug;
import Debugging.DebugVector;
import Graphics.Elements.Texture;
import Graphics.Rendering.GeneralRenderer;
import Graphics.Rendering.Shader;
import Graphics.Rendering.SpriteShader;
import Graphics.particles.ClothParticle.Constraint;
import Utility.Transformations.ProjectedTransform;
import Wrappers.Color;

public class ClothParticleSystem extends ParticleSystem<ClothParticle> {

	public ArrayList<Constraint> constraints = new ArrayList<Constraint>();

	public ClothParticleSystem(Texture tex, int w, int h) {
		super(ClothParticle.class, tex, w * h);

		// Generate rudimentary array of particles
		Vector2f bl = new Vector2f(100, 200);

		w = 20;
		h = 20;
		float dist = 20;
		float slackCoef = 1f;
		ClothParticle[][] pArr = new ClothParticle[w][h];

		// Instantiate points
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				ClothParticle p = new ClothParticle(this, new Vector2f(bl).add(x * dist, y * dist));
				pArr[x][y] = p;
			}
		}

		// Create links
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				ClothParticle p = pArr[x][y];
				ArrayList<ClothParticle> op = new ArrayList<>();

				// Bidirectional so only 1 set of constraints are created
				if (x < pArr.length - 1) // Rightwards
					op.add(pArr[x + 1][y]);
				if (y < pArr[x].length - 1) // Upwards
					op.add(pArr[x][y + 1]);

				for (ClothParticle oop : op)
					constraints.add(new Constraint(p, oop, dist * slackCoef));
			}
		}

		// Anchor top row
		for (int i = 0; i < pArr.length; i += 4)
			pArr[i][h - 1].setAnchor();
		pArr[w - 1][h - 1].setAnchor();

		// Place particles in array
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				particles[x * w + y] = pArr[x][y];
			}
		}

		// Input particles into arrays

		// Testing
		Vector2f[] verts = new Vector2f[h * w];
		uvs = new Vector2f[h * w];
		int[] indexes = new int[h * w * 6];

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int a = x * w + y;
				verts[a] = pArr[x][y].pos;
				uvs[a] = new Vector2f(((float) x) / w, ((float) y) / h);

				// Input indexes for this square
				// Points are inputted vertically upwards and from the left

				if (x < w - 1 && y < h - 1) { // Squares one less in dimension than point array
					indexes[a * 6] = a;
					indexes[a * 6 + 1] = a + 1;
					indexes[a * 6 + 2] = a + h;
					indexes[a * 6 + 3] = a + 1;
					indexes[a * 6 + 4] = a + h;
					indexes[a * 6 + 5] = a + h + 1;
				}
			}
		}

		rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));
		rend.init(new ProjectedTransform(), verts, uvs, new Color(1, 1, 1, 1));
		rend.setIndexBuffer(indexes);
	}

	public void init() {
		// Does not populate with particles
		// Load in the data
		rend = new GeneralRenderer(Shader.genShader(SpriteShader.class, "texShader"));

		// Or, send in nothing
		rend.init(new ProjectedTransform(), new Vector2f[] {}, new Vector2f[] {}, new Color(1, 1, 1, 1));
		rend.spr = this.tex;
	}

	@Override
	protected void packData() {
		// TODO Auto-generated method stub

	}

	Vector2f[] uvs;

	@Override
	public void render() {
		// TODO Auto-generated method stub
		// Debug render them
		for (Constraint l : constraints)
			Debug.enqueueElement(new DebugVector(l.p1.pos, l.p2.pos, new Color(1, 1, 0, 1), 1));

		Vector2f[] vert = new Vector2f[particles.length];
		for (int i = 0; i < particles.length; i++) {
			vert[i] = particles[i].pos;
		}

		rend.rebuildMesh(vert, uvs, new Color());
		rend.render();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		float iterations = 2;

		// Satisfy constraints
		for (int i = 0; i < iterations; i++) {
			for (Constraint c : constraints) {
				Vector2f v1 = c.p1.pos;
				Vector2f v2 = c.p2.pos;

				Vector2f delta = new Vector2f(v2).sub(v1);
				float dist = delta.length(); // TODO: Use fast sqrt to do this instead

				if (dist > c.d) {

					// Nice solution for anchored particles
					float v1c = 0.5f;
					float v2c = 0.5f;

					if (c.p1.isAnchored()) {
						v1c = 0f;
						v2c *= 2;
					}
					if (c.p2.isAnchored()) {
						v1c *= 2;
						v2c = 0f;
					}

					float diff = (dist - c.d) / dist;
					v1.add(new Vector2f(delta).mul(v1c * diff));
					v2.sub(new Vector2f(delta).mul(v2c * diff));
				}
			}
		}
	}
}
