/**
 * 
 */
package xpra.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.client.XpraClient;
import xpra.network.chunks.HeaderChunk;
import xpra.network.chunks.StreamChunk;

/**
 * @author Jakub Księżniak
 *
 */
public class TcpXpraConnector extends XpraConnector implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(TcpXpraConnector.class);
	
	private final String host;
	private final int port;
	
	private Thread thread;
	
	public TcpXpraConnector(String hostname, int port, XpraClient client) {
		super(client);
		this.host = hostname;
		this.port = port;
	}

	@Override
	public synchronized boolean connect() {
		if(thread != null) {
			return false;
		}
		thread = new Thread(this);
		thread.start();
		return true;
	}
	
	@Override
	public synchronized void disconnect() {
		if(thread != null) {
  		thread.interrupt();
  		thread = null;
		}
	}

	@Override
	public void run() {
		try(Socket socket = new Socket(host, port);
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();) {
			socket.setKeepAlive(true);
			client.onConnect(new XpraSender(os));
			StreamChunk reader = new HeaderChunk();
			logger.info("Start Xpra connection...");
			while(!Thread.interrupted() && reader != null) {
				reader = reader.readChunk(is, this);
			}
			logger.info("Finnished Xpra connection!");
		} catch (IOException e) {
			client.onConnectionError(e);
		}
		finally {
			client.onDisconnect();
		}
	}

	public boolean isRunning() {
		return thread != null ? thread.isAlive() : false;
	}
	
}
