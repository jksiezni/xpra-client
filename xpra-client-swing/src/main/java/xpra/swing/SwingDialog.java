package xpra.swing;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;

import xpra.protocol.PictureEncoding;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.WindowMetadata;

/**
 * @author Jakub Księżniak
 *
 */
public class SwingDialog extends SwingWindow<JDialog> {

	private final SwingWindow<?> owner;

	public SwingDialog(NewWindow wnd, SwingWindow<?> owner) {
		super(wnd, new JDialog(owner != null ? owner.window : null));
		this.owner = owner;
	}

	@Override
	protected void onStart(NewWindow wnd) {
		super.onStart(wnd);
		window.setLocation(wnd.getX()+owner.offsetX, wnd.getY() + owner.offsetY);
		window.getContentPane().setPreferredSize(new Dimension(wnd.getWidth(), wnd.getHeight()));
		window.pack();
		window.setVisible(true);
		window.createBufferStrategy(2);
		
		offsetX = window.getRootPane().getX();
		offsetY = window.getRootPane().getY();
		System.err.println(getClass().getSimpleName() + " offsets " + offsetX + "x" + offsetY);
	}

	@Override
	protected void onStop() {
		window.setVisible(false);
		window.dispose();
	}
	
	@Override
	protected void onMetadataUpdate(WindowMetadata metadata) {
		super.onMetadataUpdate(metadata);
		String title = metadata.getAsString("title");
		window.setTitle(title != null ? title : "unknown");
		if(!metadata.getAsBoolean("decorations") && !window.isDisplayable()) {
			window.setUndecorated(true);
			//window.setBackground(new Color(0, 0, 0, 0)); // FIXME add transparency
		}
	}
	
	@Override
	public void draw(DrawPacket packet) {
		if(packet.encoding != PictureEncoding.png) {
			throw new RuntimeException();
		}
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(packet.data));
			Graphics2D g = (Graphics2D) window.getBufferStrategy().getDrawGraphics();
//			g.setPaintMode();
//			g.setComposite(AlphaComposite.Src);
			g.drawImage(img, offsetX + packet.x, offsetY + packet.y, window);
			g.dispose();
			window.getBufferStrategy().show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
