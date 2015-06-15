/**
 * 
 */
package xpra.network;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.ardverk.coding.BencodingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.network.chunks.HeaderChunk;
import xpra.protocol.packets.Packet;

import com.github.jksiezni.rencode.RencodeOutputStream;

/**
 * @author Jakub Księżniak
 *
 */
public class XpraSender {
	static final Logger logger = LoggerFactory.getLogger(XpraSender.class);

	private final OutputStream outputStream;
	private final byte[] header = new byte[8];

	private final UnsafeByteArrayOutputStream byteStream = new UnsafeByteArrayOutputStream(4096);
	private final BencodingOutputStream bencoder = new BencodingOutputStream(byteStream);
	private final RencodeOutputStream rencoder = new RencodeOutputStream(byteStream);
	
	//private final Deflater deflater = new Deflater();

	private boolean closed = false;
	private boolean useRencode = false;
	private int compressionLevel = 0;

	public XpraSender(OutputStream os) {
		this.outputStream = os;
		header[0] = 'P';
		header[1] = 0;
		header[2] = 0;
	}

	public synchronized void send(Packet packet) {
		if(closed) {
			logger.warn("Stream closed! Failed to send packet: " + packet.type);
			return;
		}
		try {
			final ArrayList<Object> list = new ArrayList<>();
			packet.serialize(list);
			if (useRencode) {
				rencoder.writeCollection(list);
				header[1] = HeaderChunk.FLAG_RENCODE;
			} else {
				bencoder.writeCollection(list);
				header[1] = 0;
			}
			logger.info("send(" + list + ")");

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
			
			header[4] = (byte) ((packetSize >>> 24) & 0xFF);
			header[5] = (byte) ((packetSize >>> 16) & 0xFF);
			header[6] = (byte) ((packetSize >>> 8) & 0xFF);
			header[7] = (byte) (packetSize & 0xFF);

			logger.debug("send(): payload size is " + packetSize + " bytes");
			outputStream.write(header);
			outputStream.write(bytes, 0, packetSize);
			outputStream.flush();
			byteStream.reset();
		} catch (IOException e) {
			logger.error("Failed to send packet: " + packet.type, e);
		}
	}

	public void useRencode(boolean enabled) {
		this.useRencode = enabled;
	}

	public void setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}
	
	void setClosed(boolean closed) {
		this.closed = closed;
	}

}
