package GameController;

import java.util.HashMap;

public class EntityData {
	public HashMap<String, Object> d;

	public EntityData() {
		d = new HashMap<String, Object>();
	}

	@SuppressWarnings("unchecked")
	public EntityData(EntityData properties) {
		d = (HashMap<String, Object>) properties.d.clone();
	}

	public String str(String key) {
		return (String) d.get(key);
	}

	public int in(String key) {
		return (int) d.get(key);
	}

	public float fl(String key) {
		return (float) d.get(key);
	}

	public boolean bl(String key) {
		return (boolean) d.get(key);
	}

	public Object obj(String key) {
		return d.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T t(String key) {
		return (T) d.get(key);
	}
}
