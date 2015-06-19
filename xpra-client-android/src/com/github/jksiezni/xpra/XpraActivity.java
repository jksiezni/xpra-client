package com.github.jksiezni.xpra;

import java.io.File;
import java.io.IOException;

import xpra.network.SshXpraConnector;
import xpra.network.TcpXpraConnector;
import xpra.network.XpraConnector;
import xpra.network.XpraConnector.ConnectionListener;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.jksiezni.xpra.client.AndroidXpraClient;
import com.github.jksiezni.xpra.client.AndroidXpraClient.OnStackListener;
import com.github.jksiezni.xpra.client.AndroidXpraWindow;
import com.github.jksiezni.xpra.client.AndroidXpraWindow.XpraWindowListener;
import com.github.jksiezni.xpra.db.DatabaseHelper;
import com.github.jksiezni.xpra.db.entities.Connection;
import com.github.jksiezni.xpra.ssh.SshUserInfoHandler;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.jcraft.jsch.JSchException;

public class XpraActivity extends AppCompatActivity implements OnStackListener,
	OnNavigationItemSelectedListener, XpraWindowListener {

	private DatabaseHelper database;
	
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle actionBarDrawer;
	private NavigationView navigationView;
	private RelativeLayout xpraLayout;
	
	private AndroidXpraClient xpraClient;
	private XpraConnector connector;

	private View topView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xpra);
		database = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		// Setup toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(toolbar);
		
		// Setup navigation drawer
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		actionBarDrawer = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
		drawerLayout.setDrawerListener(actionBarDrawer);

		navigationView = (NavigationView) findViewById(R.id.navigation_view);
		navigationView.setItemIconTintList(null);
		navigationView.setNavigationItemSelectedListener(this);

		// Setup Xpra client
		xpraLayout = (RelativeLayout) findViewById(R.id.xpraLayout);
		xpraClient = new AndroidXpraClient(xpraLayout);
		xpraClient.setStackListener(this);
		
		// Setup connection
		final Intent intent = getIntent();
		if(intent == null) {
			finish();
			return;
		}
		final int id = intent.getIntExtra("connection_id", 0);
		Connection c = database.getConnectionDao().queryForId(id);
		prepareConnector(c);
		{
			TextView tv = (TextView) navigationView.findViewById(R.id.connection_name);
			tv.setText(c.name);
		}
		{
			TextView tv = (TextView) navigationView.findViewById(R.id.connectionUrlView);
			tv.setText(c.getURL());
		}
		
		ConnectingFragment connectingFragment = new ConnectingFragment();
		connector.addListener(connectingFragment);
		getFragmentManager().beginTransaction()
			.add(R.id.xpraLayout, connectingFragment)
			.commit();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		actionBarDrawer.syncState();
		getSupportActionBar().setIcon(R.mipmap.ic_launcher);
	}
	
	@Override
	protected void onDestroy() {
		OpenHelperManager.releaseHelper();
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		connector.connect();
	}
	
	@Override
	protected void onStop() {
		connector.disconnect();
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		xpraClient.onResume();
	}
	
	@Override
	protected void onPause() {
		xpraClient.onPause();
		super.onPause();
	}
	
	private void prepareConnector(Connection c) {
		switch (c.type) {
		case TCP:
			connector = new TcpXpraConnector(xpraClient, c.host, c.port);
			break;
		case SSH:
			SshXpraConnector sshConnector = new SshXpraConnector(xpraClient, c.host, c.username, c.port, new SshUserInfoHandler(this));
			setupSSHConnector(sshConnector, c);
			this.connector = sshConnector;
		default:
			break;
		}
	}

	private void setupSSHConnector(SshXpraConnector connector, Connection c) {
		try {
			final File knownHosts = getFileStreamPath("known_hosts");
			knownHosts.createNewFile();
			connector.getJsch().setKnownHosts(knownHosts.getAbsolutePath());
			if(c.sshPrivateKeyFile != null) {
				connector.getJsch().addIdentity(c.sshPrivateKeyFile);
			}
			if(c.displayId >= 0) {
				connector.setDisplay(c.displayId);
			}
		} catch (JSchException | IOException e) {
			// TODO implement some fix if necessary 
			e.printStackTrace();
		}		
	}

	private void toggleKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(topView != null) {
			if(!imm.isActive(topView)) {
				topView.setFocusable(true);
				topView.setFocusableInTouchMode(true);
				if(topView.requestFocus()) {
					imm.showSoftInput(topView, InputMethodManager.SHOW_IMPLICIT);
				}
			} else {
				imm.toggleSoftInput(0, 0);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		actionBarDrawer.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.xpra_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if(actionBarDrawer.onOptionsItemSelected(item)) {
			return true;
		}
		else if(xpraClient.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_keyboard:
			toggleKeyboard();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onWindowPushed(AndroidXpraWindow window) {
		getSupportActionBar().setTitle(window.getTitle());
		getSupportActionBar().setIcon(window.getIconDrawable());
		drawerLayout.closeDrawers();
		topView = window.getView();
		
		// update navigation drawer
		final MenuItem section = navigationView.getMenu().findItem(R.id.xpra_windows_section);
		MenuItem menu = section.getSubMenu().findItem(window.getId());
		if(menu == null) {
			window.addOnMetadataListener(this);
			menu = section.getSubMenu()
					.add(R.id.xpra_windows_group, window.getId(), Menu.NONE, window.getTitle())
					.setIcon(window.getIconDrawable());
			section.getSubMenu().setGroupCheckable(R.id.xpra_windows_group, true, true);
		}
		menu.setChecked(true);
		
		hackfixNavigationView();
	}

	private void hackfixNavigationView() {
		// HACKFIX force update items in the navigation menu. Adding items to the sub-menu do not refresh it by default.
		MenuItem temp = navigationView.getMenu().add("temp");
		navigationView.getMenu().removeItem(temp.getItemId());
	}

	@Override
	public void onWindowPoped(AndroidXpraWindow window) {
		topView = null;
		final MenuItem section = navigationView.getMenu().findItem(R.id.xpra_windows_section);
		section.getSubMenu().removeItem(window.getId());
		hackfixNavigationView();
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		if(item.getGroupId() == R.id.xpra_windows_group) {
			return xpraClient.onWindowSelected(item.getItemId());
		}
		switch (item.getItemId()) {
		case R.id.drawer_disconnect:
			finish();
			return true;

		default:
			return false;
		}
	}

	@Override
	public void onMetadataChanged(AndroidXpraWindow window) {
	}

	@Override
	public void onIconChanged(AndroidXpraWindow window) {
		final MenuItem section = navigationView.getMenu().findItem(R.id.xpra_windows_section);
		MenuItem menu = section.getSubMenu().findItem(window.getId());
		if(menu != null) {
			menu.setIcon(window.getIconDrawable());
			if(menu.isChecked()) {
				getSupportActionBar().setIcon(window.getIconDrawable());
			}
			hackfixNavigationView();
		}
	}
	
	private class ConnectingFragment extends Fragment implements ConnectionListener {
		private static final int MSG_ERROR = 1;
		private static final int MSG_CONNECTED = 2;
		private static final int MSG_DISCONNECTED = 3;
		
		String disconnectReason = "Disconnected from server.";
		
		final Handler handler = new Handler(new Handler.Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_ERROR:
					drawerLayout.openDrawer(Gravity.START);
					return true;
				case MSG_CONNECTED:
					progressView.setVisibility(View.INVISIBLE);
					textView.setText("There's no windows opened.");
					return true;
				case MSG_DISCONNECTED:
					progressView.setVisibility(View.INVISIBLE);
					textView.setText(disconnectReason);
					return true;

				default:
					return false;
				}
			}
		});

		private TextView textView;
		private View progressView;
		private boolean connected;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.connecting_fragment, container, false);
		}
		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			textView = (TextView) getView().findViewById(R.id.connectionTextView);
			progressView = getView().findViewById(R.id.progressBar1);
		}
		
		@Override
		public void onConnected() {
			connected = true;
			handler.obtainMessage(MSG_CONNECTED).sendToTarget();
		}

		@Override
		public void onDisconnected() {
			if(!isDestroyed()) {
				handler.obtainMessage(MSG_DISCONNECTED).sendToTarget();
			}
		}

		@Override
		public void onConnectionError(final IOException e) {
			if(connected) {
				disconnectReason = "Connection lost."; 
			} else {
				disconnectReason = "Failed to connect.";
				handler.obtainMessage(MSG_ERROR, e).sendToTarget();
			}
		}
	}
}
