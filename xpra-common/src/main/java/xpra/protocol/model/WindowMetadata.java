package xpra.protocol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import xpra.protocol.PictureEncoding;

public class WindowMetadata extends WindowPacket {

	private final Map<String, Object> meta;

	public WindowMetadata() {
		this(new HashMap<String, Object>());
	}
	
	public WindowMetadata(Map<String, Object> meta) {
		super("window-metadata");
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
		final Boolean val = (Boolean) meta.get(key);
		if(val != null) {
			return val;
		}
		return false;
	}
	
	public PictureEncoding getIconEncoding() {
		List<?> iconlist = (List<?>) meta.get("icon");
		if(iconlist != null) {
			return PictureEncoding.valueOf(Packet.asString(iconlist.get(2)));
		}
		return PictureEncoding.unknown;
	}

	public byte[] getIconData() {
		List<?> iconlist = (List<?>) meta.get("icon");
		if(iconlist != null) {
			return (byte[]) iconlist.get(3);
		}
		return null;
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
		return 0;
	}
	public Integer getAsInt(String key) {
		final Object value = meta.get(key);
		if(value != null) {
			return asInt(value);
		}
		return null;
	}
}
