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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderChunk {
	public static final int FLAG_ZLIB       = 0x0;
	public static final int FLAG_RENCODE   = 0x1;
	public static final int FLAG_CIPHER    = 0x2;
	public static final int FLAG_YAML      = 0x4;
	// 0x8 is free
	public static final int FLAG_LZ4        = 0x10;
	public static final int FLAG_LZO        = 0x20;
	public static final int FLAGS_NOHEADER  = 0x40;
	// 0x80 is free

  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderChunk.class);

  private static final int HEADER_SIZE = 8;
  private static final byte MAGIC_BYTE = 'P';

  private final byte[] header = new byte[HEADER_SIZE];


  public HeaderChunk() {
    header[0] = MAGIC_BYTE;
  }

	void readHeader(InputStream is) throws IOException {
    int headerRead = 0;
    while (headerRead < HEADER_SIZE) {
      final int bytesRead = is.read(header, headerRead, HEADER_SIZE - headerRead);
      if (bytesRead < 0) {
        throw new EOFException("Failed to read header.");
      }
      headerRead += bytesRead;
    }
    LOGGER.trace("Header received: " + toString() + ", size=" + getPacketSize());
    if(header[0] != MAGIC_BYTE) {
      throw new IOException("Bad header. expected=80, received=" + header[0]);
    }
    if(hasFlags(FLAG_CIPHER | FLAG_YAML | FLAG_LZ4 | FLAG_LZO | FLAGS_NOHEADER)) {
      throw new IOException("unsupported flags detected");
    }
	}

  public void writeHeader(OutputStream outputStream) throws IOException {
    outputStream.write(header);
  }

	byte getFlags() {
    return header[1];
  }

  public void setFlags(int flags) {
    header[1] = (byte) flags;
  }

  boolean hasFlags(int flags) {
    return (getFlags() & flags) > 0;
  }

  byte getCompressionLevel() {
    return header[2];
  }

  void setCompressionLevel(int level) {
    header[2] = (byte) level;
  }

  boolean isDataCompressed() {
    return getCompressionLevel() > 0;
  }

  int getPacketIndex() {
    return header[3];
  }

  void setPacketIndex(int packetIndex) {
    header[3] = (byte) packetIndex;
  }

  int getPacketSize() {
    return (header[4] & 0xFF) << 24 | (header[5] & 0xFF) << 16 | (header[6] & 0xFF) << 8 | (header[7] & 0xFF);
  }

  public void setPacketSize(int packetSize) {
    header[4] = (byte) ((packetSize >>> 24) & 0xFF);
    header[5] = (byte) ((packetSize >>> 16) & 0xFF);
    header[6] = (byte) ((packetSize >>> 8) & 0xFF);
    header[7] = (byte) (packetSize & 0xFF);
  }

	@Override
	public String toString() {
		return getClass().getSimpleName() + Arrays.toString(header);
	}
}