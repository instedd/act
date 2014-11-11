package org.instedd.act.models

import org.instedd.act.db.DatabaseConnector

import com.google.inject.Inject

class SqliteDataStore implements DataStore {
	@Inject DatabaseConnector connector

	@Override
	public boolean isDeviceRegistered() {
		true
	}

	@Override
	public synchronized void register(Device device) {
	}

	@Override
	public synchronized void register(Case aCase) {
	}

	@Override
	public boolean isDeviceIdentifierGenerated() {
		true
	}

	@Override
	public synchronized void saveDeviceIdentifier(String identifier) {
	}

	@Override
	public String getDeviceIdentifier() {
		"device_id"
	}
}
