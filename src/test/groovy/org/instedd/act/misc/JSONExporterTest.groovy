package org.instedd.act.misc

import groovy.json.JsonSlurper
import org.instedd.act.models.Case
import org.instedd.act.models.User
import spock.lang.*

class JSONExporterTest extends Specification{

    JSONExporter exporter = new JSONExporter()
    JsonSlurper parser = new JsonSlurper()

    def "serializes users"() {
        setup:
        def user = new User('msf-spain', 'Sierra Leone/Northern Province/Kambia District/Kambia')
        when:
        def jsonUser = exporter.toJSON(user)
        def parsedUser = parser.parseText(jsonUser)
        then:
        parsedUser == [location: [
                "Sierra Leone",
                "Northern Province",
                "Kambia District",
                "Kambia"
            ], organization: 'msf-spain']
    }

    def "serializes cases"() {
        setup:
        def aCase = new Case(patientName: 'John Doe', patientPhone: '55513562', patientAge: 42, patientSex: 'Male', reason: 'Has fever')
        when:
        def jsonCase = exporter.toJSON(aCase)
        def parsedCase = parser.parseText(jsonCase)
        then:
        parsedCase == [name:"John Doe",phone:"55513562",age:42,sex:"Male",reason:"Has fever"]
    }
}
