package com.github.jksiezni.xpra.fragments;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xpra.protocol.PictureEncoding;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.design.widget.FloatingActionButton;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.github.jksiezni.xpra.GlobalActivityAccessor;
import com.github.jksiezni.xpra.R;
import com.github.jksiezni.xpra.XpraActivity;
import com.github.jksiezni.xpra.db.entities.Connection;
import com.github.jksiezni.xpra.db.entities.ConnectionType;
import com.github.jksiezni.xpra.preference.PreferenceHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;

/**
 * @author Jakub Księżniak
 *
 */
public class ConnectionPrefsFragment extends PreferenceFragment {

	private static final String TEMP_CONN_PREFERENCES = "connection_prefs.tmp";

	private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_\\-\\.]*$");

	private GlobalActivityAccessor activityAccessor;

	private RuntimeExceptionDao<Connection, Integer> connectionDao;

	private Connection connection;

	public ConnectionPrefsFragment() {
		setHasOptionsMenu(true);
		connection = new Connection();
	}

	public ConnectionPrefsFragment(Connection connection) {
		this();
		this.connection = connection;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityAccessor = (GlobalActivityAccessor) getActivity();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityAccessor.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getPreferenceManager().setSharedPreferencesName(TEMP_CONN_PREFERENCES);

		// setup connection DAO
		connectionDao = activityAccessor.getHelper().getConnectionDao();

		final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
		if (connection.getId() != 0) {
			preferences.edit()
				.putString("connection_type", connection.type.toString())
				.putString("name", connection.name)
				.putString("host", connection.host)
				.putString("username", connection.username)
				.putString("private_keyfile", connection.sshPrivateKeyFile)
				.putString("port", String.valueOf(connection.port))
				.putString("display_id", String.valueOf(connection.displayId))
				.putString("picture_encoding", connection.pictureEncoding.toString())
				.apply();
		}

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.connection_preferences);
		setup();

		for (Entry<String, ?> entry : preferences.getAll().entrySet()) {
			final Preference p = findPreference(entry.getKey());
			if (p != null) {
				PreferenceHelper.callChangeListener(p, entry.getValue());
			}
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FloatingActionButton button = activityAccessor.getFloatingActionButton();
		button.setImageResource(R.drawable.ic_play_arrow_white_48dp);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(save()) {
  				Intent intent = new Intent(getActivity(), XpraActivity.class);
  				intent.putExtra("connection_id", connection.getId());
  				startActivity(intent);
				}
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.connection_edit_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_save:
			if (save()) {
				getFragmentManager().popBackStack();
			}
			break;

		case android.R.id.home:
			getFragmentManager().popBackStack();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setup() {
		findPreference("name").setOnPreferenceChangeListener(new SimplePreferenceChanger("Enter a unique connection name"));
		findPreference("host").setOnPreferenceChangeListener(new SimplePreferenceChanger("Enter the hostname"));
		findPreference("username").setOnPreferenceChangeListener(new SimplePreferenceChanger("Enter your username"));
		findPreference("private_keyfile").setOnPreferenceChangeListener(new SimplePreferenceChanger("Choose a private key file"));
		findPreference("connection_type").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				final ConnectionType type = ConnectionType.valueOf(newValue.toString());
				switch (type) {
				case TCP:
					preference.setSummary(R.string.connection_type_tcp);
					setSshPreferencesEnabled(false);
					break;
				case SSH:
					preference.setSummary(R.string.connection_type_ssh);
					setSshPreferencesEnabled(true);
					break;
				}
				return true;
			}
		});
		findPreference("port").setOnPreferenceChangeListener(new SimplePreferenceChanger(""));
		findPreference("display_id").setOnPreferenceChangeListener(new SimplePreferenceChanger("Automatic") {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					connection.displayId = Integer.parseInt(newValue.toString());
				} catch(NumberFormatException e) {
					connection.displayId = -1;
				} finally {
					if(connection.displayId < 0) {
						preference.setSummary(defaultSummary);
					} else {
						preference.setSummary(newValue.toString());
					}
				}
				return true;
			}
		});
		ListPreference pictureEncPreference = (ListPreference) findPreference("picture_encoding");
		pictureEncPreference.setEntries(PictureEncoding.toString(PictureEncoding.values()));
		pictureEncPreference.setEntryValues(PictureEncoding.toString(PictureEncoding.values()));
		pictureEncPreference.setOnPreferenceChangeListener(new SimplePreferenceChanger(""));
	}

	protected void setSshPreferencesEnabled(boolean enabled) {
		final EditTextPreference portPref = (EditTextPreference) findPreference("port");
		if(enabled) {
			if("10000".equals(portPref.getText())) {
				portPref.setText("22");
				PreferenceHelper.callChangeListener(portPref, portPref.getText());
			}
		} else {
			if("22".equals(portPref.getText())) {
				portPref.setText("10000");
				PreferenceHelper.callChangeListener(portPref, portPref.getText());
			}
		}
		findPreference("username").setEnabled(enabled);
		findPreference("private_keyfile").setEnabled(enabled);
	}

	protected boolean validateName(String name) {
		if (name == null || name.isEmpty()) {
			Toast.makeText(getActivity(), "The connection name must not be empty.", Toast.LENGTH_LONG).show();
			return false;
		} else if (name.equals(connection.name)) {
			return true;
		} else if (!connectionDao.queryForEq("name", name).isEmpty()) {
			Toast.makeText(getActivity(), "The connection with that name exists already.", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	protected boolean validateHostname(final String host) {
		if (host == null || host.isEmpty()) {
			Toast.makeText(getActivity(), "The hostname must not be empty.", Toast.LENGTH_LONG).show();
			return false;
		} else if (!HOSTNAME_PATTERN.matcher(host).matches()) {
			final Matcher matcher = Patterns.IP_ADDRESS.matcher(host);
			if (!matcher.matches()) {
				Toast.makeText(getActivity(), "Invalid hostname: " + host, Toast.LENGTH_LONG).show();
				return false;
			}
		}
		return true;
	}

	private boolean validate(SharedPreferences prefs) {
		final String name = prefs.getString("name", "");
		final String host = prefs.getString("host", "");
		return validateName(name) && validateHostname(host);
	}

	private boolean save() {
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		if (validate(prefs)) {
			connection.name = prefs.getString("name", null);
			connection.type = ConnectionType.valueOf(prefs.getString("connection_type", "TCP"));
			connection.host = prefs.getString("host", null);
			connection.port = Integer.parseInt(prefs.getString("port", "10000"));
			connection.username = prefs.getString("username", null);
			connection.sshPrivateKeyFile = prefs.getString("private_keyfile", null);
			connection.pictureEncoding = PictureEncoding.decode(prefs.getString("picture_encoding", "png"));
			connectionDao.createOrUpdate(connection);
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		getPreferenceManager().getSharedPreferences().edit().clear().apply();
		activityAccessor.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		super.onDestroy();
	}

	private static class SimplePreferenceChanger implements OnPreferenceChangeListener {

		protected final String defaultSummary;

		public SimplePreferenceChanger(String defaultSummary) {
			this.defaultSummary = defaultSummary;
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (newValue == null || newValue.toString().isEmpty()) {
				preference.setSummary(defaultSummary);
			} else {
				preference.setSummary(newValue.toString());
			}
			return true;
		}
	}

}
