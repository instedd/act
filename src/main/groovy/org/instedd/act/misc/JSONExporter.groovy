package org.instedd.act.misc

import groovy.json.JsonBuilder
import org.instedd.act.models.Case
import org.instedd.act.models.User

class JSONExporter {
    
    def toJSON(User user) {
        def json = new JsonBuilder()
        json organization: user.organization, location: user.location
        json.toString()
    }
    
    def toJSON(Case aCase) {
        def json = new JsonBuilder()
        json name: aCase.patientName, phone: aCase.patientPhone, age: aCase.patientAge, sex: aCase.patientSex, reason: aCase.reason
        json.toString()
    }
}
