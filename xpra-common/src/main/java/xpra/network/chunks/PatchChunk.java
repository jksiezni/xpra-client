/**
 * 
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

/**
 * @author Jakub Księżniak
 *
 */
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
