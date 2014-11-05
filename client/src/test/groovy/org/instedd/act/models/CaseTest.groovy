package org.instedd.act.models

import spock.lang.*

class CaseTest extends Specification{
    def "creates users"() {
        when:
        def aCase = new Case(patientName: 'John Doe', patientPhone: '55513562', patientAge: 42, patientSex: 'Male', reason: 'Has fever')
        then:
        aCase.patientAge == 42
        aCase.patientName == 'John Doe'
        aCase.patientPhone == '55513562'
        aCase.patientSex == 'Male'
        aCase.reason == 'Has fever'
    }
}
