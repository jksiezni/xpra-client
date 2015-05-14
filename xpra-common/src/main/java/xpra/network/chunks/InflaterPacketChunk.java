/**
 * 
 */
package xpra.network.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import xpra.network.XpraConnector;

/**
 * @author Jakub Księżniak
 *
 */
public class InflaterPacketChunk implements StreamChunk {

	private final int packetSize;
	private final StreamChunk parent;

	public InflaterPacketChunk(byte compressionLevel, int packetSize, StreamChunk parent) {
		this.packetSize = packetSize;
		this.parent = parent;
	}

	@Override
	public StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException {
		InflaterInputStream inflaterInputStream = new InflaterInputStream(is, new Inflater(), packetSize);
		return parent.readChunk(inflaterInputStream, connector);
	}
	
}
