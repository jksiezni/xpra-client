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

package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class HelloResponse extends xpra.protocol.Packet {
	
	private Map<String, Object> capabilities;

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		capabilities = (Map<String, Object>) iter.next();
	}

	public Map<String, Object> getCaps() {
		return capabilities;
	}

	public boolean isRencode() {
		return asBoolean(capabilities.get("rencode"));
	}

	public List<String> getStringArray(String key) {
		Object values = capabilities.get(key);
		List<String> sList = new ArrayList<>();
		if(values instanceof Collection) {
			Collection<?> list = (Collection<?>) values;
			for(Object elem : list) {
				sList.add(asString(elem));
			}
		}
		return sList;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": "
				+ "encodings.allowed=" + getStringArray("encodings.allowed")
				+ ", encodings=" + getStringArray("encodings")
				+ ", root-window-size=" + capabilities.get("root_window_size")
				+ ", All caps: " + capabilities.keySet();
	}
}
