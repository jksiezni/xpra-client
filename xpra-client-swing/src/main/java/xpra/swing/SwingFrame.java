/**
 * 
 */
package xpra.swing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import xpra.network.XpraSender;
import xpra.protocol.model.DrawPacket;
import xpra.protocol.model.NewWindow;
import xpra.protocol.model.WindowMetadata;

/**
 * @author Jakub Księżniak
 *
 */
public class SwingFrame extends SwingWindow<JFrame> {

	private final XpraCanvas canvas;

	public SwingFrame(int id, XpraSender sender) {
		super(id, sender, new JFrame());
		canvas = new XpraCanvas(this);
	}

	@Override
	protected void onStart(NewWindow wnd) {
		updateMetadata(wnd.getMetadata());
		
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
	protected void updateMetadata(WindowMetadata metadata) {
		super.updateMetadata(metadata);
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
