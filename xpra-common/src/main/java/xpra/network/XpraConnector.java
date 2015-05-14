/**
 * 
 */
package xpra.network;

import xpra.client.XpraClient;

/**
 * @author Jakub Księżniak
 *
 */
public abstract class XpraConnector {
	
	protected final XpraClient client;

	public XpraConnector(XpraClient client) {
		this.client = client;
	}

	public abstract boolean connect();
	
	public abstract void disconnect();
	
	public XpraClient getClient() {
		return client;
	}
}
