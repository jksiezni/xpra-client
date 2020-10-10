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

import androidx.room.Entity
import androidx.room.PrimaryKey
import xpra.protocol.PictureEncoding
import java.io.Serializable
import java.util.*

@Entity
class ServerDetails : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var name: String? = null

    var type: ConnectionType = ConnectionType.TCP

    var host: String? = null

    var port: Int = 10000

    var displayId: Int = -1

    var username: String? = null

    var sshPrivateKeyFile: String? = null

    var pictureEncoding: PictureEncoding = PictureEncoding.jpeg

    val url: String
        get() {
            val builder = StringBuilder(type.toString().toLowerCase(Locale.getDefault()))
            builder.append("://")
            if (type == ConnectionType.SSH) {
                builder.append(username)
                builder.append('@')
            }
            builder.append(host)
            builder.append(':')
            builder.append(port)
            return builder.toString()
        }

    override fun toString(): String {
        return name!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ServerDetails
        return id == that.id && port == that.port && displayId == that.displayId &&
                name == that.name && type == that.type &&
                host == that.host &&
                username == that.username &&
                sshPrivateKeyFile == that.sshPrivateKeyFile && pictureEncoding == that.pictureEncoding
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, type, host, port, displayId, username, sshPrivateKeyFile, pictureEncoding)
    }
}