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

package com.github.jksiezni.xpra.connection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.jksiezni.xpra.client.AndroidXpraWindow
import com.github.jksiezni.xpra.client.ConnectionEventListener
import com.github.jksiezni.xpra.view.Intents
import com.github.jksiezni.xpra.client.ServiceBinderFragment
import com.github.jksiezni.xpra.config.ServerDetails
import com.github.jksiezni.xpra.databinding.ActiveConnectionFragmentBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.io.IOException

/**
 *
 */
class ActiveConnectionFragment : Fragment() {

    private var _binding: ActiveConnectionFragmentBinding? = null
    private val binding get() = _binding!!

    private val service by lazy { ServiceBinderFragment.obtain(activity) }

    private val disposables = CompositeDisposable()

    private val connectionListener = object : ConnectionEventListener {
        override fun onConnected(serverDetails: ServerDetails) {
            // do nothing
        }

        override fun onDisconnected(serverDetails: ServerDetails) {
            exit()
        }

        override fun onConnectionError(serverDetails: ServerDetails, e: IOException) {
            exit()
        }
    }

    init {
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        service.whenXpraAvailable { api ->
            api.registerConnectionListener(connectionListener)
            if (!api.isConnected) {
                exit()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        service.whenXpraAvailable { api ->
            api.unregisterConnectionListener(connectionListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActiveConnectionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = TasksAdapter(binding.emptyView)
        adapter.onClickAction.subscribe { item ->
            val intent = Intents.createXpraIntent(requireContext(), item.windowId)
            startActivity(intent)
        }.addTo(disposables)
        binding.windowsRecyclerView.adapter = adapter

        service.whenXpraAvailable { api ->
            api.xpraClient.getWindowsLiveData().observe(viewLifecycleOwner) { windows ->
                val items = windows.map {
                    val androidWindow = it as AndroidXpraWindow
                    TaskItem(it.id, it.title, androidWindow.iconDrawable)
                }
                adapter.submitList(items)
            }
        }

        binding.createCommandBtn.setOnClickListener {
            StartCommandDialogFragment().showNow(childFragmentManager, StartCommandDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        service.whenXpraAvailable { api ->
            activity?.title = api.connectionDetails?.name
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                exit()
                true
            }
            else -> false
        }
    }

    private fun exit() {
        parentFragmentManager.popBackStack()
    }
}