package xpra.network.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.network.XpraConnector;

public class HeaderChunk implements StreamChunk {
	public static final int FLAG_ZLIB       = 0x0;
	public static final int FLAG_RENCODE   = 0x1;
	public static final int FLAG_CIPHER    = 0x2;
	public static final int FLAG_YAML      = 0x4;
	// 0x8 is free
	public static final int FLAG_LZ4        = 0x10;
	public static final int FLAG_LZO        = 0x20;
	public static final int FLAGS_NOHEADER  = 0x40;
	// 0x80 is free
	
	static final Logger logger = LoggerFactory.getLogger(HeaderChunk.class); 
			
	private final byte[] buffer = new byte[8];
	int headerRead = 0;
	
	private final Map<Integer, byte[]> patches;
	
	public HeaderChunk() {
		patches = new HashMap<Integer, byte[]>();
	}
	
	public HeaderChunk(Map<Integer, byte[]> previousPatches) {
		patches = previousPatches;
	}
	
	@Override
	public StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException {
		final int bytesRead = is.read(buffer, headerRead, buffer.length-headerRead);
		if(bytesRead < 0) {
			return null;
		}
		headerRead += bytesRead;
		if(headerRead == buffer.length) {
			headerRead = 0;
			return parseHeader();
		}
		return this;
	}

	private StreamChunk parseHeader() throws IOException {
		final byte flags = buffer[1];
		final byte compressionLevel = buffer[2];
		final byte packetIndex = buffer[3];
		final int packetSize = (buffer[4] & 0xFF) << 24 | (buffer[5] & 0xFF) << 16 | (buffer[6] & 0xFF) << 8 | (buffer[7] & 0xFF);
		logger.trace("Header Received: " + toString() + ", size=" + packetSize);
		if(buffer[0] != 'P') {
			throw new IOException("Bad header. expected=80, received=" + buffer[0]);
		}
		if(hasFlags(flags, FLAG_CIPHER | FLAG_YAML | FLAG_LZ4 | FLAG_LZO | FLAGS_NOHEADER)) {
			throw new RuntimeException("unsupported flags detected");
		}
		
		StreamChunk packetChunk;
		if(packetIndex > 0) {
			packetChunk = new PatchChunk(packetIndex, packetSize, patches);
		} else {
			packetChunk = hasFlags(flags, FLAG_RENCODE) ? 
					new RencodedPacketChunk(patches): 
					new BencodedPacketChunk(patches);
		}
		if(compressionLevel > 0) {
			return new InflaterPacketChunk(compressionLevel, packetSize, packetChunk);
		}
		return packetChunk;
	}
	
	private static boolean hasFlags(byte value, int flags) {
		return (value & flags) > 0;
	}

	@Override
	public String toString() {
		return Arrays.toString(buffer);
	}
}