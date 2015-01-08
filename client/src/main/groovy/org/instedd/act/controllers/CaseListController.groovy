package org.instedd.act.controllers

import org.instedd.act.events.CaseUpdatedEvent
import org.instedd.act.events.CasesFileUpdatedEvent;
import org.instedd.act.models.Case
import org.instedd.act.models.CasesFile;
import org.instedd.act.models.DataStore
import org.instedd.act.sync.DocumentSynchronizer
import org.instedd.act.sync.SynchronizationProcess
import org.instedd.act.ui.NewCaseForm
import org.instedd.act.ui.caselist.CaseList

import com.google.common.base.Strings
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.inject.Inject

class CaseListController {

	@Inject DataStore dataStore
	@Inject SynchronizationProcess daemon

	@Inject NewCaseController newCaseController
	@Inject DocumentSynchronizer synchronizer
	
	CaseList caseList = new CaseList(this)
	NewCaseForm newCaseForm
	
	Boolean onlyShowUnread = false
	
	@Inject
	CaseListController(EventBus eventBus) {
		eventBus.register(this)
	}

	def buildView() {
		caseList.build(this.listCases(), this.listFiles());
	}	

	void newCaseButtonPressed() {
		newCaseForm = new NewCaseForm(caseList, this)
		newCaseForm.build()
	}

	void caseCreated(Case newCase) {
		dataStore.register(newCase)
		daemon.requestSync()
		
		caseList.updateCases(this.listCases())
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
	
	def markAsRead(Case aCase) {
		dataStore.markCaseAsRead(aCase)
		aCase.updated = false
	}
	
	void markCasesAsRead(cases) {
		def changed = false
		cases.each { selectedCase ->
			if(selectedCase.updated) { 
				this.markAsRead(selectedCase)
				changed = true
			}
		}
		if (changed) {
			caseList.updateCases(this.listCases())
		}
	}
	
	@Subscribe
	void onCaseUpdated(CaseUpdatedEvent event) {
		caseList.updateCases(this.listCases())
	}
	
	String[] availableDialects() {
		dataStore.availableDialects()
	}
	
	String[] contactReasons() {
		dataStore.contactReasons()
	}
	
	void setOnlyShowUnread(Boolean onlyShowUnread) {
		this.onlyShowUnread = onlyShowUnread
		reloadTable()
	}
	
	List<Case> listCases() {
		if(onlyShowUnread) {
			dataStore.unreadCases()
		} else {
			dataStore.listCases()
		}
	}
	
	List<CasesFile> listFiles() {
		dataStore.listCasesFiles()
	}
	
	void reloadTable() {
		caseList.updateCases(this.listCases())
		caseList.updateCasesCountLabel()
		caseList.updateMarkAsReadText()
	}
	
	void syncCasesFile(File document) {
		String documentName = document.name
		String documentPath = document.absolutePath
		String guid = UUID.randomUUID().toString()
		CasesFile file = new CasesFile([name: documentName, path: documentPath, guid: guid])
		dataStore.register(file)
		synchronizer.queueForSync(guid, document)
		caseList.updateFiles(this.listFiles())
	}
	
	@Subscribe
	void onCasesFileUpdated(CasesFileUpdatedEvent event) {
		caseList.updateFiles(this.listFiles())
	}
	
}
