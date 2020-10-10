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
package com.github.jksiezni.xpra

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.jksiezni.xpra.client.ConnectionEventListener
import com.github.jksiezni.xpra.client.ServiceBinderFragment
import com.github.jksiezni.xpra.config.ConfigDatabase
import com.github.jksiezni.xpra.config.ServerDetails
import com.github.jksiezni.xpra.ssh.SshUserInfoHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_connect.*
import timber.log.Timber
import java.io.IOException


class ConnectXpraActivity : AppCompatActivity(), ConnectionEventListener {

    private val serviceBinderFragment by lazy { ServiceBinderFragment.obtain(this) }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate()")
        setContentView(R.layout.activity_connect)
        serviceBinderFragment.whenXpraAvailable { api ->
            api.registerConnectionListener(this)
            if (api.isConnected) {
                setResult(RESULT_CANCELED)
                finish()
                return@whenXpraAvailable
            }
            val db = ConfigDatabase.getInstance()
            val id = intent.getIntExtra(EXTRA_CONNECTION_ID, 0)

            db.configs.getById(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { connection: ServerDetails ->
                                title = connection.name
                                api.connect(connection, SshUserInfoHandler(this))
                            },
                            { throwable: Throwable? ->
                                Timber.e(throwable)
                                finish()
                            })
                    .addTo(disposables)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
        serviceBinderFragment.whenXpraAvailable { api ->
            api.unregisterConnectionListener(this)
        }
    }

    override fun onConnected(serverDetails: ServerDetails) {
        setResult(RESULT_OK)
        finish()
    }

    override fun onDisconnected(serverDetails: ServerDetails) {
    }

    override fun onConnectionError(serverDetails: ServerDetails, e: IOException) {
        Timber.e(e)
        lifecycleScope.launchWhenStarted {
            connectProgressBar.visibility = View.GONE
            connectionLabel.text = e.message
        }
    }

    fun onCancel(@Suppress("UNUSED_PARAMETER") view: View) {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object {
        const val EXTRA_CONNECTION_ID = "connection_id"
    }

}