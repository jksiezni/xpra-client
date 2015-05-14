package xpra.protocol.model;

import java.util.Iterator;

public class CursorPacket extends Packet {

	Object k;
	
	public CursorPacket() {
		super("cursor");
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		k = iter.next();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + (k instanceof byte[] ? asString(k) : k);
	}
}
