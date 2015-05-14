/**
 * 
 */
package xpra.protocol.model;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Jakub Księżniak
 *
 */
public class NewWindow extends WindowPacket {

	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected WindowMetadata metadata;
	
	public NewWindow() {
		this(false);
	}
	
	public NewWindow(boolean overrideRedirect) {
		super(overrideRedirect ? "new-override-redirect" : "new-window");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		x = asInt(iter.next());
		y = asInt(iter.next());
		width = asInt(iter.next());
		height = asInt(iter.next());
		metadata = new WindowMetadata((Map<String, Object>) iter.next());
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("( id=");
		builder.append(windowId);
		builder.append(", ");
		builder.append(x);
		builder.append('x');
		builder.append(y);
		builder.append(':');
		builder.append(width);
		builder.append('x');
		builder.append(height);
		builder.append(", ");
		builder.append(metadata);
		return builder.toString();
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public WindowMetadata getMetadata() {
		return metadata;
	}

	public boolean isOverrideRedirect() {
		return metadata.getAsBoolean("override-redirect");
	}
	
	public void setOverrideRedirect(boolean enabled) {
		metadata.put("override-redirect", enabled);
	}
}
