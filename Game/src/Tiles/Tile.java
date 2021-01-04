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
	public Shape.ShapeEnum shape; // NOT IN CONSTRUCTOR BECAUSE ITS NOT SET WITHIN HASHMAP (individual to when
	// loaded in maps)
	public SubTexture subTex;

	public ArrayList<Map.CompEdgeSegment> edgeSegs;

	// TODO: Why is this an array? Only 1 shader's effects is going to appear
	// anyways
	public ArrayList<TileGFX> tGFX; // Is shared between all tiles clones.

	public Tile(GeneralRenderer renderer, ShapeEnum hs, SubTexture subTex, Vector2f pos, Vector2f rect) {
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

		// Enqueue data
		if (shape == null) {
			// System.err.println("Hammer state not specified, capitulating to default.");
			shape = Shape.ShapeEnum.SQUARE;
		}

		// will be set later
		edgeSegs = new ArrayList<>();

		this.renderer.init(new Transformation(pos), rect, shape, new Color(), subTex);
	}

	public Tile(Tile t, Vector2f pos, Vector2f rect) {
		this(t.renderer, t.shape, t.subTex, pos, rect);
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
}
