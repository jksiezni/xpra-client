package xpra.protocol.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KeyAction extends WindowPacket {

	String keyname = "";
	boolean pressed;
	List<String> modifiers = new ArrayList<>();
	int keyval;
	String name = "";
	int keycode;
	
	public KeyAction(int windowId, int keycode, String keyname, boolean pressed) {
		super("key-action", windowId);
		this.keyval = this.keycode = keycode;
		this.keyname = this.name = keyname;
		this.pressed = pressed;
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(keyname);
		elems.add(pressed);
		elems.add(modifiers);
		elems.add(keyval);
		elems.add(name);
		elems.add(keycode);
	}

}
