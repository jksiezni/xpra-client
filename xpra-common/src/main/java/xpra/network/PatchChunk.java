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

package xpra.network;

import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;


class PatchChunk {

  private final Inflater inflater = new Inflater();
  private final byte[] buffer = new byte[4096];

	byte[] readPatch(InputStream is, int packetSize, boolean compressed) throws IOException {
		if(compressed) {
			return readCompressed(is, packetSize);
		} else {
      byte[] buffer = new byte[packetSize];
      int bytesRead = 0;
      while (bytesRead < packetSize) {
        final int r = is.read(buffer, bytesRead, packetSize - bytesRead);
        if (r < 0) {
          throw new EOFException("Unexpected end of stream: " + is);
        }
        bytesRead += r;
      }
      return buffer;
		}
	}
	
	private byte[] readCompressed(InputStream in, int packetSize) throws IOException {
    ChunkInflaterInputStream inCompressed = new ChunkInflaterInputStream(in, inflater, packetSize);
    ByteArrayOutputStream output = new ByteArrayOutputStream(packetSize);
		while(inCompressed.available() > 0) {
			final int r = inCompressed.read(buffer, 0, buffer.length);
			if(r < 0) {
        LoggerFactory.getLogger(getClass()).warn("readCompressed(): EOS");
				break;
			}
			output.write(buffer, 0, r);
		}
    inCompressed.drain();
		return output.toByteArray();
	}

}
