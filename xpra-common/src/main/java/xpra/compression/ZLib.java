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

package xpra.compression;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 *
 */
class ZLib implements Decompressor {

    private final Inflater inflater = new Inflater();

    public boolean decompress(byte[] inputData, byte[] outputData) throws CompressionException {
        try {
            inflater.reset();
            inflater.setInput(inputData);
            inflater.inflate(outputData);
            return inflater.finished();
        } catch (DataFormatException e) {
            throw new CompressionException("ZLIB decompress failed", e);
        }
    }
}
