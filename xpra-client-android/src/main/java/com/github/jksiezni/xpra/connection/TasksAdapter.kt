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
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 *
 */
class TasksAdapter(private val emptyView: TextView) : ListAdapter<TaskItem, TasksAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val clickPublisher = PublishSubject.create<TaskItem>()

    val onClickAction: Observable<TaskItem> = clickPublisher

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.window_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.titleView.text = item.title
        holder.iconView.setImageDrawable(item.icon)
        holder.itemView.setOnClickListener { clickPublisher.onNext(item) }
    }

    override fun onCurrentListChanged(previousList: MutableList<TaskItem>, currentList: MutableList<TaskItem>) {
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
        val DIFF_CALLBACK: DiffUtil.ItemCallback<TaskItem> = object : DiffUtil.ItemCallback<TaskItem>() {
            override fun areItemsTheSame(
                    oldItem: TaskItem, newItem: TaskItem): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldItem.windowId == newItem.windowId
            }

            override fun areContentsTheSame(
                    oldItem: TaskItem, newItem: TaskItem): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return Objects.equals(oldItem, newItem)
            }
        }
    }
}