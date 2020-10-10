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

    private XpraSender sender;

    private String title;

	public XpraWindow(NewWindow wndPacket) {
		this.id = wndPacket.getWindowId();
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

    protected void onStart(NewWindow wnd) {
		onMetadataUpdate(wnd.getMetadata());
	}
	
	protected abstract void onStop();
	
	protected void onMetadataUpdate(WindowMetadata metadata) {
		onIconUpdate(metadata.getIcon());
		this.title = metadata.getTitle();
	}

	protected void onMoveResize(ConfigureWindow config) {
		// empty
	}
	
	protected void onIconUpdate(WindowIcon windowIcon) {
		
	}

	public abstract void draw(DrawPacket packet);

	protected void sendDamageSequence(DrawPacket packet, long frameTime) {
		sendDamageSequence(sender, packet, frameTime);
	}

	static void sendDamageSequence(XpraSender sender, DrawPacket packet, long frameTime) {
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
		sender.send(new MapWindow(id, x, y, width, height));
	}
	
	protected void configureWindow(int x, int y, int width, int height) {
		sender.send(new ConfigureWindow(id, x, y, width, height));
	}
	
	protected void unmapWindow() {
		sender.send(new UnmapWindow(id));
	}
	
	protected void closeWindow() {
		sender.send(new CloseWindow(id));
	}
	
	public void movePointer(int x, int y) {
		sender.send(new PointerPosition(id, x, y));
	}
	
	public void mouseAction(int button, boolean pressed, int x, int y) {
		if(x < 0 || y < 0) {
			throw new IllegalArgumentException("Minus coordinates are not allowed: " + x + ", " + y);
		}
		sender.send(new MouseButtonAction(id, button, pressed, x, y));
	}
	
	public void keyboardAction(int keycode, String keyname, boolean pressed) {
		sender.send(new KeyAction(id, keycode, keyname, pressed));
	}
}
