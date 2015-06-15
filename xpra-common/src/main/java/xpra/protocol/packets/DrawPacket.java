package xpra.protocol.packets;

import java.util.Iterator;

import xpra.protocol.PictureEncoding;

public class DrawPacket extends WindowPacket {

	public int x;
	public int y;
	public int w;
	public int h;
	public PictureEncoding encoding;
	public byte[] data;
	public int packet_sequence;
	public int rowstride;
	
	public DrawPacket() {
		super("draw");
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		x = asInt(iter.next());
		y = asInt(iter.next());
		w = asInt(iter.next());
		h = asInt(iter.next());
		encoding = PictureEncoding.valueOf(asString(iter.next()));
		data = asByteArray(iter.next());
		packet_sequence = asInt(iter.next());
		rowstride = asInt(iter.next());
	}

	@Override
	public String toString() {
		return String.format("%s(%d, %d, %d, %d, %s)",getClass().getSimpleName(), x, y, w, h, encoding);
	}
}
