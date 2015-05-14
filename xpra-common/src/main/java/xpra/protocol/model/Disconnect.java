package xpra.protocol.model;

import java.util.Iterator;

public class Disconnect extends Packet {

	public String reason;
	
	public Disconnect() {
		super("disconnect");
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		reason = asString(iter.next());
	}

}
