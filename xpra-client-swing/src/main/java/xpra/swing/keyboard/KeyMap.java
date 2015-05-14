package xpra.swing.keyboard;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * @author Jakub Księżniak
 *
 */
public class KeyMap {

	private static final Map<Integer, String> keycodesMap = new HashMap<>();
	
	static {
		for(int i = KeyEvent.VK_A; i <= KeyEvent.VK_Z; ++i) {
			map(i, i);
		}
		keycodesMap.put(KeyEvent.VK_BACK_SPACE, "BackSpace");
		keycodesMap.put(KeyEvent.VK_ENTER, "Return");
	}
	
	private static void map(int keycode, int c) {
		keycodesMap.put(keycode, String.format("U%s", Integer.toHexString(c).toUpperCase()));
	}
	
	public static String getUnicodeName(int keycode) {
		return keycodesMap.get(keycode);
	}
	
	public static Set<Entry<Integer, String>> getEntries() {
		return keycodesMap.entrySet();
	}
}
