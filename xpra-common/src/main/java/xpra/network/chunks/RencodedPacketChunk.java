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

package xpra.network.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xpra.network.XpraConnector;

import com.github.jksiezni.rencode.RencodeInputStream;

public class RencodedPacketChunk implements StreamChunk {
	private final Map<Integer, byte[]> patches;
	
	public RencodedPacketChunk(Map<Integer, byte[]> patches) {
		this.patches = patches;
	}

	@Override
	public StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException {
		final RencodeInputStream rencode = new RencodeInputStream(is, false);
		final List<Object> list = rencode.readList();
		for(Entry<Integer, byte[]> entry : patches.entrySet()) {
			list.set(entry.getKey(), entry.getValue());
		}
		connector.onPacketReceived(list);
		return new HeaderChunk();
	}
	
}
