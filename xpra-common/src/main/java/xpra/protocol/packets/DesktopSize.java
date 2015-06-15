package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

public class DesktopSize extends Packet {

	private int width;
	private int height;

	public DesktopSize(int width, int height) {
		super("desktop_size");
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(width);
		elems.add(height);
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		width = asInt(iter.next());
		height = asInt(iter.next());
	}

}
