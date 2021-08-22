package Entities.Framework;

public interface ECP<T extends Entity> {
	public void invoke(T ent);
}
