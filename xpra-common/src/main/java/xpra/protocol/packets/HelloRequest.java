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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import xpra.client.XpraKeyboard;
import xpra.client.XpraKeyboard.KeyDesc;
import xpra.protocol.PictureEncoding;
import xpra.protocol.ProtocolConstants;


public class HelloRequest extends xpra.protocol.IOPacket {

	private final Map<String, Object> caps = new LinkedHashMap<>();

	public HelloRequest(int screenWidth, int screenHeight, XpraKeyboard keyboard, PictureEncoding defaultEncoding, PictureEncoding[] encodings) {
		super("hello");
		caps.put("version", ProtocolConstants.VERSION);
		// if (enc_pass != null) {
		// caps.put("challenge_response", enc_pass);
		// }
		final int[] screenDims = new int[] { screenWidth, screenHeight };
		caps.put("desktop_size", screenDims);
		caps.put("dpi", 96);
//		caps.put("dpi.x", 0);
//		caps.put("dpi.y", 0);
		caps.put("client_type", "Java");
		caps.put("screen_sizes", new int[][] { screenDims });
		caps.put("encodings", PictureEncoding.toString(encodings));
		caps.put("zlib", true);
		caps.put("clipboard", false);
		caps.put("notifications", true);
		caps.put("cursors", true);
		caps.put("named_cursors", true);
		caps.put("bell", true);
		caps.put("bencode", true);
		caps.put("rencode", true);
		caps.put("chunked_compression", true);
		if (defaultEncoding != null) {
			caps.put("encoding", defaultEncoding.toString());
			if (PictureEncoding.jpeg.equals(defaultEncoding)) {
				caps.put("jpeg", 40);
			}
		}
		caps.put("platform", System.getProperty("os.name").toLowerCase());
		caps.put("uuid", UUID.randomUUID().toString().replace("-", ""));
		setKeyboard(keyboard);
	}

	public void setDpi(int dpi, int xdpi, int ydpi) {
		caps.put("dpi", dpi);		
//		caps.put("dpi.x", xdpi);		
//		caps.put("dpi.y", ydpi);		
	}

	private void setKeyboard(XpraKeyboard keyboard) {
		if (keyboard != null) {
			caps.put("keyboard", true);
			caps.put("keyboard_sync", false);
			caps.put("xkbmap_layout", keyboard.getLocale().getLanguage());
			caps.put("xkbmap_variant", keyboard.getLocale().getVariant());
			caps.put("xkbmap_keycodes", buildKeycodes(keyboard.getKeycodes()));
		} else {
			caps.put("keyboard", false);
			caps.put("keyboard_sync", false);
		}
	}

	private Object buildKeycodes(List<KeyDesc> keycodes) {
		List<Object> list = new ArrayList<>();
		for(KeyDesc kd : keycodes) {
			list.add(kd.toList());
		}
		return list;
	}
//	
//	private Object buildKeycodes2(List<KeyDesc> keycodes) {
//		Map<Object, Object> out = new HashMap<Object, Object>();
//		List<Object> list = new ArrayList<>();
//		list.add("a");
//		list.add("A");
//		out.put("10", list);
//		return out;
//	}

	@Override
	protected void serialize(Collection<Object> elems) {
		elems.add(caps);
	}

}
