package xpra.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Jakub Księżniak
 *
 */
public interface XpraKeyboard {

	Locale getLocale();

	List<KeyDesc> getKeycodes();

	/**
	 * Key descriptor.
	 * 
	 */
	public static class KeyDesc {
		
		int keyval;
		String keyname = "";
		int keycode;
		int group;
		int level;
		
		public KeyDesc(int keycode, String keyname) {
			this.keyval = keycode;
			this.keycode = keycode;
			this.keyname = keyname;
		}

		public List<Object> toList() {
			final List<Object> list = new ArrayList<>(5);
			list.add(keyval);
			list.add(keyname);
			list.add(keycode);
			list.add(group);
			list.add(level);
			return list;
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "( " + keycode + ", " + keyname + ")";
		}
	}
}
