package xpra.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import xpra.client.XpraWindow;
import xpra.network.XpraSender;
import xpra.protocol.model.WindowMetadata;


/**
 * @author Jakub Księżniak
 *
 */
public abstract class SwingWindow<T extends Window> extends XpraWindow
	implements WindowFocusListener, WindowListener, ComponentListener {
	
	protected final T window;
	
	protected int offsetX;
	protected int offsetY;
	
	
	public SwingWindow(int id, XpraSender sender, T window) {
		super(id, sender);
		this.window = window;
		window.addWindowFocusListener(this);
		window.addWindowListener(this);
		window.addComponentListener(this);
	}

	@Override
	protected void updateMetadata(WindowMetadata metadata) {
		switch (metadata.getIconEncoding()) {
		case png:
		case jpeg:
			changeIcon(metadata.getIconData());
			break;

		default:
			break;
		}
	}

	protected Rectangle getContentBounds() {
		Point l = window.getLocation();
		Dimension d = SwingUtilities.getRootPane(window).getContentPane().getSize();
		return new Rectangle(l, d);
	}

	private void changeIcon(byte[] iconData) {
		window.setIconImage(new ImageIcon(iconData).getImage());
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		setFocused(true);
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		setFocused(false);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		final Rectangle r = getContentBounds();
		mapWindow(r.x, r.y, r.width, r.height);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		closeWindow();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		final Rectangle r = getContentBounds();
		configureWindow(r.x, r.y, r.width, r.height);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		final Rectangle r = getContentBounds();
		configureWindow(r.x, r.y, r.width, r.height);
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

}
