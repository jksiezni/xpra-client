/**
 * 
 */
package xpra.protocol.packets;

import java.util.Iterator;

import xpra.protocol.PictureEncoding;

/**
 * @author Jakub Księżniak
 *
 */
public class WindowIcon extends WindowPacket {

	public int width;
	public int height;
	public PictureEncoding encoding;
	public byte[] data;
	
	public WindowIcon() {
		super("window-icon");
	}
	
	public WindowIcon(int windowId) {
		super("window-icon", windowId);
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		readLocal(iter);
	}
	
	void readLocal(Iterator<?> iter) {
		width = asInt(iter.next());
		height = asInt(iter.next());
		encoding = PictureEncoding.decode(asString(iter.next()));
		data = (byte[]) iter.next();	
	}

}
