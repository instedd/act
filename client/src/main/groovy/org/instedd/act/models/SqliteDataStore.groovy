package org.instedd.act.models

import java.util.List;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper
import groovy.sql.Sql;

import org.instedd.act.db.DatabaseConnector

import com.google.inject.Inject

class SqliteDataStore implements DataStore {
	DatabaseConnector connector
	Sql sql
	
	@Inject
	SqliteDataStore(DatabaseConnector connector) {
		this.connector = connector
		sql = new Sql(connector.connection)
	}

	@Override
	public boolean isDeviceRegistered() {
		def info = sql.firstRow("select organization, location, supervisor_number from device_info limit 1")
		info.organization && info.location && info.supervisor_number
	}

	@Override
	public synchronized void register(Device device) {
		sql.execute("update device_info set organization = ${device.organization}, location = ${device.location.id}, supervisor_name = ${device.supervisorName}, supervisor_number = ${device.supervisorNumber}")
	}

	@Override
	public synchronized void register(Case aCase) {
		sql.dataSet("cases").add([guid: aCase.id, name: aCase.name, phone: aCase.phone, age: aCase.age, gender: aCase.gender, dialect: aCase.preferredDialect, reasons: new JsonBuilder(aCase.reasons), notes: aCase.notes])
	}

	@Override
	public boolean isDeviceIdentifierGenerated() {
		sql.firstRow("select guid from device_info limit 1") != null
	}

	@Override
	public synchronized void saveDeviceIdentifier(String identifier) {
		sql.execute("insert into device_info (guid) values (${identifier})")
	}

	@Override
	public String getDeviceIdentifier() {
		sql.execute("select guid from device_info limit 1") { isResultSet, resultSet ->
			resultSet[0]
		}
	}

	@Override
	public List<Case> listCases() {
		sql.rows("select * from cases").collect { row ->
			new Case([id: row.guid, name: row.name, phone: row.phone, age: row.age, gender: row.gender, preferredDialect: row.dialect, reasons: new JsonSlurper().parseText(row.reasons), notes: row.notes, sick: row.sick])
		}
	}
}
