/**
 * 
 */
package xpra.swing;

import java.awt.GraphicsEnvironment;

import xpra.client.XpraClient;
import xpra.client.XpraWindow;
import xpra.protocol.PictureEncoding;
import xpra.protocol.model.NewWindow;
import xpra.swing.keyboard.SimpleXpraKeyboard;

/**
 * @author Jakub Księżniak
 *
 */
public class SwingXpraClient extends XpraClient {

	private static final PictureEncoding[] PICTURE_ENCODINGS = {PictureEncoding.png, PictureEncoding.jpeg};
	
	private static int getDesktopWidth() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	}
	
	private static int getDesktopHeight() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	}

	public SwingXpraClient() {
		super(getDesktopWidth(), getDesktopHeight(), PICTURE_ENCODINGS, new SimpleXpraKeyboard());
//		System.out.println(GraphicsEnvironment.getLocalGraphicsEnvironment()
//				.getDefaultScreenDevice()
//				.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT));
//		System.out.println(GraphicsEnvironment.getLocalGraphicsEnvironment()
//				.getDefaultScreenDevice()
//				.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSLUCENT));
//		System.out.println(GraphicsEnvironment.getLocalGraphicsEnvironment()
//				.getDefaultScreenDevice()
//				.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSPARENT));
	}

	@Override
	protected XpraWindow createWindow(NewWindow wnd) {
		if(wnd.isOverrideRedirect()) {
			final SwingWindow<?> owner = (SwingWindow<?>) getWindow(wnd.getMetadata().getParentId());
			return new SwingPopup(wnd.getWindowId(), getSender(), owner);
		} else {
			return new SwingFrame(wnd.getWindowId(), getSender());
		}
	}

}
