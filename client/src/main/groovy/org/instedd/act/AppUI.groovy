package org.instedd.act

import javax.swing.SwingUtilities

import org.instedd.act.controllers.CaseListController
import org.instedd.act.controllers.NewCaseController
import org.instedd.act.controllers.RegistrationController
import org.instedd.act.models.DataStore
import org.instedd.act.sync.Daemon

import com.google.inject.Inject

class AppUI {

	@Inject DataStore dataStore
	@Inject RegistrationController registrationController
	@Inject CaseListController casesController
	@Inject Daemon daemon
	
	void start() {
		SwingUtilities.invokeLater {
			if (!dataStore.isDeviceRegistered()) {
				registrationController.buildView()
			} else {
				casesController.buildView()
			}
		}
	}

	void registrationDone() {
		daemon.requestSync()
		casesController.buildView()
	}

}
