package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

public class ConfigureWindow extends WindowPacket {

	private int x;
	private int y;
	private int width;
	private int height;

	public ConfigureWindow(int windowId, int x, int y, int width, int height) {
		super("configure-window", windowId);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public ConfigureWindow(String type) {
		super(type);
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(x);
		elems.add(y);
		elems.add(width);
		elems.add(height);
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		x = asInt(iter.next());
		y = asInt(iter.next());
		width = asInt(iter.next());
		height = asInt(iter.next());
	}

}
