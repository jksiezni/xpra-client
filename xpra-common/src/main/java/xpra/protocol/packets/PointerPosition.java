package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PointerPosition extends WindowPacket {

	List<Integer> posList = new ArrayList<>(2);
	List<Integer> modifiers = new ArrayList<>();
	
	public PointerPosition(int windowId, int x, int y) {
		super("pointer-position");
		this.windowId = windowId;
		posList.add(x);
		posList.add(y);
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(posList);
		elems.add(modifiers);
	}

}
