package Entities.Framework.StateMachine;

import Entities.Framework.Entity;

public interface ECB<T extends Entity> {
	public void invoke(T ent);
}
