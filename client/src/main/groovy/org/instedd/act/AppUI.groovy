package org.instedd.act

import javax.swing.SwingUtilities

import org.instedd.act.controllers.CasesController
import org.instedd.act.controllers.RegistrationController
import org.instedd.act.models.DataStore
import org.instedd.act.sync.Daemon
import org.instedd.act.ui.NewCaseForm

import com.google.inject.Inject

class AppUI {

	@Inject DataStore dataStore
	@Inject RegistrationController registrationController
    @Inject CasesController casesController
	@Inject Daemon daemon
	
	void start() {
		SwingUtilities.invokeLater {
			if (!dataStore.isDeviceRegistered()) {
				registrationController.buildView()
			} else {
				registrationDone();
			}
		}
	}

	void registrationDone() {
		daemon.requestSync()
		casesController.buildView()
	}

}
