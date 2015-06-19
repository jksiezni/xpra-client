package xpra.network;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.client.XpraClient;
import xpra.network.chunks.HeaderChunk;
import xpra.network.chunks.StreamChunk;
import xpra.protocol.packets.Disconnect;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * An SSH connector to Xpra Server.
 * @author Jakub Księżniak
 *
 */
public class SshXpraConnector extends XpraConnector implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(SshXpraConnector.class);

	private final JSch jsch = new JSch();

	private final UserInfo userInfo;
	private final String username;
	private final String host;
	private final int port;
	
	private int display = 100;

	private Thread thread;
	private Session session;

	public SshXpraConnector(XpraClient client, String host) {
		this(client, host, null);
	}

	public SshXpraConnector(XpraClient client, String host, String username) {
		this(client, host, username, 22, null);
	}

	public SshXpraConnector(XpraClient client, String host, String username, int port, UserInfo userInfo) {
		super(client);
		this.host = host;
		this.username = username;
		this.port = port;
		this.userInfo = userInfo;
		JSch.setConfig("compression_level", "0");
	}

	@Override
	public boolean connect() {
		if (thread != null) {
			return false;
		}
		try {
			session = jsch.getSession(username, host, port);
			session.setUserInfo(userInfo);
			//disableStrictHostKeyChecking();
			
			thread = new Thread(this);
			thread.start();
		} catch (JSchException e) {
			client.onConnectionError(new IOException(e));
			return false;
		}
		return true;
	}

	/**
	 * This setting will cause JSCH to automatically add all target servers'
	 * entry to the known_hosts file
	 */
	void disableStrictHostKeyChecking() {
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
	}

	@Override
	public synchronized void disconnect() {
		if(thread != null) {
			if(!disconnectCleanly()) {
    		thread.interrupt();
			}
			thread = null;
		}
	}

	private boolean disconnectCleanly() {
		final XpraSender s = client.getSender();
		if(s != null) {
			s.send(new Disconnect());
			return true;
		}
		return false;
	}

	@Override
	public boolean isRunning() {
		return thread != null && thread.isAlive();
	}

	@Override
	public void run() {
		try {
			session.connect();
			final Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("~/.xpra/run-xpra _proxy :" + display);
			channel.connect();

			final InputStream in = channel.getInputStream();
			client.onConnect(new XpraSender(channel.getOutputStream()));
			fireOnConnectedEvent();
			StreamChunk reader = new HeaderChunk();
			logger.info("Start Xpra connection...");
			while (!Thread.interrupted() && !client.isDisconnectedByServer()) {
				reader = reader.readChunk(in, this);
			}
			logger.info("Finnished Xpra connection!");
		} catch (JSchException e) {
			client.onConnectionError(new IOException(e));
			fireOnConnectionErrorEvent(new IOException(e));
		} catch (IOException e) {
			client.onConnectionError(e);
			fireOnConnectionErrorEvent(e);
		} finally {
			if(client.getSender() != null) {
				client.getSender().setClosed(true);
			}
			if (session != null) {
				session.disconnect();
			}
			client.onDisconnect();
			fireOnDisconnectedEvent();
		}
	}

	public JSch getJsch() {
		return jsch;
	}
	
	public void setDisplay(int displayId) {
		this.display = displayId;
	}

}
