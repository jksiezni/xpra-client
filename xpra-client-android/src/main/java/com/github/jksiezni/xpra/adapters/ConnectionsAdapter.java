/*
 * Copyright (C) 2017 Jakub Ksiezniak
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

package com.github.jksiezni.xpra.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.jksiezni.xpra.R;
import com.github.jksiezni.xpra.db.entities.Connection;

/**
 * @author Jakub Księżniak
 *
 */
public class ConnectionsAdapter extends ArrayAdapter<Connection> {

	public ConnectionsAdapter(Context context, List<Connection> connections) {
		super(context, R.layout.connection_list_item, connections);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connection_list_item, parent, false);
		}
		TextView nameView = (TextView) view.findViewById(R.id.connection_name);
		TextView typeView = (TextView) view.findViewById(R.id.connection_type);
		
		final Connection connection = getItem(position);
		nameView.setText(connection.name);
		typeView.setText(connection.getURL());
		return view;
	}

}
