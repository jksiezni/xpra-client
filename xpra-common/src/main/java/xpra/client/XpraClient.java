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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xpra.protocol.PictureEncoding;
import xpra.protocol.XpraReceiver;
import xpra.protocol.XpraSender;
import xpra.protocol.packets.ConfigureWindowOverrideRedirect;
import xpra.protocol.packets.CursorPacket;
import xpra.protocol.packets.DesktopSize;
import xpra.protocol.packets.Disconnect;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.HelloRequest;
import xpra.protocol.packets.HelloResponse;
import xpra.protocol.packets.LostWindow;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.NewWindowOverrideRedirect;
import xpra.protocol.packets.Ping;
import xpra.protocol.packets.PingEcho;
import xpra.protocol.packets.RaiseWindow;
import xpra.protocol.packets.SetDeflate;
import xpra.protocol.packets.StartupComplete;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;

public abstract class XpraClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(XpraClient.class);

    private final Map<Integer, XpraWindow> windows = new HashMap<>();

    private final PictureEncoding[] pictureEncodings;
    private final XpraKeyboard keyboard;

    private XpraSender sender;
    private XpraReceiver receiver;

    /* Configuration options. */
    private PictureEncoding encoding;
    private int desktopWidth;
    private int desktopHeight;
    private int dpi = 96;
    private int xdpi;
    private int ydpi;

    /**
     * It is set to true, when a disconnect packet is sent from a Server.
     */
    private boolean disconnectedByServer;


    public XpraClient(int desktopWidth, int desktopHeight, PictureEncoding[] supportedPictureEncodings) {
        this(desktopWidth, desktopHeight, supportedPictureEncodings, null);
    }

    public XpraClient(int desktopWidth, int desktopHeight, PictureEncoding[] supportedPictureEncodings, XpraKeyboard keyboard) {
        this.desktopWidth = desktopWidth;
        this.desktopHeight = desktopHeight;
        this.pictureEncodings = supportedPictureEncodings;
        this.encoding = pictureEncodings[0];
        this.keyboard = keyboard;
        this.receiver = new XpraReceiver();

        //  setup packet handlers
        receiver.registerHandler(HelloResponse.class, new HelloHandler());
        receiver.registerHandler(Ping.class, new PingHandler());
        receiver.registerHandler(Disconnect.class, new XpraReceiver.PacketHandler<Disconnect>() {
            @Override
            public void process(Disconnect response) throws IOException {
                LOGGER.debug("Server disconnected with msg: " + response.reason);
                disconnectedByServer = true;
            }
        });
        receiver.registerHandler(NewWindow.class, new XpraReceiver.PacketHandler<NewWindow>() {
            @Override
            public void process(NewWindow response) throws IOException {
                LOGGER.info("Processing... " + response);
                final XpraWindow parent = windows.get(response.getMetadata().getParentId());
                final XpraWindow window = onCreateWindow(response, parent);
                window.setSender(sender);
                windows.put(window.getId(), window);
                window.onStart(response);
                onWindowStarted(window);
            }
        });
        receiver.registerHandler(NewWindowOverrideRedirect.class, new XpraReceiver.PacketHandler<NewWindowOverrideRedirect>() {
            @Override
            public void process(NewWindowOverrideRedirect response) throws IOException {
                LOGGER.info("Processing... " + response);
                XpraWindow parent = windows.get(response.getParentWindowId());
                final XpraWindow window = onCreateWindow(response, parent);
                window.setSender(sender);
                windows.put(window.getId(), window);
                window.onStart(response);
                onWindowStarted(window);
            }
        });
        receiver.registerHandler(SetDeflate.class, new XpraReceiver.PacketHandler<SetDeflate>() {
            @Override
            public void process(SetDeflate response) throws IOException {
                sender.setCompressionLevel(response.compressionLevel);
            }
        });
        receiver.registerHandler(DrawPacket.class, new XpraReceiver.PacketHandler<DrawPacket>() {
            @Override
            public void process(DrawPacket packet) throws IOException {
                final XpraWindow xpraWindow = windows.get(packet.getWindowId());
                if (xpraWindow != null) {
                    xpraWindow.onDraw(packet);
                } else {
                    LOGGER.error("Missing window when handling: " + packet);
                    //XpraWindow.sendDamageSequence(sender, packet, 0);
                }
            }
        });
        receiver.registerHandler(WindowMetadata.class, new XpraReceiver.PacketHandler<WindowMetadata>() {
            @Override
            public void process(WindowMetadata meta) throws IOException {
                XpraWindow window = windows.get(meta.getWindowId());
                if (window != null) {
                    window.onMetadataUpdate(meta);
                    onWindowMetadataUpdated(window);
                }
            }
        });
        receiver.registerHandler(LostWindow.class, new XpraReceiver.PacketHandler<LostWindow>() {
            @Override
            public void process(LostWindow response) throws IOException {
                final XpraWindow window = windows.remove(response.getWindowId());
                if (window != null) {
                    window.onStop();
                    onDestroyWindow(window);
                }
            }
        });
        receiver.registerHandler(CursorPacket.class, new XpraReceiver.PacketHandler<CursorPacket>() {
            @Override
            public void process(CursorPacket packet) throws IOException {
                onCursorUpdate(packet);
            }
        });
        receiver.registerHandler(WindowIcon.class, new XpraReceiver.PacketHandler<WindowIcon>() {
            @Override
            public void process(WindowIcon response) throws IOException {
                XpraWindow window = getWindow(response.getWindowId());
                if (window != null) {
                    window.onIconUpdate(response);
                    onWindowMetadataUpdated(window);
                }
            }
        });
        receiver.registerHandler(ConfigureWindowOverrideRedirect.class,
            new XpraReceiver.PacketHandler<ConfigureWindowOverrideRedirect>() {
                @Override
                public void process(ConfigureWindowOverrideRedirect response) throws IOException {
                    XpraWindow window = windows.get(response.getWindowId());
                    if (window != null) {
                        window.onMoveResize(response);
                    }
                }
            });
        receiver.registerHandler(RaiseWindow.class, new XpraReceiver.PacketHandler<RaiseWindow>() {
            @Override
            public void process(RaiseWindow response) throws IOException {
                LOGGER.info("raise-window: " + response.getWindowId());
            }
        });
        receiver.registerHandler(StartupComplete.class, new XpraReceiver.PacketHandler<StartupComplete>() {
            @Override
            public void process(StartupComplete response) throws IOException {
                LOGGER.info(response.toString());
            }
        });
    }

    /**
     * The DPI should be set before connecting to Server.
     *
     * @param dpi
     * @param xdpi
     * @param ydpi
     */
    protected void setDpi(int dpi, int xdpi, int ydpi) {
        this.dpi = dpi;
        this.xdpi = xdpi;
        this.ydpi = ydpi;
    }

    /**
     * Called when a new window is created.
     *
     * @param wndPacket - A new window packet.
     * @param parentWindow - A parent window, or {@code null}
     * @return
     */
    protected abstract XpraWindow onCreateWindow(NewWindow wndPacket, XpraWindow parentWindow);

    /**
     * Called when a window is destroyed.
     *
     * @param window
     */
    protected void onDestroyWindow(XpraWindow window) {
    }

    protected void onWindowStarted(XpraWindow window) {
    }

    protected void onWindowMetadataUpdated(XpraWindow window) {}

    protected void onCursorUpdate(CursorPacket cursorPacket) {
        LOGGER.info(cursorPacket.toString());
    }

    public void onConnect(XpraSender sender) {
        this.sender = sender;
        final HelloRequest hello = new HelloRequest(desktopWidth, desktopHeight, keyboard, encoding, pictureEncodings);
        hello.setDpi(dpi, xdpi, ydpi);
        sender.send(hello);
    }

    public void onDisconnect() {
        for (XpraWindow w : windows.values()) {
            w.onStop();
        }
        windows.clear();
        disconnectedByServer = false;
        sender = null;
    }

    public void onConnectionError(IOException e) {
        LOGGER.error("connection error", e);
    }

    public XpraSender getSender() {
        return sender;
    }

    public XpraWindow getWindow(int windowId) {
        return windows.get(windowId);
    }

    public Collection<XpraWindow> getWindows() {
        return windows.values();
    }

    public void setDesktopSize(int width, int height) {
        this.desktopWidth = width;
        this.desktopHeight = height;
        if (sender != null) {
            sender.send(new DesktopSize(width, height));
        }
    }

    public boolean isDisconnectedByServer() {
        return disconnectedByServer;
    }

    public void setPictureEncoding(PictureEncoding pictureEncoding) {
        this.encoding = pictureEncoding;
    }

    public void onPacketReceived(List<Object> list) throws IOException {
        receiver.onReceive(list);
    }


    private class HelloHandler implements XpraReceiver.PacketHandler<HelloResponse> {

        private final SetDeflate setDeflate = new SetDeflate(3);

        @Override
        public void process(HelloResponse response) throws IOException {
            LOGGER.debug(response.toString());
            sender.useRencode(response.isRencode());
            sender.send(setDeflate);
        }
    }

    private class PingHandler implements XpraReceiver.PacketHandler<Ping> {

        @Override
        public void process(Ping response) throws IOException {
            // TODO: load average:
            long l1 = 1;
            long l2 = 1;
            long l3 = 1;
            int serverLatency = -1;
            // if len(self.server_latency)>0:
            // sl = self.server_latency[-1]
            sender.send(new PingEcho(response, l1, l2, l3, serverLatency));
        }
    }

}
