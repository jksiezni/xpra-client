package xpra.protocol.packets;

import java.util.Collection;

public class DamageSequence extends Packet {

	private int packet_sequence;
	private int id;
	private int w;
	private int h;
	private long frametime;

	public DamageSequence(DrawPacket response, long frametime) {
		super("damage-sequence");
		this.packet_sequence = response.packet_sequence;
		this.id = response.getWindowId();
		this.w = response.w;
		this.h = response.h;
		this.frametime = frametime;
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(packet_sequence);
		elems.add(id);
		elems.add(w);
		elems.add(h);
		elems.add(frametime);
	}

}
