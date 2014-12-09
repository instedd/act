package org.instedd.act.controllers

import org.instedd.act.events.CaseUpdatedEvent;
import org.instedd.act.models.Case
import org.instedd.act.models.DataStore
import org.instedd.act.sync.SynchronizationProcess
import org.instedd.act.ui.NewCaseForm
import org.instedd.act.ui.caselist.CaseList

import com.google.common.base.Strings
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject

class CaseListController {

	@Inject DataStore dataStore
	@Inject SynchronizationProcess daemon

	@Inject NewCaseController newCaseController
	
	CaseList caseList = new CaseList(this)
	NewCaseForm newCaseForm
	
	@Inject
	CaseListController(EventBus eventBus) {
		eventBus.register(this)
	}

	def buildView() {
		caseList.build(dataStore.listCases());
	}	

	void newCaseButtonPressed() {
		newCaseForm = new NewCaseForm(caseList, this)
		newCaseForm.build()
	}

	void caseCreated(Case newCase) {
		dataStore.register(newCase)
		daemon.requestSync()
		
		caseList.updateCases(dataStore.listCases())
	}
	
	def newCaseSubmitted() {
		newCaseForm.clearMessage()

		if (missingRequiredFields()) {
			newCaseForm.displayMessage("Please specify the contact's name, phone and preferred dialect")
		} else if(!this.validAge()) {
			newCaseForm.displayMessage("Please specify a valid age")
		} else if (!this.validPhone()){
			newCaseForm.displayMessage("Please specify a valid full phone number")
		} else {
			caseCreated(new Case([
				name: newCaseForm.patientName,
				phone: newCaseForm.phone,
				age: newCaseForm.age.toInteger(),
				gender: newCaseForm.gender,
				preferredDialect: newCaseForm.dialect,
				reasons: newCaseForm.reasons,
				notes: newCaseForm.notes
			]))
			newCaseForm.dispose()
		}
	}

	// TODO: Move all these validations to the model.
	
	def missingRequiredFields() {
		def name = newCaseForm.patientName
		def phone = newCaseForm.phone
		def dialect = newCaseForm.dialect

		boolean missingName = Strings.isNullOrEmpty(name)
		boolean missingPhone = Strings.isNullOrEmpty(phone)
		boolean missingDialect = Strings.isNullOrEmpty(dialect)
		
		missingPhone || missingName || missingDialect
	}
	
	def validAge() {
		try {
			Integer.parseInt(newCaseForm.age)
			return true
		} catch (NumberFormatException e) {
			return false
		}
	}
	
	def validPhone() {
		// FIXME: implement validations
		true
	}
	
	def markAsSeen(Case aCase) {
		dataStore.markCaseAsSeen(aCase)
		aCase.updated = false
	}
	
	void markCasesAsSeen(cases) {
		def changed = false
		cases.each { selectedCase ->
			if(selectedCase.updated) { 
				this.markAsSeen(selectedCase)
				changed = true
			}
		}
		if (changed) {
			caseList.updateCases(dataStore.listCases())
		}
	}
	
	@Subscribe
	void onCaseUpdated(CaseUpdatedEvent event) {
		caseList.updateCases(dataStore.listCases())
	}
	
	String[] availableDialects() {
		dataStore.availableDialects()
	}
	
	String[] contactReasons() {
		dataStore.contactReasons()
	}
	
	void onlyShowUnseen(Boolean onlyUnseen) {
		println "Setting show unseen: ${onlyUnseen}"
	}
	
}
