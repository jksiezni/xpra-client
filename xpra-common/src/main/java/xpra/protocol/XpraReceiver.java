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

package xpra.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xpra.protocol.packets.ConfigureWindowOverrideRedirect;
import xpra.protocol.packets.CursorPacket;
import xpra.protocol.packets.Disconnect;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.HelloResponse;
import xpra.protocol.packets.LostWindow;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.NewWindowOverrideRedirect;
import xpra.protocol.packets.Ping;
import xpra.protocol.packets.RaiseWindow;
import xpra.protocol.packets.SetDeflate;
import xpra.protocol.packets.StartupComplete;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;

/**
 *
 */
public class XpraReceiver {
    private static final Logger logger = LoggerFactory.getLogger(XpraReceiver.class);
    private static final Map<String, Builder<Packet>> PACKETS_MAP = new HashMap<>();

    private final Map<Class<?>, PacketHandler<?>> handlers = new HashMap<>();

    static {
        PACKETS_MAP.put("hello", HelloResponse::new);
        PACKETS_MAP.put("cursor", CursorPacket::new);
        PACKETS_MAP.put("ping", Ping::new);
        PACKETS_MAP.put("startup-complete", StartupComplete::new);
        PACKETS_MAP.put("disconnect", Disconnect::new);
        PACKETS_MAP.put("new-window", NewWindow::new);
        PACKETS_MAP.put("new-override-redirect", NewWindowOverrideRedirect::new);
        PACKETS_MAP.put("set_deflate", SetDeflate::new);
        PACKETS_MAP.put("draw", DrawPacket::new);
        PACKETS_MAP.put("window-metadata", WindowMetadata::new);
        PACKETS_MAP.put("lost-window", LostWindow::new);
        PACKETS_MAP.put("window-icon", WindowIcon::new);
        PACKETS_MAP.put("configure-override-redirect", ConfigureWindowOverrideRedirect::new);
        PACKETS_MAP.put("raise-window", RaiseWindow::new);
        //PACKETS_MAP.put("notify_show", NotifyShow::new);
    }

    public <T extends Packet> void registerHandler(Class<T> packetClass, PacketHandler<T> handler) {
        handlers.put(packetClass, handler);
    }

    public void onReceive(List<Object> dp) throws IOException {
        if (dp.size() < 1) {
            logger.error("onReceive(..) decoded data is too small: " + dp);
            return;
        }

        Iterator<Object> it = dp.iterator();
        String type = Packet.asString(it.next());
        Builder<Packet> builder = PACKETS_MAP.get(type);
        if (builder != null) {
            Packet packet = builder.build();
            packet.deserialize(it);
            logger.trace("onReceive(): " + packet);
            process(packet);
        } else {
            logger.error("Not supported packet: " + type + ": " + dp);
        }
    }

    @SuppressWarnings("unchecked")
    private void process(Packet packet) throws IOException {
        PacketHandler handler = handlers.get(packet.getClass());
        handler.process(packet);
    }

    private interface Builder<T> {

        T build();
    }

    public interface PacketHandler<T extends Packet> {

        void process(T packet) throws IOException;
    }
}
