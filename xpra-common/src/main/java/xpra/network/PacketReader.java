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

import com.github.jksiezni.rencode.RencodeInputStream;

import org.ardverk.coding.BencodingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 *
 */

class PacketReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(PacketReader.class);

  private final InputStream in;

  private final HeaderChunk headerChunk = new HeaderChunk();
  private final PatchChunk patchChunk = new PatchChunk();

  PacketReader(InputStream in) {
    this.in = in;
  }

  List<Object> readList() throws IOException {
    final Map<Integer, byte[]> patches = new HashMap<>();
    boolean packetReady = false;
    while (!packetReady) {
      // read header
      headerChunk.readHeader(in);
      // if packetIndex > 0 then read patches
      if(headerChunk.getPacketIndex() > 0) {
        byte[] patch = patchChunk.readPatch(in, headerChunk.getPacketSize(), headerChunk.isDataCompressed());
        patches.put(headerChunk.getPacketIndex(), patch);
      } else {
        // else decode to list
        packetReady = true;
      }
    }
    InputStream input = in;
    if(headerChunk.isDataCompressed()) {
      input = new ChunkInflaterInputStream(in, new Inflater(), headerChunk.getPacketSize());
    }
    List<Object> list;
    if (headerChunk.hasFlags(HeaderChunk.FLAG_RENCODE)) {
      list = readRencodedList(input);
    } else {
      list = readBencodedList(input);
    }
    if (headerChunk.isDataCompressed()) {
      ((ChunkInflaterInputStream)input).drain();
    }
    for(Map.Entry<Integer, byte[]> entry : patches.entrySet()) {
			list.set(entry.getKey(), entry.getValue());
		}
    return list;
  }

  private List<Object> readBencodedList(InputStream is) throws IOException {
    return new BencodingInputStream(is).readList();
  }

  private List<Object> readRencodedList(InputStream is) throws IOException {
    return new RencodeInputStream(is, false).readList();
  }
}
