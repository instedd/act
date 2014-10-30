package org.instedd.act.controllers

import org.instedd.act.models.DataStore;
import org.instedd.act.models.LocationTree;
import org.instedd.act.models.User
import org.instedd.act.ui.RegistrationForm

import com.google.common.base.Strings;
import com.google.inject.Inject

class RegistrationController {

	@Inject DataStore dataStore
	@Inject LocationTree locationTree
	
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

		boolean missingOrganization = Strings.isNullOrEmpty(organizationName)
		boolean missingLocation = locationPath.empty
		
		if (missingOrganization && missingLocation) {
			view.displayError("Please specify your organization and location.")
		} else if (missingOrganization) {
			view.displayError("Please specify your organization.")
		} else if (missingLocation){
			view.displayError("Please specify your location.")
		} else {
			def user = new User(organizationName, locationPath)
			dataStore.register(user)
		}
	}
		
}
