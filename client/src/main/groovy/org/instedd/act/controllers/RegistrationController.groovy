package org.instedd.act.controllers

import org.instedd.act.AppUI
import org.instedd.act.authentication.DeviceKeyRegistration;
import org.instedd.act.models.DataStore
import org.instedd.act.models.Device
import org.instedd.act.models.LocationTree
import org.instedd.act.ui.RegistrationForm

import com.google.common.base.Strings
import com.google.inject.Inject

class RegistrationController {

	@Inject AppUI app
	@Inject DataStore dataStore
	@Inject LocationTree locationTree
	@Inject DeviceKeyRegistration keyRegistration
	
	RegistrationForm view
	
	RegistrationController() {
		this.view = new RegistrationForm(this)
	}
	
	def buildView() {
		view.build(locationTree.rootLocations())
	}
	
	def locationCleared(int level) {
		view.removeLocationSelectorsAboveLevel(level)
	}
	
	def locationChosen(int level) {
		def path = view.locationPathUntilLevel(level)
		
		view.removeLocationSelectorsAboveLevel(level)
		
		def children = locationTree.children(path)
		if (!children.empty) {
			view.addLocationSelector(children)
		}
	}

	def submit() {
		view.clearError()
		
		def organizationName = view.organizationName
		def locationPath = view.locationPath
		def supervisorName = view.supervisorName
		def supervisorNumber = view.supervisorNumber

		boolean missingOrganization = Strings.isNullOrEmpty(organizationName)
		boolean missingLocation = locationPath.empty
		boolean missingSupervisorNumber = Strings.isNullOrEmpty(supervisorNumber)
		
		if (missingOrganization || missingLocation || missingSupervisorNumber) {
			view.displayError("Please specify your organization, location and field supervisor phone number.")
		} else {
			def device = new Device(organizationName, locationPath.last(), supervisorName, supervisorNumber)
			dataStore.storeDeviceInfo(device)
			view.dispose()
			keyRegistration.performInBackground()
			app.registrationDone()
		}
	}
		
}
