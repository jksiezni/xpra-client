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

package com.github.jksiezni.xpra.connection

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.github.jksiezni.xpra.R
import com.github.jksiezni.xpra.client.ServiceBinderFragment
import xpra.protocol.packets.StartCommand

/**
 *
 */
class StartCommandDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    private val service by lazy { ServiceBinderFragment.obtain(activity) }

    private var editText: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        editText = EditText(requireContext()).apply {
            hint = getString(R.string.hint_start_cmd)

        }
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.start_command)
                .setView(editText)
                .setPositiveButton(R.string.start, this)
                .setNegativeButton(R.string.cancel, this)
                .create()
    }

    companion object {
        const val TAG = "StartCommandDialogFragment"
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val cmd = editText?.text?.trim().toString()
                if (cmd.isNotBlank()) {
                    service.whenXpraAvailable { api ->
                        api.xpraClient.sender.send(StartCommand(cmd, cmd))
                    }
                }
            }
            else -> {
            }
        }
    }
}