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

package com.github.jksiezni.xpra.config

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceFragmentCompat
import com.github.jksiezni.xpra.R
import java.util.regex.Pattern

class ServerDetailsFragment : PreferenceFragmentCompat() {

    private lateinit var dataStore: ServerDetailsDataStore

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val serverDetails = requireArguments().getSerializable(KEY_SERVER_DETAILS) as ServerDetails
        val dao = ConfigDatabase.getInstance().configs
        dataStore = ServerDetailsDataStore(serverDetails, dao)
        preferenceManager.preferenceDataStore = dataStore
        addPreferencesFromResource(R.xml.server_details_preference)
        setupPreferences()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.server_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> if (save()) {
                parentFragmentManager.popBackStack()
            }
            android.R.id.home -> parentFragmentManager.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupPreferences() {
        findPreference<Preference>(ServerDetailsDataStore.PREF_NAME)?.summaryProvider = EditTextSummaryProvider(getString(R.string.enter_unique_name))
        findPreference<Preference>(ServerDetailsDataStore.PREF_HOST)?.summaryProvider = EditTextSummaryProvider(getString(R.string.enter_unique_name))
        findPreference<Preference>(ServerDetailsDataStore.PREF_USERNAME)?.summaryProvider = EditTextSummaryProvider(getString(R.string.enter_unique_name))
        findPreference<Preference>(ServerDetailsDataStore.PREF_DISPLAY_ID)?.summaryProvider = DisplayIdSummaryProvider(getString(R.string.automatic))

        findPreference<ListPreference>(ServerDetailsDataStore.PREF_CONNECTION_TYPE)?.let {
            it.setOnPreferenceChangeListener { _, newValue ->
                setSshPreferencesEnabled(ConnectionType.SSH.name == newValue)
                true
            }
            setSshPreferencesEnabled(ConnectionType.SSH.name == it.value)
        }

        findPreference<Preference>(ServerDetailsDataStore.PREF_PRIVATE_KEY)?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/x-pem-file" // https://pki-tutorial.readthedocs.io/en/latest/mime.html
            }
            startActivity(intent)
            true
        }
    }

    private fun save(): Boolean {
        return if (validate(dataStore.serverDetails)) {
            dataStore.save()
            true
        } else {
            false
        }
    }

    private fun setSshPreferencesEnabled(enabled: Boolean) {
        val portPref = findPreference<EditTextPreference>(ServerDetailsDataStore.PREF_PORT)
        if (enabled) {
            if ("10000" == portPref?.text) {
                portPref.text = "22"
            }
        } else {
            if ("22" == portPref?.text) {
                portPref.text = "10000"
            }
        }
        findPreference<Preference>(ServerDetailsDataStore.PREF_USERNAME)?.isEnabled = enabled
        findPreference<Preference>(ServerDetailsDataStore.PREF_PRIVATE_KEY)?.isEnabled = enabled
    }

    private fun validateName(name: String?): Boolean {
        if (name == null || name.isEmpty()) {
            Toast.makeText(activity, "The connection name must not be empty.", Toast.LENGTH_LONG).show()
            return false
        }
        //		else if (!connectionDao.queryForEq("name", name).isEmpty()) {
//			Toast.makeText(getActivity(), "The connection with that name exists already.", Toast.LENGTH_LONG).show();
//			return false;
//		}
        return true
    }

    private fun validateHostname(host: String?): Boolean {
        if (host == null || host.isEmpty()) {
            Toast.makeText(activity, "The hostname must not be empty.", Toast.LENGTH_LONG).show()
            return false
        } else if (!HOSTNAME_PATTERN.matcher(host).matches()) {
            val matcher = Patterns.IP_ADDRESS.matcher(host)
            if (!matcher.matches()) {
                Toast.makeText(activity, "Invalid hostname: $host", Toast.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    private fun validate(serverDetails: ServerDetails): Boolean {
        return validateName(serverDetails.name) && validateHostname(serverDetails.host)
    }

    companion object {
        private const val KEY_SERVER_DETAILS = "server_details"

        private val HOSTNAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_\\-.]*$")

        fun create(serverDetails: ServerDetails): ServerDetailsFragment {
            return ServerDetailsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_SERVER_DETAILS, serverDetails)
                }
            }
        }
    }

}

class EditTextSummaryProvider(private val emptySummary: String) : SummaryProvider<EditTextPreference> {
    override fun provideSummary(preference: EditTextPreference): CharSequence {
        return if (TextUtils.isEmpty(preference.text)) {
            emptySummary
        } else {
            preference.text
        }
    }
}

class DisplayIdSummaryProvider(private val emptySummary: String) : SummaryProvider<EditTextPreference> {
    override fun provideSummary(preference: EditTextPreference): CharSequence {
        val text = preference.text
        return if (TextUtils.isEmpty(text) || "-1" == text) {
            emptySummary
        } else {
            text
        }
    }
}
