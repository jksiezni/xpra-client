/**
 * 
 */
package xpra.swing;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import xpra.client.XpraClient;
import xpra.client.XpraWindow;
import xpra.protocol.PictureEncoding;
import xpra.protocol.packets.CursorPacket;
import xpra.protocol.packets.NewWindow;
import xpra.swing.keyboard.SimpleXpraKeyboard;

/**
 * @author Jakub Księżniak
 *
 */
public class SwingXpraClient extends XpraClient {

	private static final PictureEncoding[] PICTURE_ENCODINGS = {PictureEncoding.pngP, PictureEncoding.png, PictureEncoding.jpeg};
	
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
	protected XpraWindow onCreateWindow(NewWindow wnd) {
		if(wnd.isOverrideRedirect()) {
			final XpraWindow owner = getWindow(wnd.getMetadata().getParentId());
			if(owner instanceof SwingPopup) {
				return new SwingPopup(wnd, ((SwingPopup)owner).getOwner());
			} else {
				return new SwingPopup(wnd, (SwingWindow<?>) owner);
			}
		} else {
			return new SwingFrame(wnd);
		}
	}

	@Override
	protected void onCursorUpdate(CursorPacket cursorPacket) {
		super.onCursorUpdate(cursorPacket);
		if(cursorPacket.isEmpty()) {
			return;
		}
		int width = cursorPacket.width;
		int height = cursorPacket.height;
		
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int[] nBits = {8, 8, 8, 8};
    int[] bOffs = {1, 2, 3, 0};
    ColorModel colorModel = new ComponentColorModel(cs, nBits, true, true,
                                         Transparency.TRANSLUCENT,
                                         DataBuffer.TYPE_BYTE);
    WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                            width, height,
                                            width*4, 4,
                                            bOffs, null);
    
		BufferedImage img = new BufferedImage(colorModel, raster, true, null);
		img.getRaster().setDataElements(0, 0, width, height, cursorPacket.pixels);
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension size = toolkit.getBestCursorSize(width, height);
		Point hotspot = new Point(cursorPacket.xHotspot, cursorPacket.yHotspot);
		Image outputImg = img;
		if(size.width != width || size.height != height) {
			outputImg = img.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
			hotspot.x = hotspot.x * size.width / width;
			hotspot.y = hotspot.y * size.height / height;
		}
		
		Cursor c = toolkit.createCustomCursor(outputImg, hotspot, "test");
		SwingFrame window = (SwingFrame) getWindow(1);
		window.window.setCursor(c);
	}
}
