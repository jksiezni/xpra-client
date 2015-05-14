/**
 * 
 */
package xpra.client;

import xpra.network.XpraSender;
import xpra.protocol.model.CloseWindow;
import xpra.protocol.model.ConfigureWindow;
import xpra.protocol.model.DrawPacket;
import xpra.protocol.model.FocusRequest;
import xpra.protocol.model.KeyAction;
import xpra.protocol.model.MapWindow;
import xpra.protocol.model.MouseButtonAction;
import xpra.protocol.model.NewWindow;
import xpra.protocol.model.PointerPosition;
import xpra.protocol.model.UnmapWindow;
import xpra.protocol.model.WindowMetadata;

/**
 * @author Jakub Księżniak
 *
 */
public abstract class XpraWindow implements XpraInput {

	private final int id;
	private final XpraSender sender;
	
	public XpraWindow(int id, XpraSender sender) {
		this.id = id;
		this.sender = sender;
	}
	
	public int getId() {
		return id;
	}
	
	protected abstract void onStart(NewWindow wnd);
	
	protected abstract void onStop();
	
	public abstract void draw(DrawPacket packet);

	protected void setFocused(boolean focused) {
		if(focused) {
			sender.send(new FocusRequest(id));
		} else {
			sender.send(new FocusRequest(0));
		}
	}

	protected void mapWindow(int x, int y, int width, int height) {
		sender.send(new MapWindow(id, x, y, width, height));
	}
	
	protected void configureWindow(int x, int y, int width, int height) {
		sender.send(new ConfigureWindow(id, x, y, width, height));
	}
	
	protected void unmapWindow() {
		sender.send(new UnmapWindow(id));
	}
	
	protected void closeWindow() {
		sender.send(new CloseWindow(id));
	}
	
	public void movePointer(int x, int y) {
		sender.send(new PointerPosition(id, x, y));
	}
	
	public void mouseAction(int button, boolean pressed, int x, int y) {
		if(x < 0 || y < 0) {
			System.err.println("Minus coords");
			System.exit(1);
		}
		sender.send(new MouseButtonAction(id, button, pressed, x, y));
	}
	
	@Override
	public void keyboardAction(int keycode, String keyname, boolean pressed) {
		sender.send(new KeyAction(id, keycode, keyname, pressed));
	}

	protected void updateMetadata(WindowMetadata metadata) {
		// empty
	}
}
