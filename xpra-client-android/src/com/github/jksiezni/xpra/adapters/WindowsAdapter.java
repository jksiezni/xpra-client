/**
 * 
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
