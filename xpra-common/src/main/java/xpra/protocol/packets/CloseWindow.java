package xpra.protocol.packets;

public class CloseWindow extends WindowPacket {

	public CloseWindow(int id) {
		super("close-window");
		windowId = id;
	}

}
