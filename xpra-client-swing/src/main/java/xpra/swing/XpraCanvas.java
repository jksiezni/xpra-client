package xpra.swing;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import xpra.client.XpraWindow;
import xpra.protocol.PictureEncoding;
import xpra.protocol.packets.DrawPacket;
import xpra.swing.keyboard.KeyMap;


/**
 * @author Jakub Księżniak
 *
 */
public class XpraCanvas extends Canvas implements HierarchyListener, MouseListener, MouseMotionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	
	private final XpraWindow xwnd;

	private Window window;

	public XpraCanvas(XpraWindow window) {
		this.xwnd = window;
		addHierarchyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		setBackground(new Color(0,0,0,0));
	}
	
	public void setCustomRoot(Window wnd) {
		window = wnd;
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && isDisplayable()) {
			createBufferStrategy(2);
			requestFocus();
		}
	}

	public void draw(DrawPacket packet) {
		if(packet.encoding != PictureEncoding.png
				&& packet.encoding != PictureEncoding.pngL
				&& packet.encoding != PictureEncoding.pngP) {
			throw new RuntimeException("Invalid encoding: " + packet.encoding);
		}
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(packet.data));
			Graphics2D g = (Graphics2D) getBufferStrategy().getDrawGraphics();
//			g.setPaintMode();
//			g.setComposite(AlphaComposite.Src);
			g.drawImage(img, packet.x, packet.y, this);
			g.dispose();
			getBufferStrategy().show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Point getTruePos(int x, int y) {
		JRootPane root = SwingUtilities.getRootPane(window != null ? window : this);
		//System.err.println("root insets: " + root.getLocation());
		return new Point(x-root.getX(), y-root.getY());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point p = getTruePos(e.getXOnScreen(), e.getYOnScreen());
		xwnd.mouseAction(e.getButton(), true, p.x, p.y);		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = getTruePos(e.getXOnScreen(), e.getYOnScreen());
		xwnd.movePointer(p.x, p.y);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// not used
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point p = getTruePos(e.getXOnScreen(), e.getYOnScreen());
		xwnd.mouseAction(e.getButton(), true, p.x, p.y);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Point p = getTruePos(e.getXOnScreen(), e.getYOnScreen());
		xwnd.mouseAction(e.getButton(), false, p.x, p.y);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Point p = getTruePos(e.getXOnScreen(), e.getYOnScreen());
		xwnd.movePointer(p.x, p.y);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Point p = getTruePos(e.getXOnScreen(), e.getYOnScreen());
		xwnd.movePointer(p.x, p.y);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// not used
	}

	@Override
	public void keyPressed(KeyEvent e) {
		xwnd.keyboardAction(e.getKeyCode(), KeyMap.getUnicodeName(e.getKeyCode()), true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		xwnd.keyboardAction(e.getKeyCode(), KeyMap.getUnicodeName(e.getKeyCode()), false);
	}

}
