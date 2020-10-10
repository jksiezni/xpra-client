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

package xpra.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.ardverk.coding.BencodingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.network.HeaderChunk;

import com.github.jksiezni.rencode.RencodeOutputStream;

public final class XpraSender implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(XpraSender.class);

    private final OutputStream outputStream;

    private final SendWorker sendWorker;
    //private final Deflater deflater = new Deflater();

    private boolean useRencode = false;
    private int compressionLevel = 0;

    public XpraSender(OutputStream os) {
        this.outputStream = os;
        this.sendWorker = new SendWorker();
        this.sendWorker.setDaemon(true);
        this.sendWorker.start();
    }

    public synchronized void send(IOPacket packet) {
        if (!sendWorker.isAlive()) {
            logger.warn("Stream closed! Failed to send packet: " + packet.type);
            return;
        }
        final ArrayList<Object> list = new ArrayList<>();
        list.add(packet.type);
        packet.serialize(list);
        sendWorker.queue.offer(list);
    }

    public void useRencode(boolean enabled) {
        this.useRencode = enabled;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    @Override
    public void close() throws IOException {
        sendWorker.interrupt();
        try {
            sendWorker.join();
        } catch (InterruptedException e) {
            // unused
        }
    }

    private class SendWorker extends Thread {

        private final HeaderChunk headerChunk = new HeaderChunk();

        private final UnsafeByteArrayOutputStream byteStream = new UnsafeByteArrayOutputStream(4096);
        private final BencodingOutputStream bencoder = new BencodingOutputStream(byteStream);
        private final RencodeOutputStream rencoder = new RencodeOutputStream(byteStream);

        private final BlockingQueue<ArrayList> queue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    ArrayList list = queue.take();
                    send(list);
                }
            } catch (InterruptedException e) {
                logger.debug("Finished sender thread.");
            }

        }

        private void send(ArrayList list) {
            try {
                if (useRencode) {
                    rencoder.writeCollection(list);
                    headerChunk.setFlags(HeaderChunk.FLAG_RENCODE);
                } else {
                    bencoder.writeCollection(list);
                    headerChunk.setFlags(0);
                }
                logger.trace("send(" + list + ")");

                final byte[] bytes = byteStream.getBytes();
                final int packetSize = byteStream.size();

                // compress data when enabled
                if (compressionLevel > 0) {
                    // currently we do not need to use a compressed data
//				header[1] |= HeaderChunk.FLAG_ZLIB;
//				deflater.setLevel(compressionLevel);
//				deflater.setInput(bytes, 0, packetSize);
//				deflater.deflate(b, off, len);
//				deflater.reset();
                }

                headerChunk.setPacketSize(packetSize);

                logger.trace("send(): payload size is " + packetSize + " bytes");
                headerChunk.writeHeader(outputStream);
                outputStream.write(bytes, 0, packetSize);
                outputStream.flush();
                byteStream.reset();
            } catch (IOException e) {
                logger.error("Failed to send packet: " + list.get(0), e);
            }
        }
    }
}
