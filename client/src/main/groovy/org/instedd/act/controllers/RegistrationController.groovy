package org.instedd.act.controllers

import javax.swing.SwingWorker

import org.instedd.act.AppUI
import org.instedd.act.models.DataStore
import org.instedd.act.models.Device
import org.instedd.act.models.Location
import org.instedd.act.models.LocationIndex;
import org.instedd.act.ui.LocationSelectionController
import org.instedd.act.ui.RegistrationForm

import com.google.common.base.Strings
import com.google.common.base.Supplier
import com.google.inject.Inject
import com.google.inject.name.Named

class RegistrationController {

	@Inject AppUI app
	@Inject DataStore dataStore
	
	def locationIndex
	
	LocationSelectionController locationController
	RegistrationForm view
	
	@Inject
	RegistrationController() {
		this.view = new RegistrationForm(this)
	}
	
	def buildView() {
		locationIndex = LocationIndex.build()
		view.build()
		locationController = new LocationSelectionController(this, view)
	}
	
	def submit() {
		view.clearError()
		
		def organizationName = view.organizationName
		def location = locationController.selectedLocation
		def supervisorName = view.supervisorName
		def supervisorNumber = view.supervisorNumber

		boolean missingOrganization = Strings.isNullOrEmpty(organizationName)
		boolean missingLocation = location == null
		boolean missingSupervisorNumber = Strings.isNullOrEmpty(supervisorNumber)
		
		if (missingOrganization || missingLocation || missingSupervisorNumber) {
			view.displayError("Please specify your organization, location and field supervisor phone number.")
		} else {
			def device = new Device(organizationName, location, supervisorName, supervisorNumber)
			dataStore.storeDeviceInfo(device)
			view.dispose()
			app.registrationDone()
		}
	}

	def locationInputChanged(String query) {
		if (query.length() >= 3) {
			view.showLocationOptions()
			new SwingWorker<Collection<Location>, Void>() {
				
				@Override
				Collection<Location> doInBackground() throws Exception {
					locationIndex.matches(query)
				}
				
				@Override
				void done() {
					locationController.clearSelection()
					view.clearLocationOptions()
					view.displayLocationOptions(get())
				}
				
			}.execute();
		} else {
			locationController.clearSelection()
			view.clearLocationOptions()
			view.hideLocationOptions()
			return
		}
	}
	
}
