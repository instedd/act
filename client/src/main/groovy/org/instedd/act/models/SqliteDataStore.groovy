package org.instedd.act.models

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql

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
	public boolean userInfoCompleted() {
		sql.firstRow("select organization from device_info") != null
	}

	@Override
	public synchronized void storeDeviceInfo(Device device) {
		sql.execute("insert into device_info(organization, location, supervisor_name, supervisor_number) values (${device.organization}, ${device.location.id}, ${device.supervisorName}, ${device.supervisorNumber})")
	}

	@Override
	public synchronized void register(Case aCase) {
		sql.dataSet("cases").add([guid: aCase.id, name: aCase.name, phone: aCase.phone, age: aCase.age, gender: aCase.gender, dialect: aCase.preferredDialect, reasons: new JsonBuilder(aCase.reasons), notes: aCase.notes])
		exporter.exportCase(aCase)
	}

	@Override
	public List<Case> listCases() {
		sql.rows("select * from cases").collect { row ->
			new Case([id: row.guid, name: row.name, phone: row.phone, age: row.age, gender: row.gender, preferredDialect: row.dialect, reasons: new JsonSlurper().parseText(row.reasons), notes: row.notes, sick: row.sick, synced: row.synced, updated: row.updated])
		}
	}

	@Override
	public boolean isDeviceRegistered() {
		def device = sql.firstRow("select registered from device_info limit 1")
		device && device.registered
	}

	@Override
	public Map<String, Object> deviceInfo() {
		def device = sql.firstRow("select * from device_info limit 1")
		[organization: device.organization, location: device.location, supervisorNumber: device.supervisor_number, supervisorName: device.supervisor_name]
	}

	@Override
	public void markDeviceRegistered() {
		sql.execute("update device_info set registered = ${true}")
	}
	
	@Override
	public List<Case> unsyncedCases() {
		sql.rows("select * from cases where synced = ${false}").collect { row ->
			new Case([id: row.guid, name: row.name, phone: row.phone, age: row.age, gender: row.gender, preferredDialect: row.dialect, reasons: new JsonSlurper().parseText(row.reasons), notes: row.notes, sick: row.sick, synced: row.synced, updated: row.updated])
		}
	}

	@Override
	public void registerCaseSynced(String guid) {
		sql.execute("update cases set synced = ${true} where guid = ${guid}")
	}

	@Override
	public void updateSickCase(String guid, Boolean isSick) {
		sql.execute("update cases set sick = ${isSick}, updated = ${true} where guid = ${guid}")
	}

	@Override
	public void markCaseAsSeen(Case aCase) {
		sql.execute("update cases set updated = ${false} where guid = ${aCase.id}")
	}

	@Override
	public String[] availableDialects() {
		sql.rows("select name from dialects").collect { row -> row.name } as String[]
	}
}
