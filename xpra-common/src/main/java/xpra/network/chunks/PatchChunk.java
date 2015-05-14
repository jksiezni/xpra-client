/**
 * 
 */
package xpra.network.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import xpra.network.XpraConnector;

/**
 * @author Jakub Księżniak
 *
 */
public class PatchChunk implements StreamChunk {

	private final int packetIndex;
	private final byte[] buffer;
	private final Map<Integer, byte[]> patches;
	
	private int bytesRead = 0;
	
	public PatchChunk(int packetIndex, int packetSize, Map<Integer, byte[]> patches) {
		this.packetIndex = packetIndex;
		this.buffer = new byte[packetSize];
		this.patches = patches;
		
	}

	@Override
	public StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException {
		final int r = is.read(buffer, bytesRead, buffer.length-bytesRead);
		if(r < 0) {
			return null;
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
