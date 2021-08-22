package Entities.Framework.StateMachine;

import Entities.Framework.Entity;

public interface ECP<T extends Entity> {
	public void invoke(T ent);
}
