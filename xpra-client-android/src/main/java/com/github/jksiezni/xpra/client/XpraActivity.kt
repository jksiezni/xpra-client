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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.github.jksiezni.xpra.R
import com.github.jksiezni.xpra.client.AndroidXpraWindow.XpraWindowListener
import com.github.jksiezni.xpra.client.Intents.getWindowId
import com.github.jksiezni.xpra.client.Intents.isValidXpraActivityIntent
import com.github.jksiezni.xpra.config.ServerDetails
import kotlinx.android.synthetic.main.activity_xpra.*
import timber.log.Timber
import java.io.IOException

class XpraActivity : AppCompatActivity(), XpraWindowListener, ConnectionEventListener {

    private var windowId = 0

    private val serviceBinderFragment: ServiceBinderFragment by lazy { ServiceBinderFragment.obtain(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate(): %s", intent)
        setContentView(R.layout.activity_xpra)
        if (!isValidXpraActivityIntent(intent)) {
            finish()
            return
        }
        windowId = getWindowId(intent)
        setSupportActionBar(toolbar)

        serviceBinderFragment.whenXpraAvailable { api ->
            val window = api.xpraClient.getWindow(windowId)
            window.setTargetView(xpraView)
            title = window.title
            api.registerConnectionListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceBinderFragment.whenXpraAvailable { api -> api.unregisterConnectionListener(this) }
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
                toggleKeyboard(xpraView)
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

    override fun onMetadataChanged(window: AndroidXpraWindow) {
        title = window.title
    }

    override fun onIconChanged(window: AndroidXpraWindow) {
        supportActionBar?.setIcon(window.iconDrawable)
    }

    override fun onConnected(serverDetails: ServerDetails) {
    }

    override fun onDisconnected(serverDetails: ServerDetails) {
        finish()
    }

    override fun onConnectionError(serverDetails: ServerDetails, e: IOException) {
        finish()
    }
}