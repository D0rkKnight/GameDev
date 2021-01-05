package Tiles;

import java.util.ArrayList;

import org.joml.Vector2f;

import Collision.Shapes.Shape;
import Collision.Shapes.Shape.ShapeEnum;
import GameController.Map;
import Graphics.Elements.SubTexture;
import Graphics.Elements.TileGFX;

/**
 * Tile
 * 
 * @author Benjamin
 *
 */
public class Tile {
	protected Map map;
	public Shape.ShapeEnum shape; // NOT IN CONSTRUCTOR BECAUSE ITS NOT SET WITHIN HASHMAP (individual to when
	// loaded in maps)
	public SubTexture subTex;

	public ArrayList<Map.CompEdgeSegment> edgeSegs;
	public TileGFX tGFX; // Is shared between all tiles clones.

	public Tile(ShapeEnum hs, SubTexture subTex, TileGFX tGFX, Vector2f pos, Vector2f rect) {
		this.shape = hs;
		this.tGFX = tGFX;
		this.subTex = subTex;

		// Enqueue data
		if (shape == null) {
			// System.err.println("Hammer state not specified, capitulating to default.");
			shape = Shape.ShapeEnum.SQUARE;
		}

		// will be set later
		edgeSegs = new ArrayList<>();
	}

	public Tile(Tile t, Vector2f pos, Vector2f rect) {
		this(t.shape, t.subTex, t.tGFX, pos, rect);
	}

	public ShapeEnum getHammerState() {
		return shape;
	}
}
