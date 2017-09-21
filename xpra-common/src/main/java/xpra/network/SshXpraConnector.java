/*
 * Copyright (C) 2017 Jakub Ksiezniak
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

package xpra.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.client.XpraClient;
import xpra.protocol.packets.Disconnect;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * An SSH connector to Xpra Server.
 */
public class SshXpraConnector extends XpraConnector implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(SshXpraConnector.class);

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
		final xpra.protocol.XpraSender s = client.getSender();
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
			session.setServerAliveInterval(1000);
			session.setServerAliveCountMax(15);
			logger.debug("Keep-alive interval={}, maxAliveCount={}", session.getServerAliveInterval(), session.getServerAliveCountMax());
			session.connect();
			final Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("~/.xpra/run-xpra _proxy :" + display);
			channel.connect();

			final InputStream in = channel.getInputStream();
			client.onConnect(new xpra.protocol.XpraSender(channel.getOutputStream()));
			fireOnConnectedEvent();
			PacketReader reader = new PacketReader(in);
			logger.info("Start Xpra connection...");
			while (!Thread.interrupted() && !client.isDisconnectedByServer()) {
        List<Object> dp = reader.readList();
        onPacketReceived(dp);
			}
		} catch (JSchException e) {
			client.onConnectionError(new IOException(e));
			fireOnConnectionErrorEvent(new IOException(e));
		} catch (IOException e) {
			client.onConnectionError(e);
			fireOnConnectionErrorEvent(e);
		} finally {
      logger.info("Finnished Xpra connection!");
			if(client.getSender() != null) try {
          client.getSender().close();
      } catch (IOException ignore) {}
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
