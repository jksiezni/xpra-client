package xpra.protocol.model;

import java.util.Collection;
import java.util.Iterator;

public class SetDeflate extends Packet {

	public int compressionLevel;

	public SetDeflate() {
		super("set_deflate");
	}

	public SetDeflate(int compressionLevel) {
		super("set_deflate");
		this.compressionLevel = compressionLevel;
	}

	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(compressionLevel);
	}

	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		compressionLevel = asInt(iter.next());
	}

}
