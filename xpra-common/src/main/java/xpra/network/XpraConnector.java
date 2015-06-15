/**
 * 
 */
package xpra.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xpra.client.XpraClient;

/**
 * @author Jakub Księżniak
 *
 */
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
	
	public interface ConnectionListener {
		void onConnected();
		void onDisconnected();
		void onConnectionError(IOException e);
	}

}
