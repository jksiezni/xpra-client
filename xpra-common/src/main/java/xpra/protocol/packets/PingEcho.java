/**
 * 
 */
package xpra.protocol.packets;

import java.util.Collection;

/**
 * @author Jakub Księżniak
 *
 */
public class PingEcho extends Packet {

	public long timestamp;
	public long l1 = 1;
	public long l2 = 1;
	public long l3 = 1;
	public int serverLatency = -1;
	
	public PingEcho(Ping ping, long l1, long l2, long l3, int serverLatency) {
		super("ping_echo");
		timestamp = ping.getTimestamp();
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(timestamp);
		elems.add(l1);
		elems.add(l2);
		elems.add(l3);
		elems.add(serverLatency);
	}

}
