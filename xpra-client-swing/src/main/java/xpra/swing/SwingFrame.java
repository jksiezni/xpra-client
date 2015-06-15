/**
 * 
 */
package xpra.swing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.WindowMetadata;

/**
 * @author Jakub Księżniak
 *
 */
public class SwingFrame extends SwingWindow<JFrame> {

	private final XpraCanvas canvas;

	public SwingFrame(NewWindow wnd) {
		super(wnd, new JFrame());
		canvas = new XpraCanvas(this);
	}

	@Override
	protected void onStart(NewWindow wnd) {
		super.onStart(wnd);
		window.setLocation(wnd.getX(), wnd.getY());
		window.getContentPane().setPreferredSize(new Dimension(wnd.getWidth(), wnd.getHeight()));
		window.getContentPane().add(canvas);
		window.pack();
		window.setVisible(true);
		
		offsetX = window.getRootPane().getX();
		offsetY = window.getRootPane().getY();
	}

	@Override
	protected void onStop() {
		window.setVisible(false);
		window.dispose();		
	}
	
	@Override
	protected void onMetadataUpdate(WindowMetadata metadata) {
		super.onMetadataUpdate(metadata);
		final String title = metadata.getAsString("title");
		if(title != null) {
			window.setTitle(title);
		}
		if(!metadata.getAsBoolean("decorations") && !window.isDisplayable()) {
			window.setUndecorated(true);
			window.setBackground(new Color(0, 0, 0, 0));
		}
	}

	@Override
	public void draw(DrawPacket packet) {
		canvas.draw(packet);
	}

}
