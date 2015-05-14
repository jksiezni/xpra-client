package xpra.swing.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import xpra.client.XpraKeyboard;

/**
 * @author Jakub Księżniak
 *
 */
public class SimpleXpraKeyboard implements XpraKeyboard {

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	@Override
	public List<KeyDesc> getKeycodes() {
		ArrayList<KeyDesc> keycodes = new ArrayList<>();
		for(Entry<Integer, String> e : KeyMap.getEntries()) {
			keycodes.add(new KeyDesc(e.getKey(), e.getValue()));
		}
		return keycodes;
	}

}
