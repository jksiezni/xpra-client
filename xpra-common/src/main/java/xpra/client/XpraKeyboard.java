/*
 * Copyright (C) 2017 Jakub Ksiezniak
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package xpra.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
