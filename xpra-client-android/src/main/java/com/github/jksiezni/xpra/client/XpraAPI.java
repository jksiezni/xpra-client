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

package com.github.jksiezni.xpra.client;

import com.github.jksiezni.xpra.config.ServerDetails;
import com.github.jksiezni.xpra.ssh.SshUserInfoHandler;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *
 */
public interface XpraAPI {

    @NonNull
    AndroidXpraClient getXpraClient();

    @Nullable
    ServerDetails getConnectionDetails();

    void connect(@NonNull ServerDetails serverDetails, @NonNull SshUserInfoHandler userInfoHandler) throws IOException;

    void disconnect();

    boolean isConnected();

    void registerConnectionListener(ConnectionEventListener listener);

    void unregisterConnectionListener(ConnectionEventListener listener);
}
