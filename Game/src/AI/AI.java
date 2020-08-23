package AI;

import Entities.*;

public abstract class AI {
	public Entity entity;
	public AI(Entity entity) {
		if(!(entity instanceof Dynamic)) {
			System.out.println("ERROR, NONDYNAMIC ENTITY INITIALIZING AI");
			System.exit(0);
		}
		this.entity = entity;
	}
	public abstract void move();
}
