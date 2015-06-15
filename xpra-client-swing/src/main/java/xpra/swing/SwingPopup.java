package xpra.swing;

import java.awt.Dimension;
import java.awt.Window;

import javax.swing.Popup;
import javax.swing.PopupFactory;

import xpra.client.XpraWindow;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.NewWindow;

/**
 * @author Jakub Księżniak
 *
 */
public class SwingPopup extends XpraWindow {

	private final SwingWindow<?> owner;
	
	private XpraCanvas canvas;
	private Popup popup;

	public SwingPopup(NewWindow wnd, SwingWindow<?> rootWnd) {
		super(wnd);
		this.owner = rootWnd;
	}

	@Override
	protected void onStart(NewWindow wnd) {
		Window window = null;
		int offsetX = 0;
		int offsetY = 0;
		if(owner != null) {
			window = owner.window;
			offsetX = owner.offsetX;
			offsetY = owner.offsetY;
		}
		canvas = new XpraCanvas(this);
		canvas.setCustomRoot(window);
		canvas.setPreferredSize(new Dimension(wnd.getWidth(), wnd.getHeight()));
		popup = PopupFactory.getSharedInstance().getPopup(window, canvas, wnd.getX() + offsetX, wnd.getY() + offsetY);
		popup.show();
	}

	@Override
	protected void onStop() {
		popup.hide();
		popup = null;
	}

	@Override
	public void draw(DrawPacket packet) {
		canvas.draw(packet);
	}
	
	public SwingWindow<?> getOwner() {
		return owner;
	}

}
