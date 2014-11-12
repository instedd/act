package org.instedd.act.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migrator {
	public static final String MIGRATIONS_PATH = "db/migration";
	public static final String CHANGELOG = "db/migration/changelog";
	
	protected final static Logger logger = LoggerFactory.getLogger(Migrator.class);

	private DatabaseConnector connector;

	public Migrator(DatabaseConnector connector) {
		this.connector = connector;
	}

	public DatabaseConnector getConnector() {
		return connector;
	}

	public void setConnector(DatabaseConnector connector) {
		this.connector = connector;
	}

	public List<String> listMigrations() {
		List<String> result = new ArrayList<>();

		InputStream changelog = Migrator.class.getClassLoader().getResourceAsStream(CHANGELOG);
		if (changelog == null) {
			throw new RuntimeException("Cannot read migration changelog file");
		}
		try {
			for (String line : IOUtils.readLines(changelog)) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				result.add(line);
			}

		} catch (IOException e) {
			throw new RuntimeException("Cannot read migration changelog file", e);
		} finally {
			try {
				changelog.close();
			} catch (Exception e) {
			}
		}

		if (result.isEmpty()) {
			logger.warn("No migrations found");
		}

		return result;
	}

	public String loadMigration(String filename) {
		String path = MIGRATIONS_PATH + "/" + filename;
		InputStream stream = Migrator.class.getClassLoader().getResourceAsStream(path);
		try {
			return IOUtils.toString(stream);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load migration file "
					+ filename, e);
		} finally {
			try {
				stream.close();
			} catch (Exception e) {
			}
		}
	}

	private boolean checkMigrationsTable(Connection connection) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("select * from sqlite_master where type='table' and name='migrations'");
		boolean result = rs.next();
		rs.close();
		return result;
	}

	private void createMigrationsTable(Connection connection) throws SQLException {
		logger.info("Creating migrations table");

		Statement stmt = connection.createStatement();
		stmt.execute("create table migrations (name varchar(255), run_at timestamp);");
	}

	private List<String> listExecutedMigrations(Connection connection) throws SQLException {
		List<String> result = new ArrayList<>();
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("select name from migrations");
		while (rs.next()) {
			result.add(rs.getString(1));
		}
		rs.close();
		return result;
	}

	private void executeMigration(Connection connection, String name) throws SQLException {
		logger.info("Executing migration " + name);
		String migration = loadMigration(name);
		Statement stmt = connection.createStatement();

		// FIXME: split the migration in statements properly
		for (String s : migration.split(";")) {
			s = s.trim();
			if (s.isEmpty())
				continue;
			stmt.addBatch(s);
		}
		stmt.executeBatch();

		logger.debug("Inserting migration entry");
		PreparedStatement ps = connection.prepareStatement("insert into migrations (name, run_at) values (?,datetime('now'))");
		ps.setString(1, name);
		ps.execute();
	}

	public void migrate() {
		if (getConnector() == null) {
			throw new IllegalStateException("No database connector");
		}

		Connection connection = getConnector().getConnection();
		try {
			if (!checkMigrationsTable(connection)) {
				createMigrationsTable(connection);
			}

			List<String> availableMigrations = listMigrations();
			List<String> executedMigrations = listExecutedMigrations(connection);

			for (String migration : availableMigrations) {
				if (executedMigrations.contains(migration)) {
					continue;
				}

				executeMigration(connection, migration);
			}

			if (!connection.getAutoCommit()) {
				connection.commit();
			}

			logger.info("Database migrated");

		} catch (SQLException e) {
			throw new RuntimeException("Error migrating database", e);
		}
	}

	public static void main(String[] args) throws SQLException {
		if (args.length == 0) {
			System.err.println("Usage: migrator <dbfile>");
			System.exit(1);
		}

		String dbfile = args[0];
		DatabaseConnector connector = new SqliteConnector(dbfile);
		Migrator migrator = new Migrator(connector);
		migrator.migrate();
		connector.getConnection().close();
	}
}
