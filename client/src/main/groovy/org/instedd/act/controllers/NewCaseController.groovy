package org.instedd.act.controllers

import javax.swing.JFrame

import org.instedd.act.AppUI
import org.instedd.act.models.Case
import org.instedd.act.models.DataStore
import org.instedd.act.sync.Daemon
import org.instedd.act.sync.DocumentSynchronizer
import org.instedd.act.ui.NewCaseForm

import com.google.common.base.Strings
import com.google.inject.Inject

class NewCaseController {

	@Inject AppUI app
	@Inject DataStore dataStore

	CaseListController parentController
	NewCaseForm view

	def buildView(CaseListController parentController) {
		this.parentController = parentController
		this.view = new NewCaseForm(parentController.caseList, this)
		view.build(this.contactReasons(), this.availableDialects())
	}

}
