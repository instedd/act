package org.instedd.act.models

import spock.lang.*

class CaseTest extends Specification{
    def "creates users"() {
        when:
        def aCase = new Case(name: 'John Doe', phone: '55513562', age: 42, gender: 'Male', reasons: ['Has fever'])
        then:
        aCase.age == 42
        aCase.name == 'John Doe'
        aCase.phone == '55513562'
        aCase.gender == 'Male'
        aCase.reasons == ['Has fever']
    }
}
