/**
 * 
 */
package xpra.network.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xpra.network.XpraConnector;

import com.github.jksiezni.rencode.RencodeInputStream;

/**
 * @author Jakub Księżniak
 *
 */
public class RencodedPacketChunk implements StreamChunk {
	private final Map<Integer, byte[]> patches;
	
	public RencodedPacketChunk(Map<Integer, byte[]> patches) {
		this.patches = patches;
	}

	@Override
	@SuppressWarnings("resource")
	public StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException {
		final RencodeInputStream rencode = new RencodeInputStream(is, false);
		final List<Object> list = rencode.readList();
		for(Entry<Integer, byte[]> entry : patches.entrySet()) {
			list.set(entry.getKey(), entry.getValue());
		}
		connector.getClient().onPacketReceived(list);
		return new HeaderChunk();
	}
	
}
