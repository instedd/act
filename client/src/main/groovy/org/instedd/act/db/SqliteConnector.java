package org.instedd.act.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.instedd.act.Settings;

import com.google.inject.Inject;

public class SqliteConnector implements DatabaseConnector {
	private Connection connection;

	@Inject
	public SqliteConnector(Settings settings) {
		this(settings.get("database.path"));
	}

	public SqliteConnector(String databaseFilename) {
		File dbFile = new File(databaseFilename);
		dbFile.getParentFile().mkdirs();

		try {
			Class.forName("org.sqlite.JDBC");

			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFilename);

			connection.setAutoCommit(true);
		} catch (Exception ex) {
			throw new RuntimeException("Sqlite connection failed, check the database: "
					+ databaseFilename + " exists", ex);
		}
	}

	@Override
	public Connection getConnection() {
		return connection;
	}
}