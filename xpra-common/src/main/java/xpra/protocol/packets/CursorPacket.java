package xpra.protocol.packets;

import java.util.Iterator;

public class CursorPacket extends Packet {

	private boolean empty;
	
	public int x;
	public int y;
	public int width;
	public int height;
	public int xHotspot;
	public int yHotspot;
	public int serial;
	public byte[] pixels;

	public String name;
	
	public CursorPacket() {
		super("cursor");
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		Object first = iter.next();
		if(first instanceof byte[] && asString(first).isEmpty()) {
			empty = true;
			return; // empty cursor
		}
		empty = false;
		x = asInt(first);
		y = asInt(iter.next());
		width = asInt(iter.next());
		height = asInt(iter.next());
		xHotspot = asInt(iter.next());
		yHotspot = asInt(iter.next());
		serial = asInt(iter.next());
		pixels = asByteArray(iter.next());

		if(iter.hasNext()) {
			// named cursor
			name = asString(iter.next());
		}
	}
	
	public boolean isEmpty() {
		return empty;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append(": ");
		if(isEmpty()) {
			builder.append("empty");
		} else {
			builder.append("name=" + name);
			builder.append(" pixels.length=" + pixels.length);
		}
		return  builder.toString();
	}
}
