/**
 * 
 */
package xpra.protocol.model;

import java.util.Iterator;


/**
 * @author Jakub Księżniak
 *
 */
public class Ping extends Packet {
	
	private long timestamp;

	public Ping() {
		super("ping");
	}

  @Override
  public void deserialize(Iterator<Object> iter) {
  	super.deserialize(iter);
  	timestamp = asLong(iter.next());
  }
	
	public long getTimestamp() {
		return timestamp;
	}

}
