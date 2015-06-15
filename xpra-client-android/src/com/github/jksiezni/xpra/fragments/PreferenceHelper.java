/**
 * 
 */
package com.github.jksiezni.xpra.fragments;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

/**
 * @author Jakub Księżniak
 *
 */
public class PreferenceHelper {

	/**
	 * Allows to call a {@link OnPreferenceChangeListener}.
	 * 
	 * @param p
	 * @param newValue
	 * @return 
	 */
	public static boolean callChangeListener(Preference p, Object newValue) {
		OnPreferenceChangeListener listener = p.getOnPreferenceChangeListener();
		return listener == null ? true : listener.onPreferenceChange(p, newValue);
	}
}
