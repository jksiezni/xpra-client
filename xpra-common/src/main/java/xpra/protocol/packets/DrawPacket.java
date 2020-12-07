/*
 * Copyright (C) 2020 Jakub Ksiezniak
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import xpra.compression.CompressionException;
import xpra.compression.Decompressor;
import xpra.compression.Decompressors;
import xpra.protocol.PictureEncoding;

public class DrawPacket extends WindowPacket {

    public int x;
    public int y;
    public int w;
    public int h;
    public PictureEncoding encoding;
    public byte[] data;
    public int packet_sequence;
    public int rowstride;

    public Map<String, Object> options = new HashMap<>();

    public DrawPacket() {
        super("draw");
    }

    @Override
    public void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        x = asInt(iter.next());
        y = asInt(iter.next());
        w = asInt(iter.next());
        h = asInt(iter.next());
        encoding = PictureEncoding.decode(asString(iter.next()));
        data = asByteArray(iter.next());
        packet_sequence = asInt(iter.next());
        rowstride = asInt(iter.next());
        if (iter.hasNext()) {
            options = asMap(iter.next());
        }
    }

    public byte[] readPixels() throws CompressionException {
        if (options.containsKey(Decompressors.COMP_ZLIB)) {
            Decompressor d = Decompressors.getByName(Decompressors.COMP_ZLIB);
            byte[] pixels = new byte[w * h * 3];
            d.decompress(data, pixels);
            return pixels;
        } else {
            return data;
        }
    }

    public String getOption(String key) {
        return asString(options.get(key));
    }

    @Override
    public String toString() {
        return String.format("%s(%d, %dx%d, %dx%d, %s, opts=%s)", getClass().getSimpleName(), windowId, x, y, w, h, encoding, options.keySet());
    }
}
