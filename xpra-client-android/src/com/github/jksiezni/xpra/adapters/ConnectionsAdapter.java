/**
 * 
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
