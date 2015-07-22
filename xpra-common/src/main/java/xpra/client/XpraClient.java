/**
 * 
 */
package xpra.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.network.XpraSender;
import xpra.protocol.PictureEncoding;
import xpra.protocol.packets.ConfigureWindow;
import xpra.protocol.packets.CursorPacket;
import xpra.protocol.packets.DamageSequence;
import xpra.protocol.packets.DesktopSize;
import xpra.protocol.packets.Disconnect;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.HelloRequest;
import xpra.protocol.packets.HelloResponse;
import xpra.protocol.packets.LostWindow;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.Ping;
import xpra.protocol.packets.PingEcho;
import xpra.protocol.packets.RaiseWindow;
import xpra.protocol.packets.SetDeflate;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;

/**
 * @author Jakub Księżniak
 *
 */
public abstract class XpraClient {
	static final Logger logger = LoggerFactory.getLogger(XpraClient.class);
	
	private final Map<String, PacketHandler<?>> handlers = new HashMap<String, PacketHandler<?>>();
	private final Map<Integer, XpraWindow> windows = new HashMap<Integer, XpraWindow>();
	
	private final PictureEncoding[] pictureEncodings;
	private final XpraKeyboard keyboard;
	
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
	
	private XpraSender sender;
	

	public XpraClient(int desktopWidth, int desktopHeight, PictureEncoding[] supportedPictureEncodings) {
		this(desktopWidth, desktopHeight, supportedPictureEncodings, null);
	}
	
	public XpraClient(int desktopWidth, int desktopHeight, PictureEncoding[] supportedPictureEncodings, XpraKeyboard keyboard) {
		this.desktopWidth = desktopWidth;
		this.desktopHeight = desktopHeight;
		this.pictureEncodings = supportedPictureEncodings;
		this.encoding = pictureEncodings[0];
		this.keyboard = keyboard;
		
		//  setup packet handlers
		setHandler(new HelloHandler());
		setHandler(new PingHandler());
		setHandler(new PacketHandler<Disconnect>(Disconnect.class) {
			@Override
			protected void process(Disconnect response) throws IOException {
				logger.debug("Server disconnected with msg: %s", response.reason);
				disconnectedByServer = true;
			}
		});
		setHandler(new PacketHandler<NewWindow>(NewWindow.class) {
			@Override
			public void process(NewWindow response) throws IOException {
				logger.info("Processing... " + response);
				final XpraWindow window = onCreateWindow(response);
				window.setSender(sender);
				windows.put(window.getId(), window);
				window.onStart(response);
				onWindowStarted(window);
			}
		});
		setHandler(new PacketHandler<NewWindow>(new NewWindow(true)) {
			@Override
			public void process(NewWindow response) throws IOException {
				logger.info("Processing... " + response);
				if(!response.isOverrideRedirect()) {
					response.setOverrideRedirect(true);
				}
				final XpraWindow window = onCreateWindow(response);
				window.setSender(sender);
				windows.put(window.getId(), window);
				window.onStart(response);
				onWindowStarted(window);
			}
		});
		setHandler(new PacketHandler<SetDeflate>(SetDeflate.class) {
			@Override
			protected void process(SetDeflate response) throws IOException {
				sender.setCompressionLevel(response.compressionLevel);
			}
		});
		setHandler(new PacketHandler<DrawPacket>(DrawPacket.class) {
			@Override
			protected void process(DrawPacket drawing) throws IOException {
				final long start = System.currentTimeMillis();
				XpraWindow xpraWindow = windows.get(drawing.getWindowId());
				if(xpraWindow != null) {
					xpraWindow.draw(drawing);
				}
				if (drawing.packet_sequence >= 0) {
          sender.send(new DamageSequence(drawing, System.currentTimeMillis()-start));
				}
			}
		});
		setHandler(new PacketHandler<WindowMetadata>(WindowMetadata.class) {
			@Override
			protected void process(WindowMetadata meta) throws IOException {
				windows.get(meta.getWindowId()).onMetadataUpdate(meta);
			}
		});
		setHandler(new PacketHandler<LostWindow>(LostWindow.class) {
			@Override
			protected void process(LostWindow response) throws IOException {
				final XpraWindow window = windows.remove(response.getWindowId());
				if(window != null) {
					window.onStop();
					onDestroyWindow(window);
				}
			}
		});
		setHandler(new PacketHandler<CursorPacket>(CursorPacket.class) {
			@Override
			protected void process(CursorPacket response) throws IOException {
				onCursorUpdate(response);
			}
		});
		setHandler(new PacketHandler<WindowIcon>(WindowIcon.class) {
			@Override
			protected void process(WindowIcon response) throws IOException {
				XpraWindow window = getWindow(response.getWindowId());
				if(window != null) {
					window.onIconUpdate(response);
				}
			}
		});
		setHandler(new PacketHandler<ConfigureWindow>(new ConfigureWindow("configure-override-redirect")) {
			@Override
			protected void process(ConfigureWindow response) throws IOException {
				XpraWindow window = windows.get(response.getWindowId());
				if(window != null) {
					window.onMoveResize(response);
				}
			}
		});
		setHandler(new PacketHandler<RaiseWindow>(RaiseWindow.class) {
			@Override
			protected void process(RaiseWindow response) throws IOException {
				logger.info("raise-window: " + response.getWindowId());
			}
		});
	}
	
