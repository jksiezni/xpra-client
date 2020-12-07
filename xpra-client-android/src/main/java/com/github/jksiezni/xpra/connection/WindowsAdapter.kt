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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.jksiezni.xpra.R
import com.github.jksiezni.xpra.client.AndroidXpraWindow
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import xpra.client.XpraWindow
import java.util.*

/**
 *
 */
class WindowsAdapter(private val emptyView: TextView) : ListAdapter<XpraWindow, WindowsAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val clickPublisher = PublishSubject.create<AndroidXpraWindow>()

    val onClickAction: Observable<AndroidXpraWindow> = clickPublisher

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.window_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) as AndroidXpraWindow
        holder.titleView.text = item.title
        holder.iconView.setImageDrawable(item.iconDrawable)
        holder.itemView.setOnClickListener { clickPublisher.onNext(item) }
    }

    override fun onCurrentListChanged(previousList: MutableList<XpraWindow>, currentList: MutableList<XpraWindow>) {
        if (currentList.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconView: ImageView = itemView.findViewById(android.R.id.icon)
        val titleView: TextView = itemView.findViewById(android.R.id.title)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<XpraWindow> = object : DiffUtil.ItemCallback<XpraWindow>() {
            override fun areItemsTheSame(
                    oldItem: XpraWindow, newItem: XpraWindow): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItem: XpraWindow, newItem: XpraWindow): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return Objects.equals(oldItem, newItem)
            }
        }
    }
}