package xpra.protocol.model;

public class UnmapWindow extends WindowPacket {

	public UnmapWindow(int windowId) {
		super("unmap-window");
		this.windowId = windowId;
	}

}
