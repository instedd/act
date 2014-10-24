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
    
    def write(object) {
        this.fileFor(object).withWriter { out ->
            out.writeLine(this.toJSON(object))
        }
    }
    
    // FIXME: refactor
    def fileFor(object) {
        new File(targetDirectory, object.hashCode() + ".json")
    }
    
    File targetDirectory
    
    def JSONExporter() {
        targetDirectory = new File("json/export")
        targetDirectory.mkdirs()
    }
}
