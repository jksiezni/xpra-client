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
public class HelloResponse extends Packet {
	
	private Map<String, Object> capabilities;

	public HelloResponse() {
		super("hello");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		capabilities = (Map<String, Object>) iter.next();
	}

	public Map<String, Object> getCaps() {
		return capabilities;
	}

	public boolean isRencode() {
		return asBoolean(capabilities.get("rencode"));
	}

}
