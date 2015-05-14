/**
 * 
 */
package xpra.network.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ardverk.coding.BencodingInputStream;

import xpra.network.XpraConnector;

/**
 * @author Jakub Księżniak
 *
 */
public class BencodedPacketChunk implements StreamChunk {

	private final Map<Integer, byte[]> patches;
	
	public BencodedPacketChunk(Map<Integer, byte[]> patches) {
		this.patches = patches;
	}

	@Override
	@SuppressWarnings("resource")
	public StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException {
		final BencodingInputStream bencode = new BencodingInputStream(is);
		final List<Object> list = bencode.readList();
		for(Entry<Integer, byte[]> entry : patches.entrySet()) {
			list.set(entry.getKey(), entry.getValue());
		}
		connector.getClient().onPacketReceived(list);
		return new HeaderChunk();
	}
	
}
