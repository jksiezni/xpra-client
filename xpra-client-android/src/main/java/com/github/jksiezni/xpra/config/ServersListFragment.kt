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

package com.github.jksiezni.xpra.config

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.jksiezni.xpra.ConnectXpraActivity
import com.github.jksiezni.xpra.R
import com.github.jksiezni.xpra.client.ConnectionEventListener
import com.github.jksiezni.xpra.client.ServiceBinderFragment
import com.github.jksiezni.xpra.connection.ActiveConnectionFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.servers_fragment.*
import timber.log.Timber
import java.io.IOException

class ServersListFragment : Fragment() {

    private val disposables = CompositeDisposable()
    private val service by lazy { ServiceBinderFragment.obtain(activity) }
    private val adapter = ServerDetailsAdapter()

    private val connectionListener = object : ConnectionEventListener {
        override fun onConnected(serverDetails: ServerDetails) {
            adapter.setConnected(serverDetails, true)
        }

        override fun onDisconnected(serverDetails: ServerDetails) {
            adapter.setConnected(serverDetails, false)
        }

        override fun onConnectionError(serverDetails: ServerDetails, e: IOException) {
            adapter.setConnected(serverDetails, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.servers_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val floatingButton = view.findViewById<View>(R.id.floatingButton)
        floatingButton.setOnClickListener { newConnection() }
        serversList.adapter = adapter
        adapter.secondaryAction.subscribe { item ->
            if (adapter.isConnected(item)) {
                service.whenXpraAvailable { it.disconnect() }
            } else {
                editConnection(item)
            }
        }.addTo(disposables)
        adapter.primaryAction.subscribe { item ->
            when {
                adapter.isConnected(item) -> {
                    openActiveConnection()
                }
                service.xpraAPI?.isConnected == true -> {
                    Toast.makeText(requireContext(), R.string.already_connected, Toast.LENGTH_LONG).show()
                }
                else -> {
                    Timber.v("User intents to connect with xpra..")
                    val intent = Intent(context, ConnectXpraActivity::class.java)
                    intent.putExtra(ConnectXpraActivity.EXTRA_CONNECTION_ID, item.id)
                    startActivityForResult(intent, RESULT_CONNECTION)
                }
            }
        }.addTo(disposables)

        val viewModel = ViewModelProvider(this)[ServerDetailsViewModel::class.java]
        updateServersList(viewModel.getAllServers().value)
        viewModel.getAllServers().observe(viewLifecycleOwner) { items ->
            updateServersList(items)
        }

        service.whenXpraAvailable { s ->
            s.registerConnectionListener(connectionListener)
            s.connectedServerDetails?.let {
                adapter.setConnected(it, true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
        service.whenXpraAvailable { s ->
            s.unregisterConnectionListener(connectionListener)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.servers_menu, menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CONNECTION && resultCode == Activity.RESULT_OK) {
            openActiveConnection()
        }
    }

    private fun openActiveConnection() {
        parentFragmentManager.beginTransaction()
                .replace(id, ActiveConnectionFragment())
                .addToBackStack(null)
                .commit()
    }

    private fun updateServersList(list: List<ServerDetails>?) {
        adapter.submitList(list)
        if (list?.isEmpty() == false) {
            emptyView.visibility = View.GONE
        } else {
            emptyView.visibility = View.VISIBLE
        }
    }

    private fun newConnection() {
        parentFragmentManager.beginTransaction()
                .replace(id, ServerDetailsFragment.create(ServerDetails()))
                .addToBackStack(null)
                .commit()
    }

    private fun editConnection(connection: ServerDetails) {
        parentFragmentManager.beginTransaction()
                .replace(id, ServerDetailsFragment.create(connection))
                .addToBackStack(null)
                .commit()
    }

    companion object {
        private const val RESULT_CONNECTION = 1
    }

    init {
        setHasOptionsMenu(true)
    }
}