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

package xpra.client;

import xpra.protocol.XpraSender;
import xpra.protocol.packets.CloseWindow;
import xpra.protocol.packets.ConfigureWindow;
import xpra.protocol.packets.DamageSequence;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.FocusRequest;
import xpra.protocol.packets.KeyAction;
import xpra.protocol.packets.MapWindow;
import xpra.protocol.packets.MouseButtonAction;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.PointerPosition;
import xpra.protocol.packets.UnmapWindow;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;

public abstract class XpraWindow {

	private final int id;
	private final int parentId;

	private int x;
	private int y;
	private int width;
	private int height;

	private boolean mapped;

    private XpraSender sender;

    private String title;

	public XpraWindow(NewWindow wndPacket) {
		this.id = wndPacket.getWindowId();
		this.x = wndPacket.getX();
		this.y = wndPacket.getY();
		this.width = wndPacket.getWidth();
		this.height = wndPacket.getHeight();
		this.parentId = wndPacket.getMetadata().getParentId();
		this.title = wndPacket.getMetadata().getTitle();
	}
	
	void setSender(XpraSender sender) {
		this.sender = sender;
	}
	
	public int getId() {
		return id;
	}
	
	public int getParentId() {
		return parentId;
	}
	
	public boolean hasParent() {
		return parentId != WindowMetadata.NO_PARENT;
	}

    public String getTitle() {
        return title;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void onStart(NewWindow wnd) {
		onMetadataUpdate(wnd.getMetadata());
	}
	
	protected void onStop() {
	    mapped = false;
    }
	
	protected void onMetadataUpdate(WindowMetadata metadata) {
        final String title = metadata.getTitle();
        if (title != null) {
            this.title = title;
        }
        WindowIcon icon = metadata.getIcon();
        if (icon != null) {
            onIconUpdate(icon);
        }
    }

	protected void onMoveResize(ConfigureWindow config) {
		// empty
	}
	
	protected void onIconUpdate(WindowIcon windowIcon) {
        // empty
	}

	public abstract void onDraw(DrawPacket packet);

	protected void sendDamageSequence(DrawPacket packet, long frameTime) {
		sendDamageSequence(sender, packet, frameTime);
	}

	public static void sendDamageSequence(XpraSender sender, DrawPacket packet, long frameTime) {
		if (packet.packet_sequence >= 0) {
			sender.send(new DamageSequence(packet, frameTime));
		}
	}

	protected void setFocused(boolean focused) {
		if(focused) {
			sender.send(new FocusRequest(id));
		} else {
			sender.send(new FocusRequest(0));
		}
	}

	protected void mapWindow(int x, int y, int width, int height) {
	    if (!mapped) {
            sender.send(new MapWindow(id, x, y, width, height));
            mapped = true;
        }
	}
	
	protected void configureWindow(int x, int y, int width, int height) {
		sender.send(new ConfigureWindow(id, x, y, width, height));
	}
	
	protected void unmapWindow() {
	    if (mapped) {
            sender.send(new UnmapWindow(id));
            mapped = false;
        }
	}

    public boolean isShown() {
        return mapped;
    }

    protected void closeWindow() {
		sender.send(new CloseWindow(id));
	}
	
	public void movePointer(int x, int y) {
		sender.send(new PointerPosition(id, x, y));
	}
	
	public void mouseAction(int button, boolean pressed, int x, int y) {
		if(x < 0 || y < 0) {
			throw new IllegalArgumentException("Negative coordinates are not allowed: " + x + ", " + y);
		}
		sender.send(new MouseButtonAction(id, button, pressed, x, y));
	}
	
	public void keyboardAction(int keycode, String keyname, boolean pressed) {
		sender.send(new KeyAction(id, keycode, keyname, pressed));
	}
}
