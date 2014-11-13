package org.instedd.act.misc

import org.instedd.act.models.Case;
import org.instedd.act.models.DataStore
import org.instedd.act.models.Device;
import org.instedd.act.sync.DocumentSynchronizer

import com.google.inject.Inject

class SqliteToJsonExporter implements DocumentExporter {

	@Inject DataStore datastore
	@Inject DocumentSynchronizer synchronizer

	@Override
	public void exportDocuments() {
		if(datastore.needsSyncDeviceInfo()) {
			this.exportDeviceInfo()
		}
	}
	@Override
	public void exportDeviceInfo() {
		synchronizer.queueForSync("device.json", datastore.deviceInfo().toString())
	}
	@Override
	public void exportCase(Case aCase) {
		synchronizer.queueForSync("case-${aCase.id}.json", aCase.asJson().toString())
	}
}
