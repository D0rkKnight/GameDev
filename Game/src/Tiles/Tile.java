package Tiles;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Collision.Shapes.Shape.ShapeEnum;
import GameController.Map;
import Graphics.Elements.SubTexture;
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
	protected Shape.ShapeEnum shape; // NOT IN CONSTRUCTOR BECAUSE ITS NOT SET WITHIN HASHMAP (individual to when
	// loaded in maps)
	protected SubTexture subTex;

	public ArrayList<Map.CompEdgeSegment> edgeSegs;
	public ArrayList<TileGFX> tGFX; // Is shared between all tiles clones.

	public Tile(GeneralRenderer renderer, ShapeEnum hs, SubTexture subTex) {
		this.shape = hs;
		this.tGFX = new ArrayList<TileGFX>();
		this.subTex = subTex;

		// Create shallow copy
		try {
			this.renderer = renderer.clone();
			this.renderer.spr = subTex.tex;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	// TODO: Use some kind of special constructor instead of this
	public void init(Vector2f pos, Vector2f rect) {
		if (shape == null) {
			// System.err.println("Hammer state not specified, capitulating to default.");
			shape = Shape.ShapeEnum.SQUARE;
		}

		// will be set later
		edgeSegs = new ArrayList<>();

		this.renderer.init(new Transformation(pos), rect, shape, new Color(), subTex);
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
		return shape;
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
