/**
 * 
 */
package xpra.client;

import java.io.IOException;
import java.util.List;

import xpra.protocol.packets.Packet;

/**
 * @author Jakub Księżniak
 */
public abstract class PacketHandler<T extends Packet> {

	private final T data;
	
	public PacketHandler(T data) {
		this.data = data;
	}
	
	public PacketHandler(Class<T> packetClass) {
		try {
			this.data = packetClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getType() {
		return data.type;
	}
	
	protected T onResponse(List<Object> dp) throws IOException {
		data.deserialize(dp.iterator());
		process(data);
		return data;
	}
	
	protected abstract void process(T response) throws IOException;
	
}
