package org.instedd.act

import java.awt.BorderLayout;

import com.google.inject.Inject;
import com.google.inject.Injector

import groovy.swing.SwingBuilder

import javax.swing.SwingUtilities
import javax.swing.WindowConstants;

import org.instedd.act.controllers.RegistrationController
import org.instedd.act.models.DataStore;
import org.instedd.act.ui.NewCaseForm
import org.instedd.act.ui.RegistrationForm

class AppUI {

	@Inject DataStore dataStore
	@Inject RegistrationController registrationController
	
	void start() {
		SwingUtilities.invokeLater {
			if (!dataStore.isUserRegistered()) {
				registrationController.buildView()
			} else {
				registrationDone();
			}
		}
	}

	void registrationDone() {
		new NewCaseForm().visible = true
	}

}
