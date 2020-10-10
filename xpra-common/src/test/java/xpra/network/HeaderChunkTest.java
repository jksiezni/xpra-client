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

package xpra.network;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class HeaderChunkTest {

    private static final byte FLAGS = HeaderChunk.FLAG_RENCODE;
    private static final byte COMPRESSION_LVL = 1;
    private static final byte PACKET_INDEX = 2;

    private static final byte[] HEADER = {'P', FLAGS, COMPRESSION_LVL, PACKET_INDEX, 0, 0, 0, 3};

    private HeaderChunk headerChunk = new HeaderChunk();

    @Test
    public void testReadHeader() throws IOException {
        headerChunk.readHeader(new ByteArrayInputStream(HEADER));

        assertEquals(FLAGS, headerChunk.getFlags());
        assertEquals(COMPRESSION_LVL, headerChunk.getCompressionLevel());
        assertEquals(PACKET_INDEX, headerChunk.getPacketIndex());
        assertEquals(3, headerChunk.getPacketSize());
    }

    @Test
    public void testInvalidHeader_incomplete() {
        try {
            headerChunk.readHeader(new ByteArrayInputStream(new byte[1]));
            fail("Incomplete header");
        } catch (IOException e) {
            // everything is OK
        }
    }

    @Test
    public void testInvalidHeader_noMagicByte() {
        try {
            headerChunk.readHeader(new ByteArrayInputStream(new byte[8]));
            fail("Header should start with a magic byte");
        } catch (IOException e) {
            // everything is OK
        }
    }

    @Test
    public void testWriteHeader() throws IOException {
        headerChunk.setFlags(FLAGS);
        headerChunk.setCompressionLevel(COMPRESSION_LVL);
        headerChunk.setPacketIndex(PACKET_INDEX);
        headerChunk.setPacketSize(3);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        headerChunk.writeHeader(out);
        assertArrayEquals(HEADER, out.toByteArray());
    }

}
