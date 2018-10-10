package kilim;

import java.lang.reflect.Field;

public class KilimTools {

	
	private static Boolean INIT = true;
	
	private static boolean isKilimMode = false;
	
	
	public static boolean isKilimMode() {
		if(INIT)init(); // first init if need.
		return isKilimMode;
	}

	private static void init() {
		if(INIT) {
			synchronized (INIT) {
				if(INIT) {
					Field[] fields = KilimTools.class.getDeclaredFields();
					for(Field field : fields) {
						if(field.getName().equals("$isWoven")) {
							isKilimMode = true;
							break;
						}
					}
					//
					INIT = false;
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static void test() throws Pausable { }
}
