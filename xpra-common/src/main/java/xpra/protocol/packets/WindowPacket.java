package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

public abstract class WindowPacket extends Packet {

	protected int windowId;

	protected WindowPacket(String type) {
		super(type);
	}
	
	protected WindowPacket(String type, int wid) {
		super(type);
		this.windowId = wid;
	}
	
	public int getWindowId() {
		return windowId;
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(windowId);
	}

	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		windowId = asInt(iter.next());
	}

}