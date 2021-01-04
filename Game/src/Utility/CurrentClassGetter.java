package Utility;

public class CurrentClassGetter extends SecurityManager {
	public String getClassName() {
		return getClassContext()[1].getName();
	}

	public String getSimpleClassName() {
		return getClassContext()[1].getSimpleName();
	}
}
