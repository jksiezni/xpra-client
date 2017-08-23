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

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.jksiezni.xpra.R;
import com.github.jksiezni.xpra.client.AndroidXpraWindow;
import com.github.jksiezni.xpra.client.AndroidXpraWindow.XpraWindowListener;

/**
 * @author Jakub Księżniak
 *
 */
public class WindowsAdapter extends ArrayAdapter<AndroidXpraWindow> implements XpraWindowListener {

	public WindowsAdapter(Context context) {
		super(context, R.layout.window_list_item, new ArrayList<AndroidXpraWindow>());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);
		final AndroidXpraWindow item = getItem(position);
		view.setCompoundDrawables(item.getIconDrawable(), null, null, null);
		return view;
	}
	
	@Override
	public void add(AndroidXpraWindow object) {
		super.add(object);
		object.addOnMetadataListener(this);
	}

	@Override
	public void onMetadataChanged(AndroidXpraWindow metadata) {
		notifyDataSetChanged();
	}

	@Override
	public void onIconChanged(AndroidXpraWindow window) {
		// TODO Auto-generated method stub
		
	}

}
