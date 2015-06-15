package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class WindowMetadata extends WindowPacket {
	public static final int NO_PARENT = -1;

	private final Map<String, Object> meta;
	
	public WindowMetadata() {
		this(0, new HashMap<String, Object>());
	}
	
	public WindowMetadata(int windowId, Map<String, Object> meta) {
		super("window-metadata", windowId);
		this.meta = meta;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		meta.putAll((Map<String, Object>) iter.next());
	}
	
	public int getWindowId() {
		return windowId;
	}
	
	public String getAsString(String key) {
		return Packet.asString(meta.get(key));
	}
	
	public boolean getAsBoolean(String key) {
		Object value = meta.get(key);
		if(value instanceof Boolean) {
			return (Boolean) value;
		} else if(value instanceof Number) {
			return ((Number)value).intValue() != 0;
		}
		return false;
	}
	
	public WindowIcon getIcon() {
		List<?> iconlist = (List<?>) meta.get("icon");
		WindowIcon icon = new WindowIcon(windowId);
		if(iconlist != null) {
			icon.readLocal(iconlist.iterator());
		}
		return icon;
	}
	
	@Override
	public String toString() {
		TreeMap<String, Object> m = new TreeMap<>();
		for(Entry<String, Object> e : meta.entrySet()) {
			if(e.getValue() instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> list = new ArrayList<>((List<Object>) e.getValue());
				for(int i = 0; i < list.size(); ++i) {
					if(list.get(i) instanceof byte[]) {
						list.set(i, asString(list.get(i)));
					}
				}
				m.put(e.getKey(), list);
			}
			else if(e.getValue() instanceof byte[]) {
				m.put(e.getKey(), asString(e.getValue()));
			} else {
				m.put(e.getKey(), e.getValue());
			}
		}
		return m.toString();
	}

	public void put(String key, Object value) {
		meta.put(key, value);
	}

	public int getParentId() {
		final Object value = meta.get("transient-for");
		if(value != null) {
			return asInt(value);
		}
		return NO_PARENT;
	}
	public Integer getAsInt(String key) {
		final Object value = meta.get(key);
		if(value != null) {
			return asInt(value);
		}
		return null;
	}
}
