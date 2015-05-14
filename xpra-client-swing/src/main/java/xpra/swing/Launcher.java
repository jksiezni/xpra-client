/**
 * 
 */
package xpra.swing;

import xpra.client.XpraClient;
import xpra.network.TcpXpraConnector;

/**
 * @author Jakub Księżniak
 *
 */
public class Launcher {

	public static void main(String[] args) throws Exception {
		XpraClient client = new SwingXpraClient();
		TcpXpraConnector connector = new TcpXpraConnector("localhost", 10000, client);
		connector.connect();
		while(connector.isRunning());
	}
}
