package xpra.protocol.model;


public class FocusRequest extends WindowPacket {

	public FocusRequest(int windowId) {
		super("focus");
		this.windowId = windowId;
	}

}
