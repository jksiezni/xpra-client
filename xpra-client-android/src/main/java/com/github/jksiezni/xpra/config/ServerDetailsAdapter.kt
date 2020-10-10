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

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.jksiezni.xpra.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class ServerDetailsAdapter : ListAdapter<ServerDetails, ServerDetailsAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val primaryActionPublisher = PublishSubject.create<ServerDetails>()
    private val secondaryActionPublisher = PublishSubject.create<ServerDetails>()

    val primaryAction: Observable<ServerDetails> = primaryActionPublisher
    val secondaryAction: Observable<ServerDetails> = secondaryActionPublisher

    private val connectionFlags: MutableSet<Int> = HashSet()

    fun setConnected(serverDetails: ServerDetails, connected: Boolean) {
        val position = getPosition(serverDetails)
        if (position < 0) return
        if (connected) {
            connectionFlags.add(serverDetails.id)
        } else {
            connectionFlags.remove(serverDetails.id)
        }
        notifyItemChanged(position)
    }

    private fun getPosition(serverDetails: ServerDetails): Int = currentList.indexOf(serverDetails)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.connection_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val connected = connectionFlags.contains(item!!.id)
        holder.nameView.text = item.name
        holder.typeView.text = item.url
        holder.secondaryBtn.setOnClickListener {
            secondaryActionPublisher.onNext(item)
        }
        holder.primaryBtn.setOnClickListener {
            primaryActionPublisher.onNext(item)
        }
        val context = holder.itemView.context
        if (connected) {
            holder.secondaryBtn.setTextColor(ColorStateList.valueOf(context.getColor(R.color.design_default_color_error)))
            holder.secondaryBtn.setText(R.string.disconnect)
            holder.primaryBtn.setText(R.string.open)
        }
    }

    fun isConnected(item: ServerDetails): Boolean {
        return connectionFlags.contains(item.id)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView: TextView = itemView.findViewById(R.id.connection_name)
        val typeView: TextView = itemView.findViewById(R.id.connection_type)
        val secondaryBtn: Button = itemView.findViewById(R.id.connection_edit)
        val primaryBtn: Button = itemView.findViewById(R.id.connection_start)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ServerDetails> = object : DiffUtil.ItemCallback<ServerDetails>() {
            override fun areItemsTheSame(
                    oldItem: ServerDetails, newItem: ServerDetails): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItem: ServerDetails, newItem: ServerDetails): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldItem == newItem
            }
        }
    }
}