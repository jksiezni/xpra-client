package com.github.jksiezni.xpra.db.entities;

import java.io.Serializable;
import java.util.Locale;

import xpra.protocol.PictureEncoding;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "connections")
public class Connection implements Serializable {
	private static final long serialVersionUID = 1L;

	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(canBeNull = false)
	public String name;

	@DatabaseField(canBeNull = false)
	public ConnectionType type = ConnectionType.TCP;

	@DatabaseField(canBeNull = false)
	public String host;

	@DatabaseField
	public int port = 10000;
	
	@DatabaseField(columnName="display_id")
	public int displayId = -1;

	@DatabaseField
	public String username;
	
	@DatabaseField(columnName="private_key_file")
	public String sshPrivateKeyFile;
	
	@DatabaseField(columnName="picture_encoding", canBeNull = false)
	public PictureEncoding pictureEncoding = PictureEncoding.png;
	
	public int getId() {
		return id;
	}
	
	public String getURL() {
		StringBuilder builder = new StringBuilder(type.toString().toLowerCase(Locale.getDefault()));
		builder.append("://");
		if(type == ConnectionType.SSH) {
			builder.append(username);
			builder.append('@');
		}
		builder.append(host);
		builder.append(':');
		builder.append(port);
		return builder.toString();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return id > 0 ? id : super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o instanceof Connection) {
			Connection c = (Connection) o;
			return c.id > 0 && c.id == id;
		}
		return false;
	}
}
