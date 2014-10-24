package org.instedd.act.controllers

import org.instedd.act.models.Case

class CaseController {
    
    def cases = []
    
    def index() {
        cases
    }
    
    def create(params) {
        def newCase = new Case(params)
        cases.add newCase
    }
}
