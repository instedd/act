package org.instedd.act.misc

import groovy.json.JsonBuilder;

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
		datastore.unsyncedCases().each { aCase ->
			this.exportCase(aCase)
		}
	}

		@Override
	public void exportCase(Case aCase) {
		synchronizer.queueForSync("case-${aCase.id}.json", aCase.asJson().toString())
	}
}
