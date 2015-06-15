package com.github.jksiezni.xpra;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.jksiezni.xpra.db.DatabaseHelper;
import com.github.jksiezni.xpra.fragments.ServersListFragment;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class MainActivity extends AppCompatActivity implements GlobalActivityAccessor {

	private DatabaseHelper database;
	
	private FloatingActionButton floatingButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		database = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		// Set the custom toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setLogo(R.mipmap.ic_launcher);
		
		floatingButton = (FloatingActionButton) findViewById(R.id.floatingButton);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new ServersListFragment()).commit();
		}
	}

	@Override
	protected void onDestroy() {
		OpenHelperManager.releaseHelper();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(!getFragmentManager().popBackStackImmediate()) {
			super.onBackPressed();
		}
	}

	@Override
	public DatabaseHelper getHelper() {
		return database;
	}

	@Override
	public FloatingActionButton getFloatingActionButton() {
		return floatingButton;
	}

}
