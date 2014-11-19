package org.instedd.act

import javax.swing.SwingUtilities

import org.instedd.act.authentication.DeviceKeyRegistration
import org.instedd.act.controllers.CaseListController
import org.instedd.act.controllers.RegistrationController
import org.instedd.act.models.DataStore
import org.instedd.act.sync.SynchronizationProcess

import com.google.inject.Inject

class AppUI {

	@Inject DataStore dataStore
	@Inject RegistrationController registrationController
	@Inject CaseListController casesController
	@Inject SynchronizationProcess daemon
		
	void start() {
		SwingUtilities.invokeLater {
			if (!dataStore.userInfoCompleted()) {
				registrationController.buildView()
			} else {
				casesController.buildView()
			}
		}
	}

	void registrationDone() {
		casesController.buildView()
	}

}
