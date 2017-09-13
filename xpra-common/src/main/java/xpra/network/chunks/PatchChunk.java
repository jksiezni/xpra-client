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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.network.XpraConnector;

public class PatchChunk implements StreamChunk {
	private final static Logger logger = LoggerFactory.getLogger(PatchChunk.class);

	private final int packetIndex;
	private final byte[] buffer;
	private final ByteArrayOutputStream output;
	private final Map<Integer, byte[]> patches;
	private final boolean compressed;
	
	private int bytesRead = 0;
	
	public PatchChunk(int packetIndex, int packetSize, Map<Integer, byte[]> patches, boolean compressed) {
		this.packetIndex = packetIndex;
		this.buffer = new byte[packetSize];
		this.patches = patches;
		this.compressed = compressed;
		this.output = compressed ? new ByteArrayOutputStream(packetSize) : null;
	}

	@Override
	public StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException {
		if(compressed) {
			return readCompressed(is);
		} else {
  		final int r = is.read(buffer, bytesRead, buffer.length-bytesRead);
  		if(r < 0) {
  			logger.error("Unexpected end of stream.");
  			throw new EOFException("Unexpected end of stream: " + is);
  		}
  		bytesRead += r;
  		if(bytesRead == buffer.length) {
  			bytesRead = 0;
  			patches.put(packetIndex, buffer);
  			return new HeaderChunk(patches);
  		}
  		return this;
		}
	}
	
	private StreamChunk readCompressed(InputStream is) throws IOException {
		while(is.available() > 0) {
			final int r = is.read(buffer, 0, buffer.length);
			if(r < 0) {
				break;
			}
			output.write(buffer, 0, r);
		}
		patches.put(packetIndex, output.toByteArray());
		return new HeaderChunk(patches);
	}

}
