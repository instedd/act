package org.instedd.act.controllers

import org.instedd.act.AppUI
import org.instedd.act.models.Case
import org.instedd.act.models.DataStore
import org.instedd.act.models.LocationTree
import org.instedd.act.models.User
import org.instedd.act.sync.DocumentSynchronizer;
import org.instedd.act.ui.NewCaseForm
import org.instedd.act.ui.RegistrationForm

import com.google.common.base.Strings
import com.google.inject.Inject

class CasesController {

	@Inject AppUI app
	@Inject DataStore dataStore
	@Inject DocumentSynchronizer synchronizer

	NewCaseForm view

	CasesController() {
		this.view = new NewCaseForm(this)
	}

	def buildView() {
		view.build(this.contactReasons(), this.availableDialects())
	}

	def contactReasons() {
		[
			"Fever",
			"Severe headache",
			"Muscle pain",
			"Weakness",
			"Fatigue",
			"Diarrhea",
			"Vomiting",
			"Abdominal (stomach) pain",
			"Unexplained hemorrhage (bleeding or bruising)"] as String[]
	}

	def availableDialects() {
		[
			"Afrikaans",
			"English",
			"French",
			"Kiswahili"] as String[]
	}

	def submit() {
		view.clearMessage()

		def name = view.patientName
		def phone = view.phone
		def dialect = view.dialect
		def age

		try {
			age = view.age.toInteger()
		} catch (NumberFormatException exception) {
			age = null
		}

		boolean missingName = Strings.isNullOrEmpty(name)
		boolean missingPhone = Strings.isNullOrEmpty(phone)
		boolean missingDialect = Strings.isNullOrEmpty(dialect)

		if (missingPhone || missingName || missingDialect) {
			view.displayMessage("Please specify the contact's name, phone and preferred dialect")
		} else if (!this.validPhone(phone)){
			view.displayMessage("Please specify a valid full phone number")
		} else {
			def newCase = new Case([name: name, phone: phone, age: age, gender: view.gender, preferredDialect: dialect, reasons: view.reasons, notes: view.notes])
			dataStore.register(newCase)
			synchronizer.queueForSync("case-${newCase.id}.json", newCase.asJson().toString())
			view.displayMessage("Case successfully registered")
			view.clearValues()
		}
	}

	def validPhone(phoneNumber) {
		// FIXME: implement validations
		true
	}
}
