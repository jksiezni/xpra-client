package xpra.protocol.packets;


public class FocusRequest extends WindowPacket {

	public FocusRequest(int windowId) {
		super("focus");
		this.windowId = windowId;
	}

}
