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
package com.github.jksiezni.xpra.client

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.database.Observable
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.github.jksiezni.xpra.MainActivity
import com.github.jksiezni.xpra.R
import com.github.jksiezni.xpra.config.ConnectionType
import com.github.jksiezni.xpra.config.ServerDetails
import com.github.jksiezni.xpra.ssh.SshUserInfoHandler
import com.jcraft.jsch.JSchException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import xpra.client.XpraConnector
import xpra.network.SshXpraConnector
import xpra.network.TcpXpraConnector
import java.io.IOException
import java.util.*


class XpraService : Service() {

    private val connectionObserver = ConnectionObserver()

    private lateinit var client: AndroidXpraClient
    private var connector: XpraConnector? = null
    private var serverDetails: ServerDetails? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate()")
        client = AndroidXpraClient(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy()")
        connector?.disconnect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand(): %s", intent)
        if (intent != null && ACTION_STOP == intent.action) {
            disconnect()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder()
    }

    @Throws(IOException::class)
    fun connect(serverDetails: ServerDetails, userInfoHandler: SshUserInfoHandler) {
        this.serverDetails = serverDetails
        connector = prepareConnector(serverDetails, userInfoHandler).apply {
            addListener(object : XpraConnector.ConnectionListener {
                override fun onConnected() {
                    onConnect(serverDetails)
                    connectionObserver.onConnected(serverDetails)
                }

                override fun onDisconnected() {
                    onDisconnect(serverDetails)
                    connectionObserver.onDisconnected(serverDetails)
                }

                override fun onConnectionError(e: IOException) {
                    onDisconnect(serverDetails)
                    connectionObserver.onConnectionError(serverDetails, e)
                }

            })
            connect()
        }
    }

    fun disconnect() {
        connector?.disconnect()
    }

    @Throws(IOException::class)
    private fun prepareConnector(c: ServerDetails, userInfoHandler: SshUserInfoHandler): XpraConnector {
        return when (c.type) {
            ConnectionType.TCP -> TcpXpraConnector(client, c.host, c.port)
            ConnectionType.SSH -> {
                SshXpraConnector(client, c.host, c.username, c.port, userInfoHandler).apply {
                    setupSSHConnector(this, c)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun setupSSHConnector(connector: SshXpraConnector, c: ServerDetails) {
        val knownHosts = getFileStreamPath("known_hosts")
        try {
            if (knownHosts.isFile || knownHosts.createNewFile()) {
                connector.jsch.setKnownHosts(knownHosts.absolutePath)
            }
            if (c.sshPrivateKeyFile != null) {
                connector.jsch.addIdentity(c.sshPrivateKeyFile)
            }
            if (c.displayId >= 0) {
                connector.setDisplay(c.displayId)
            }
        } catch (e: JSchException) {
            throw IOException(e)
        }
    }

    private fun onConnect(serverDetails: ServerDetails) {
        startService(Intent(this, XpraService::class.java))
        val stopIntent = Intent(this, XpraService::class.java)
        stopIntent.action = ACTION_STOP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel()
        }
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.connected_to, serverDetails.name))
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 2, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.disconnect),
                        PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build()
        startForeground(1, notification)
    }

    private fun onDisconnect(serverDetails: ServerDetails) {
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupNotificationChannel() {
        val name = getString(R.string.channel_name)
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
        channel.setShowBadge(false)
        val nm = getSystemService(NotificationManager::class.java)
        Objects.requireNonNull(nm).createNotificationChannel(channel)
    }

    private inner class LocalBinder : Binder(), XpraAPI {

        override fun getXpraClient(): AndroidXpraClient {
            return this@XpraService.client
        }

        override fun connect(serverDetails: ServerDetails, userInfoHandler: SshUserInfoHandler) {
            this@XpraService.connect(serverDetails, userInfoHandler)
        }

        override fun disconnect() {
            this@XpraService.disconnect()
        }

        override fun getConnectedServerDetails(): ServerDetails? {
            return serverDetails
        }

        override fun isConnected(): Boolean {
            return connector?.isRunning ?: false
        }

        override fun registerConnectionListener(listener: ConnectionEventListener) {
            connectionObserver.registerObserver(listener)
        }

        override fun unregisterConnectionListener(listener: ConnectionEventListener) {
            connectionObserver.unregisterObserver(listener)
        }

    }

    private class ConnectionObserver : Observable<ConnectionEventListener>(), ConnectionEventListener {
        private val mainScope = CoroutineScope(Dispatchers.Main)

        override fun onConnected(serverDetails: ServerDetails) {
            mainScope.launch {
                for (l in mObservers) {
                    l?.onConnected(serverDetails)
                }
            }
        }

        override fun onDisconnected(serverDetails: ServerDetails) {
            mainScope.launch {
                for (l in mObservers) {
                    l?.onDisconnected(serverDetails)
                }
            }
        }

        override fun onConnectionError(serverDetails: ServerDetails, e: IOException) {
            mainScope.launch {
                for (l in mObservers) {
                    l?.onConnectionError(serverDetails, e)
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "service_channel"
        private const val ACTION_STOP = "action_stop"
    }
}