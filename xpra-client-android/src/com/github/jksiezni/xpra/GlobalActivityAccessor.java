package com.github.jksiezni.xpra;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;

import com.github.jksiezni.xpra.db.DatabaseHelper;

public interface GlobalActivityAccessor {

	DatabaseHelper getHelper();
	
	ActionBar getSupportActionBar();
	
	FloatingActionButton getFloatingActionButton();
}
