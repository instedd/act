package org.instedd.act.models

import java.util.List;
import java.util.Map;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper
import groovy.sql.Sql;

import org.instedd.act.db.DatabaseConnector
import org.instedd.act.misc.DocumentExporter

import com.google.inject.Inject

class SqliteDataStore implements DataStore {
	DatabaseConnector connector
	@Inject DocumentExporter exporter
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
		exporter.exportDeviceInfo()
	}

	@Override
	public synchronized void register(Case aCase) {
		sql.dataSet("cases").add([guid: aCase.id, name: aCase.name, phone: aCase.phone, age: aCase.age, gender: aCase.gender, dialect: aCase.preferredDialect, reasons: new JsonBuilder(aCase.reasons), notes: aCase.notes])
		exporter.exportCase(aCase)
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
		sql.firstRow("select guid from device_info limit 1").guid
	}

	@Override
	public List<Case> listCases() {
		sql.rows("select * from cases").collect { row ->
			new Case([id: row.guid, name: row.name, phone: row.phone, age: row.age, gender: row.gender, preferredDialect: row.dialect, reasons: new JsonSlurper().parseText(row.reasons), notes: row.notes, sick: row.sick, synced: row.synced])
		}
	}

	@Override
	public boolean needsSyncDeviceInfo() {
		def device = sql.firstRow("select location, registered from device_info limit 1")
		device.location && !device.registered
	}

	@Override
	public Map<String, Object> deviceInfo() {
		def device = sql.firstRow("select * from device_info limit 1")
		[id: device.guid, organization: device.organization, location: device.location, supervisorNumber: device.supervisor_number, supervisorName: device.supervisor_name]
	}

	@Override
	public void registerDeviceInfoSynced() {
		sql.execute("update device_info set registered = ${true}")
	}
	
	@Override
	public List<Case> unsyncedCases() {
		sql.rows("select * from cases where synced = ${false}").collect { row ->
			new Case([id: row.guid, name: row.name, phone: row.phone, age: row.age, gender: row.gender, preferredDialect: row.dialect, reasons: new JsonSlurper().parseText(row.reasons), notes: row.notes, sick: row.sick, synced: row.synced])
		}
	}

	@Override
	public void registerCaseSynced(String guid) {
		sql.execute("update cases set synced = ${true} where guid = ${guid}")
	}

	@Override
	public void updateSickCase(String guid, Boolean isSick) {
		sql.execute("update cases set sick = ${isSick} where guid = ${guid}")
	}
}
