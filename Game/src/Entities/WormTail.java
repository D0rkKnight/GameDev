package Entities;

import org.joml.Vector2f;

import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Stats;

public class WormTail extends WormHead{
	protected WormHead forwardsSegment;
	public WormTail(int ID, Vector2f position, Sprites sprites, Renderer renderer, String name, Stats stats, WormTail backSegment, WormHead forwardsSegment) {
		super(ID, position, sprites, renderer, name, stats, backSegment);
		this.forwardsSegment = forwardsSegment;
	}

}
