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

package com.github.jksiezni.xpra;

import android.os.Bundle;

import com.github.jksiezni.xpra.config.ServersListFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the custom toolbar
        final Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new ServersListFragment())
                .commit();
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this::shouldDisplayNavigateUp);
        shouldDisplayNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Timber.v("onBackPressed()");
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigateUp() {
        Timber.v("onNavigateUp()");
        return getSupportFragmentManager().popBackStackImmediate() || super.onNavigateUp();
    }

    private void shouldDisplayNavigateUp() {
        Timber.v("shouldDisplayNavigateUp()");
        final boolean showNavigateUp = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(showNavigateUp);
    }
}
