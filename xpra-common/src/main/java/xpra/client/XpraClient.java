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
import xpra.protocol.model.CursorPacket;
import xpra.protocol.model.DamageSequence;
import xpra.protocol.model.Disconnect;
import xpra.protocol.model.DrawPacket;
import xpra.protocol.model.HelloRequest;
import xpra.protocol.model.HelloResponse;
import xpra.protocol.model.LostWindow;
import xpra.protocol.model.NewWindow;
import xpra.protocol.model.Ping;
import xpra.protocol.model.PingEcho;
import xpra.protocol.model.SetDeflate;
import xpra.protocol.model.WindowMetadata;

/**
 * @author Jakub Księżniak
 *
 */
public abstract class XpraClient {
	static final Logger logger = LoggerFactory.getLogger(XpraClient.class);
	
	private final Map<String, PacketHandler<?>> handlers = new HashMap<String, PacketHandler<?>>();
	private final Map<Integer, XpraWindow> windows = new HashMap<Integer, XpraWindow>();
	
	private final PictureEncoding[] pictureEncodings;
	private PictureEncoding encoding;
	private int desktopWidth;
	private int desktopHeight;
	private int compressionLevel = 0;
	
	private XpraKeyboard keyboard;
	
	private XpraSender sender;

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
				System.err.println("Server disconnected with msg: " + response.reason);
			}
		});
		setHandler(new PacketHandler<NewWindow>(NewWindow.class) {
			@Override
			public void process(NewWindow response) throws IOException {
				logger.info("Processing... " + response);
				final XpraWindow window = createWindow(response);
				windows.put(window.getId(), window);
				window.onStart(response);
			}
		});
		setHandler(new PacketHandler<NewWindow>(new NewWindow(true)) {
			@Override
			public void process(NewWindow response) throws IOException {
				logger.info("Processing... " + response);
				if(!response.isOverrideRedirect()) {
					response.setOverrideRedirect(true);
				}
				final XpraWindow window = createWindow(response);
				windows.put(window.getId(), window);
				window.onStart(response);
			}
		});
		setHandler(new PacketHandler<SetDeflate>(SetDeflate.class) {
			@Override
			protected void process(SetDeflate response) throws IOException {
				compressionLevel = response.compressionLevel;
			}
		});
		setHandler(new PacketHandler<DrawPacket>(DrawPacket.class) {
			@Override
			protected void process(DrawPacket drawing) throws IOException {
				final long start = System.currentTimeMillis();
				windows.get(drawing.getWindowId()).draw(drawing);
				if (drawing.packet_sequence >= 0) {
          sender.send(new DamageSequence(drawing, System.currentTimeMillis()-start));
				}
			}
		});
		setHandler(new PacketHandler<WindowMetadata>(WindowMetadata.class) {
			@Override
			protected void process(WindowMetadata meta) throws IOException {
				windows.get(meta.getWindowId()).updateMetadata(meta);
			}
		});
		setHandler(new PacketHandler<LostWindow>(LostWindow.class) {
			@Override
			protected void process(LostWindow response) throws IOException {
				windows.remove(response.getWindowId()).onStop();
			}
		});
		setHandler(new PacketHandler<CursorPacket>(CursorPacket.class) {
			@Override
			protected void process(CursorPacket response) throws IOException {
				System.out.println(response);
			}
		});
	}
	
	protected final void setHandler(PacketHandler<?> handler) {
		handlers.put(handler.getType(), handler);
	}
	
	protected abstract XpraWindow createWindow(NewWindow wnd);

	public void onConnect(XpraSender sender) {
		this.sender = sender;
		sender.send(new HelloRequest(desktopWidth, desktopHeight, keyboard, encoding, pictureEncodings));
	}
	
	public void onDisconnect() {
		for(XpraWindow w : windows.values()) {
			w.onStop();
		}
	}
		
	public void onConnectionError(IOException e) {
		logger.error("connection error", e);
	}
	
	public XpraSender getSender() {
		return sender;
	}
	
	public XpraWindow getWindow(int windowId) {
		return windows.get(windowId);
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
	
	private class HelloHandler extends PacketHandler<HelloResponse> {

		private final SetDeflate setDeflate = new SetDeflate(3);
		
		public HelloHandler() {
			super(new HelloResponse());
		}

		@Override
		public void process(HelloResponse response) throws IOException {
			logger.debug("Hello: " + response.getCaps().toString());
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
