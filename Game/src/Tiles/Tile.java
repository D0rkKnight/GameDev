package Tiles;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Collision.Shapes.Shape.ShapeEnum;
import GameController.Map;
import Graphics.Elements.Texture;
import Graphics.Elements.TileGFX;
import Graphics.Rendering.GeneralRenderer;
import Utility.Transformation;
import Wrappers.Color;

/**
 * Tile
 * 
 * @author Benjamin
 *
 */
public class Tile implements Cloneable {
	protected Map map;
	public GeneralRenderer renderer;
	protected Shape.ShapeEnum hammerState; // NOT IN CONSTRUCTOR BECAUSE ITS NOT SET WITHIN HASHMAP (individual to when
	// loaded in maps)

	public ArrayList<Map.CompEdgeSegment> edgeSegs;
	public ArrayList<TileGFX> tGFX; // Is shared between all tiles clones.

	public Tile(GeneralRenderer renderer, Texture tex, ShapeEnum hs) {
		this.hammerState = hs;
		this.tGFX = new ArrayList<TileGFX>();

		// Create shallow copy
		try {
			this.renderer = renderer.clone();
			this.renderer.spr = tex;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	// Use a clone function instead of this dumbass init function
	public void init(Vector2f pos, Vector2f rect) {
		if (hammerState == null) {
			// System.err.println("Hammer state not specified, capitulating to default.");
			hammerState = Shape.ShapeEnum.SQUARE;
		}

		// will be set later
		edgeSegs = new ArrayList<>();

		this.renderer.init(new Transformation(pos), rect, hammerState, new Color());
	}

	/**
	 * 
	 * @param pos: position at which to render
	 * @param dim: dimensions of tile
	 */
	public void render(Vector2f pos, float dim) {
		renderer.transform.pos = pos;
		renderer.render();
	}

	public ShapeEnum getHammerState() {
		return hammerState;
	}

	public void addGFX(String gfxName) {
		tGFX.add(new TileGFX(gfxName));
	}

	// TODO: Remove this cursed clone function
	@Override
	public Tile clone() throws CloneNotSupportedException {
		Tile t = (Tile) super.clone();
		// New renderer please
		t.renderer = this.renderer.clone();

		return t;
	}
}
