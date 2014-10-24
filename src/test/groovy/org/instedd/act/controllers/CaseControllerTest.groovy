package org.instedd.act.controllers

import spock.lang.*

class CaseControllerTest extends Specification{
    
    def controller = new CaseController()
    
    def "controller indexes no cases"() {
        when:
        def cases = controller.index()
        then:
        cases.isEmpty()
    }
    
    def "controller indexes cases"() {
        setup:
        controller.create([patientName: 'John Doe', patientPhone: '55513562', patientAge: 42, patientSex: 'Male', reason: 'Has fever'])
        when:
        def cases = controller.index()
        then:
        cases.size() == 1
        cases[0].patientName == 'John Doe'
        cases[0].patientPhone == '55513562'
        cases[0].patientAge == 42
        cases[0].patientSex == 'Male'
        cases[0].reason == 'Has fever'
    }
}
