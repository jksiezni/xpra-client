/**
 * 
 */
package xpra.swing;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import xpra.client.XpraClient;
import xpra.network.SshXpraConnector;
import xpra.network.TcpXpraConnector;
import xpra.network.XpraConnector;

import com.jcraft.jsch.UserInfo;

/**
 * @author Jakub Księżniak
 *
 */
public class Launcher {

	public static void main(String[] args) throws Exception {
		XpraClient client = new SwingXpraClient();
		XpraConnector connector = new TcpXpraConnector(client, "localhost", 10000);
		//XpraConnector connector = createSSH(client);

		connector.connect();
		while (connector.isRunning());
	}

	private static SshXpraConnector createSSH(XpraClient client) {
		return new SshXpraConnector(client, "localhost", null, 22, new UserInfo() {

			String passwd;
			JTextField passwordField = (JTextField) new JPasswordField(20);

			@Override
			public void showMessage(String message) {
				JOptionPane.showMessageDialog(null, message);
			}

			@Override
			public boolean promptYesNo(String message) {
				return JOptionPane.showConfirmDialog(null, message) == JOptionPane.YES_OPTION;
			}

			@Override
			public boolean promptPassword(String message) {
				Object[] ob = { passwordField };
				int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					passwd = passwordField.getText();
					return true;
				} else {
					return false;
				}
			}

			@Override
			public boolean promptPassphrase(String message) {
				return false;
			}

			@Override
			public String getPassword() {
				return passwd;
			}

			@Override
			public String getPassphrase() {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
}
