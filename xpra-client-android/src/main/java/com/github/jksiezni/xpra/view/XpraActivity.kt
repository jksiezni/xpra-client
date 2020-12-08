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
package com.github.jksiezni.xpra.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.github.jksiezni.xpra.R
import com.github.jksiezni.xpra.client.*
import com.github.jksiezni.xpra.client.AndroidXpraWindow.XpraWindowListener
import com.github.jksiezni.xpra.view.Intents.getWindowId
import com.github.jksiezni.xpra.view.Intents.isValidXpraActivityIntent
import com.github.jksiezni.xpra.config.ServerDetails
import com.github.jksiezni.xpra.databinding.ActivityXpraBinding
import timber.log.Timber
import java.io.IOException

class XpraActivity : AppCompatActivity(), XpraEventListener, XpraWindowListener, ConnectionEventListener {

    private lateinit var binding: ActivityXpraBinding

    private lateinit var serviceBinderFragment: ServiceBinderFragment

    private var windowId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate(): %s", intent)
        serviceBinderFragment = ServiceBinderFragment.obtain(this)
        binding = ActivityXpraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!isValidXpraActivityIntent(intent)) {
            finish()
            return
        }
        windowId = getWindowId(intent)
        setSupportActionBar(binding.toolbar)

        serviceBinderFragment.whenXpraAvailable { api ->
            val rootWindow = api.xpraClient.getWindow(windowId)
            if (rootWindow == null) {
                Timber.w("Window with windowId=%d not found", windowId)
                finish()
                return@whenXpraAvailable
            }
            title = rootWindow.title
            api.registerConnectionListener(this)
            api.xpraClient.addEventListener(this)
            rootWindow.addWindowListener(this)
            restoreProxyViewHierarchy(rootWindow)
            setResult(RESULT_OK)
        }
    }

    private fun restoreProxyViewHierarchy(rootWindow: AndroidXpraWindow) {
        binding.workspaceView.addView(ProxyWindowView(this, rootWindow))
        val list = mutableListOf<AndroidXpraWindow>()
        list.addAll(rootWindow.children)
        while (list.isNotEmpty()) {
            val child = list.removeFirst()
            val proxyView = ProxyWindowView(this, child)
            binding.workspaceView.addView(proxyView)
            child.addWindowListener(XpraWindowHandler(proxyView))
            list.addAll(child.children)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceBinderFragment.whenXpraAvailable { api ->
            val window = api.xpraClient.getWindow(windowId)
            window?.removeWindowListener(this)
            api.xpraClient.removeEventListener(this)
            api.unregisterConnectionListener(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.i("onNewIntent(): %s", getIntent())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.xpra_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_keyboard -> {
                toggleKeyboard(binding.workspaceView)
                true
            }
            R.id.action_close -> {
                serviceBinderFragment.whenXpraAvailable { api ->
                    val window = api.xpraClient.getWindow(windowId)
                    window?.close()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleKeyboard(view: View?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null) {
            if (!imm.isActive(view)) {
                view.isFocusable = true
                view.isFocusableInTouchMode = true
                if (view.requestFocus()) {
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                }
            } else {
                imm.toggleSoftInput(0, 0)
            }
        }
    }

    override fun onWindowCreated(window: AndroidXpraWindow) {
        if (window.hasParent(windowId)) {
            runOnUiThread {
                val proxyView = ProxyWindowView(this, window)
                binding.workspaceView.addView(proxyView)
                window.addWindowListener(XpraWindowHandler(proxyView))
            }
        } else {
            startActivity(Intents.createXpraIntent(this, window.id))
        }
    }

    override fun onWindowLost(window: AndroidXpraWindow) {
        // each window view removes itself, when is lost
    }

    override fun onMetadataChanged(window: AndroidXpraWindow) {
        title = window.title
    }

    override fun onIconChanged(window: AndroidXpraWindow) {
        supportActionBar?.setIcon(window.iconDrawable)
    }

    override fun onLost(window: AndroidXpraWindow) {
        finish()
    }

    override fun onConnected(serverDetails: ServerDetails) {
    }

    override fun onDisconnected(serverDetails: ServerDetails) {
        finish()
    }

    override fun onConnectionError(serverDetails: ServerDetails, e: IOException) {
        finish()
    }


    inner class XpraWindowHandler(private val proxyView: ProxyWindowView) : XpraWindowListener {
        override fun onMetadataChanged(window: AndroidXpraWindow?) {
        }

        override fun onIconChanged(window: AndroidXpraWindow?) {
        }

        override fun onLost(window: AndroidXpraWindow?) {
            binding.workspaceView.removeView(proxyView)
        }
    }

}