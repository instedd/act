package org.instedd.act.misc

import org.instedd.act.models.DataStore
import org.instedd.act.sync.DocumentSynchronizer

import com.google.inject.Inject

class SqliteToJsonExporter implements DocumentExporter {

	@Inject DataStore datastore
	@Inject DocumentSynchronizer synchronizer

	@Override
	public void exportDocuments() {
		if(datastore.needsSyncDeviceInfo()) {
			synchronizer.queueForSync("device.json", datastore.deviceInfo().toString())
		}
	}
}
