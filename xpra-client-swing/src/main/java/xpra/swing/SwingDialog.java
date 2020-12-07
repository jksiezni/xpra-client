/*
 * Copyright (C) 2020 Jakub Ksiezniak
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
	public void onDraw(DrawPacket packet) {
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
		} finally {
			sendDamageSequence(packet, 0);
		}
	}
	
}
