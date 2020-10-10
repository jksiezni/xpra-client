/*
 * Copyright (C) 2020 Jakub Ksiezniak
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

package xpra.swing;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import xpra.client.XpraClient;
import xpra.network.SshXpraConnector;
import xpra.network.TcpXpraConnector;
import xpra.client.XpraConnector;

import com.jcraft.jsch.UserInfo;


public class Launcher {

    public static void main(String[] args) throws Exception {
        XpraClient client = new SwingXpraClient();
        XpraConnector connector = new TcpXpraConnector(client, "localhost", 10000);
        //XpraConnector connector = createSSH(client);

        connector.connect();
        while (connector.isRunning()) ;
    }

    private static SshXpraConnector createSSH(XpraClient client) {
        return new SshXpraConnector(client, "localhost", null, 22, new UserInfo() {

            String passwd;
            JTextField passwordField = new JPasswordField(20);

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
                Object[] ob = {passwordField};
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
                return null;
            }
        });
    }
}
