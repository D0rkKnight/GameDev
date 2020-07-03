package Entities;

import Rendering.Renderer;
import Wrappers.Sprites;
import Wrappers.Stats;
import Wrappers.Vector2;

public class WormTail extends WormHead{
	protected WormHead forwardsSegment;
	public WormTail(int ID, Vector2 position, Sprites sprites, Renderer renderer, String name, Stats stats, WormTail backSegment, WormHead forwardsSegment) {
		super(ID, position, sprites, renderer, name, stats, backSegment);
		this.forwardsSegment = forwardsSegment;
	}

}
