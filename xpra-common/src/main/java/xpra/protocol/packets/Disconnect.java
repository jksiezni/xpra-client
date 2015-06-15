package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

public class Disconnect extends Packet {

	public String reason = "";
	
	public Disconnect() {
		super("disconnect");
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(reason);
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		reason = asString(iter.next());
	}

}
