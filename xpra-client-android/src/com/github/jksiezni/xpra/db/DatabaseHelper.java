package com.github.jksiezni.xpra.db;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.jksiezni.xpra.db.entities.Connection;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

	private static final String DATABASE_NAME = "xpra_client.db";

	// any time you make changes to your database objects, you may have to
	// increase the database version
	private static final int DATABASE_VERSION = 1;

	private RuntimeExceptionDao<Connection, Integer> connectionDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
		try {
			logger.debug("onCreate()");
			TableUtils.createTable(connectionSource, Connection.class);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		logger.debug("onUpgrade(%d -> %d)", oldVersion, newVersion);

	}

	public synchronized RuntimeExceptionDao<Connection, Integer> getConnectionDao() {
		if (connectionDao == null) try {
			Dao<Connection, Integer> dao = getDao(Connection.class);
			connectionDao = new RuntimeExceptionDao<Connection, Integer>(dao);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return connectionDao;
	}

}
