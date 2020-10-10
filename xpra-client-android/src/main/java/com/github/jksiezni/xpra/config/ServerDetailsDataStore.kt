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

import androidx.preference.PreferenceDataStore
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.NotNull
import xpra.protocol.PictureEncoding

/**
 *
 */
class ServerDetailsDataStore(
        val serverDetails: ServerDetails,
        private val dao: @NotNull ConnectionDao) : PreferenceDataStore() {

    companion object {
        const val PREF_CONNECTION_TYPE = "connection_type"
        const val PREF_NAME = "name"
        const val PREF_HOST = "host"
        const val PREF_PORT = "port"
        const val PREF_USERNAME = "username"
        const val PREF_PRIVATE_KEY = "private_keyfile"
        const val PREF_DISPLAY_ID = "display_id"
        const val PREF_PICTURE_ENC = "picture_encoding"
    }

    override fun getString(key: String, defValue: String?): String? {
        return when (key) {
            PREF_CONNECTION_TYPE -> serverDetails.type.name
            PREF_NAME -> serverDetails.name
            PREF_HOST -> serverDetails.host
            PREF_PORT -> serverDetails.port.toString()
            PREF_USERNAME -> serverDetails.username
            PREF_DISPLAY_ID -> serverDetails.displayId.toString()
            PREF_PICTURE_ENC -> serverDetails.pictureEncoding.name
            else -> throw UnsupportedOperationException("$key is not supported")
        }
    }

    override fun putString(key: String, value: String?) {
        when (key) {
            PREF_CONNECTION_TYPE -> serverDetails.type = enumValueOf(value ?: ConnectionType.TCP.name)
            PREF_NAME -> serverDetails.name = value
            PREF_HOST -> serverDetails.host = value
            PREF_PORT -> serverDetails.port = value?.toInt() ?: 0
            PREF_USERNAME -> serverDetails.username = value
            PREF_DISPLAY_ID -> serverDetails.displayId = value?.toInt() ?: -1
            PREF_PICTURE_ENC -> serverDetails.pictureEncoding = enumValueOf(value ?: PictureEncoding.jpeg.name)
        }
    }

    fun save() {
        Single.just(serverDetails).subscribeOn(Schedulers.io()).subscribe(dao.save())
    }
}