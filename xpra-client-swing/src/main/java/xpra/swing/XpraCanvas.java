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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.color.ColorSpace;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import xpra.client.XpraWindow;
import xpra.protocol.packets.DrawPacket;
import xpra.swing.keyboard.KeyMap;


/**
 * @author Jakub Księżniak
 */
public class XpraCanvas extends Canvas implements HierarchyListener, MouseListener, MouseMotionListener, KeyListener {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(XpraCanvas.class);

    private final XpraWindow xwnd;

    private Window window;

    public XpraCanvas(XpraWindow window) {
        this.xwnd = window;
        addHierarchyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setBackground(new Color(0, 0, 0, 0));
    }

    public void setCustomRoot(Window wnd) {
        window = wnd;
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && isDisplayable()) {
            createBufferStrategy(2);
            requestFocus();
        }
    }

    public void draw(DrawPacket packet) {
        logger.debug("draw: " + packet);
        try {
            BufferedImage img = createBitmap(packet);
            Graphics2D g = (Graphics2D) getBufferStrategy().getDrawGraphics();
            g.drawImage(img, packet.x, packet.y, this);
            g.dispose();
            getBufferStrategy().show();
        } catch (IOException e) {
            throw new IllegalStateException("Failed decoding image: " + packet.encoding, e);
        }
    }

    private BufferedImage createBitmap(DrawPacket packet) throws IOException {
        BufferedImage img;
        switch (packet.encoding) {
            case rgb24:
                byte[] pixels = packet.readPixels();
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int[] nBits = {8, 8, 8};
                int[] bOffs = {0, 1, 2};
                ColorModel colorModel = new ComponentColorModel(cs, nBits, false, false,
                    Transparency.OPAQUE,
                    DataBuffer.TYPE_BYTE);
                WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                    packet.w, packet.h, packet.rowstride, 3,
                    bOffs, null);

                img = new BufferedImage(colorModel, raster, true, null);
                img.getRaster().setDataElements(0, 0, packet.w, packet.h, pixels);
                break;
            case png:
            case jpeg:
            case pngL:
            case pngP:
                img = ImageIO.read(new ByteArrayInputStream(packet.data));
                break;
            default:
                throw new IllegalStateException("Unexpected encoding: " + packet.encoding);
        }
        return img;
    }

    private Point getTruePos(int x, int y) {
        JRootPane root = SwingUtilities.getRootPane(window != null ? window : this);
        return new Point(x - root.getX(), y - root.getY());
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