	protected final void setHandler(PacketHandler<?> handler) {
		handlers.put(handler.getType(), handler);
	}
	
	/**
	 * The DPI should be set before connecting to Server. 
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
	 * @return
	 */
	protected abstract XpraWindow onCreateWindow(NewWindow wndPacket);

	/**
	 * Called when a window is destroyed.
	 * 
	 * @param window
	 */
	protected void onDestroyWindow(XpraWindow window) {
	}
	
	protected void onWindowStarted(XpraWindow window) {
	}

	protected void onCursorUpdate(CursorPacket cursorPacket) {
		logger.info(cursorPacket.toString());		
	}

	public void onConnect(XpraSender sender) {
		this.sender = sender;
		final HelloRequest hello = new HelloRequest(desktopWidth, desktopHeight, keyboard, encoding, pictureEncodings);
		hello.setDpi(dpi, xdpi, ydpi);
		sender.send(hello);
	}
	
	public void onDisconnect() {
		for(XpraWindow w : windows.values()) {
			w.onStop();
		}
		windows.clear();
		disconnectedByServer = false;
		sender = null;
	}
		
	public void onConnectionError(IOException e) {
		logger.error("connection error", e);
	}
	
	public final void onPacketReceived(List<Object> packetList) throws IOException {
		if (packetList.size() < 1) {
			logger.error("processPacket(..) decoded data is too small: " + packetList);
			return;
		}
		
		final Object rawType = packetList.get(0);
		if(!(rawType instanceof byte[])) {
			logger.error("processPacket(..) Expected byte[] object, but got: " + rawType);
			return;
		}
		final String type = new String((byte[]) rawType);
		if(handlers.containsKey(type)) {
			handlers.get(type).onResponse(packetList);
		} else {
			logger.error("Not supported packet type: " + type);
			System.err.println(packetList);
			System.exit(1);
		}
	}

	public XpraSender getSender() {
		return sender;
	}
	
	public XpraWindow getWindow(int windowId) {
		return windows.get(windowId);
	}
	
	public void setDesktopSize(int width, int height) {
		this.desktopWidth = width;
		this.desktopHeight = height;
		if(sender != null) {
			sender.send(new DesktopSize(1280, 800));
		}
	}
	
	public boolean isDisconnectedByServer() {
		return disconnectedByServer;
	}

	public void setPictureEncoding(PictureEncoding pictureEncoding) {
		this.encoding = pictureEncoding;
	}
	
	
	private class HelloHandler extends PacketHandler<HelloResponse> {

		private final SetDeflate setDeflate = new SetDeflate(3);
		
		public HelloHandler() {
			super(new HelloResponse());
		}

		@Override
		public void process(HelloResponse response) throws IOException {
			logger.debug(response.toString());
			logger.debug("Server caps: " + response.getCaps().toString());
			sender.useRencode(response.isRencode());
			sender.send(setDeflate);
		}
	}
	
	private class PingHandler extends PacketHandler<Ping> {
		
		public PingHandler() {
			super(new Ping());
		}
		
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
