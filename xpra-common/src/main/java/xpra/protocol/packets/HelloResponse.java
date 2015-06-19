/**
 * 
 */
package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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

	public List<String> getStringArray(String key) {
		Object values = capabilities.get(key);
		List<String> sList = new ArrayList<>();
		if(values instanceof Collection) {
			Collection<?> list = (Collection<?>) values;
			for(Object elem : list) {
				sList.add(asString(elem));
			}
		}
		return sList;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append(": ");
		builder.append("encodings.allowed=");
		builder.append(getStringArray("encodings.allowed"));
		builder.append(", encodings=");
		builder.append(getStringArray("encodings"));
		return builder.toString();
	}
}
