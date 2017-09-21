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
import java.util.ArrayList;
import java.util.List;

import xpra.client.XpraClient;

public abstract class XpraConnector {
	
	protected final XpraClient client;
	private final List<ConnectionListener> listeners = new ArrayList<>();

	public XpraConnector(XpraClient client) {
		this.client = client;
	}

	public abstract boolean connect();
	
	public abstract void disconnect();
	
	public abstract boolean isRunning();
	
	public XpraClient getClient() {
		return client;
	}
	
	public void addListener(ConnectionListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public boolean removeListener(ConnectionListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	protected void fireOnConnectedEvent() {
		synchronized (listeners) {
			for(ConnectionListener l : listeners) {
				l.onConnected();
			}
		}
	}
	
	protected void fireOnDisconnectedEvent() {
		synchronized (listeners) {
			for(ConnectionListener l : listeners) {
				l.onDisconnected();
			}
		}
	}
	
	protected void fireOnConnectionErrorEvent(IOException e) {
		synchronized (listeners) {
			for(ConnectionListener l : listeners) {
				l.onConnectionError(e);
			}
		}
	}

  protected void onPacketReceived(List<Object> list) throws IOException {
    client.onPacketReceived(list);
  }

  public interface ConnectionListener {
		void onConnected();
		void onDisconnected();
		void onConnectionError(IOException e);
	}

}
