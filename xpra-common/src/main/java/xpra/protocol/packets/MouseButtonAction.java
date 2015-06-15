package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MouseButtonAction extends WindowPacket {

	private int button;
	private boolean pressed;
	private final List<Integer> pos = new ArrayList<Integer>();
	private final List<Integer> modifiers = new ArrayList<>();
	
	public MouseButtonAction(int windowId, int button, boolean pressed, int x, int y) {
		super("button-action");
		this.windowId = windowId;
		this.button = button;
		this.pressed = pressed;
		pos.add(x);
		pos.add(y);
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(button);
		elems.add(pressed);
		elems.add(pos);
		elems.add(modifiers);
	}

}
