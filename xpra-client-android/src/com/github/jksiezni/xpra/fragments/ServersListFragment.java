/**
 * 
 */
package com.github.jksiezni.xpra.fragments;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.github.jksiezni.xpra.GlobalActivityAccessor;
import com.github.jksiezni.xpra.R;
import com.github.jksiezni.xpra.XpraActivity;
import com.github.jksiezni.xpra.adapters.ConnectionsAdapter;
import com.github.jksiezni.xpra.db.entities.Connection;
import com.j256.ormlite.dao.RuntimeExceptionDao;

/**
 * @author Jakub Księżniak
 *
 */
public class ServersListFragment extends Fragment {

	private GlobalActivityAccessor activityAccessor;
	private ListView serversList;
	
	private RuntimeExceptionDao<Connection, Integer> connectionDao;


	public ServersListFragment() {
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityAccessor = (GlobalActivityAccessor) getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.servers_fragment, container, false);
		serversList = (ListView) rootView.findViewById(R.id.serversList);
		serversList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Connection c = (Connection) parent.getAdapter().getItem(position);
				editConnection(c);
			}
		});
		serversList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Connection c = (Connection) parent.getAdapter().getItem(position);
				Intent intent = new Intent(getActivity(), XpraActivity.class);
				intent.putExtra("connection_id", c.getId());
				startActivity(intent);
				return true;
			}
		});
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FloatingActionButton floatingButton = activityAccessor.getFloatingActionButton();
		floatingButton.setImageResource(R.drawable.ic_add_white_36dp);
		floatingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newConnection();
			}
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GlobalActivityAccessor accessor = (GlobalActivityAccessor) getActivity();
		connectionDao = accessor.getHelper().getConnectionDao();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.servers_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		List<Connection> connections = connectionDao.queryForAll();
		serversList.setAdapter(new ConnectionsAdapter(getActivity(), connections));
	}

	protected void newConnection() {
		getFragmentManager().beginTransaction()
  		.replace(R.id.container, ConnectionPrefsFragment.create(new Connection()))
  		.addToBackStack(null)
  		.commit();
	}
	
	protected void editConnection(Connection connection) {
		getFragmentManager().beginTransaction()
  		.replace(R.id.container, ConnectionPrefsFragment.create(connection))
  		.addToBackStack(null)
  		.commit();
	}
	
}
