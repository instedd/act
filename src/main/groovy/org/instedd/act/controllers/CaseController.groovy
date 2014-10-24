package org.instedd.act.controllers

import org.instedd.act.models.Case

class CaseController {

    def cases = []
    def exporter

    def index() {
        cases
    }

    def create(params) {
        def newCase = new Case(params)
        cases.add newCase
        exporter?.write(newCase)
    }
}
