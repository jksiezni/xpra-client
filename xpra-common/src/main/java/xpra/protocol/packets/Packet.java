package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

public abstract class Packet {

	public final String type;
	
	public Packet(String type) {
		this.type = type;
	}
	
	public void serialize(Collection<Object> elems) {
		elems.add(type);
	}
	
	public void deserialize(Iterator<Object> iter) {
		final String newType = asString(iter.next());
		if(!type.equals(newType)) {
			throw new IllegalStateException("Trying to deserialize " + newType + " packet to " + getClass().getCanonicalName());
		}
	}
	
	protected static boolean asBoolean(Object obj) {
		if(obj instanceof Boolean) {
			return (Boolean) obj;
		} else if(obj instanceof Number) {
			return ((Number)obj).intValue() != 0;
		}
		return false;
	}
	
	protected static int asInt(Object obj) {
		return ((Number)obj).intValue();
	}
	
	protected static long asLong(Object obj) {
		return ((Number)obj).longValue();
	}
	
	protected static String asString(Object obj) {
		if(obj instanceof byte[]) {
			return new String((byte[])obj);
		} else {
			return (String) obj;
		}
	}
	
	protected byte[] asByteArray(Object obj) {
		return (byte[]) obj;
	}
}
