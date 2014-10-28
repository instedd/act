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
        def user = new User('msf-spain', 'Sierra Leone/Northern Province/Kambia District/Kambia'.split("/"))
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
    
    def "writes users"() {
        setup:
        def user = new User('msf-spain', 'Sierra Leone/Northern Province/Kambia District/Kambia'.split("/"))
        File outputFile = new File(exporter.targetDirectory, user.hashCode() + ".json")
        when:
        exporter.write(user)
        then:
        outputFile.exists()
        def parsedUser = parser.parseText outputFile.text
        parsedUser == [location: [
                "Sierra Leone",
                "Northern Province",
                "Kambia District",
                "Kambia"
            ], organization: 'msf-spain']
        cleanup:
        outputFile.delete() 
    }
    
    def "writes cases"() {
        setup:
        def aCase = new Case(patientName: 'John Doe', patientPhone: '55513562', patientAge: 42, patientSex: 'Male', reason: 'Has fever')
        File outputFile = new File(exporter.targetDirectory, aCase.hashCode() + ".json")
        when:
        exporter.write(aCase)
        then:
        outputFile.exists()
        def parsedCase = parser.parseText outputFile.text
        parsedCase == [name:"John Doe",phone:"55513562",age:42,sex:"Male",reason:"Has fever"]
        cleanup:
        outputFile.delete()
    }
    
    def "writes in custom paths"() {
        setup:
        def user = new User('msf-spain', 'Sierra Leone/Northern Province/Kambia District/Kambia'.split("/"))
        def targetDirectory = new File("tmp/dir")
        targetDirectory.mkdirs()
        def targetFile = new File(targetDirectory, user.hashCode() + ".json")
        def exporter = new JSONExporter(targetDirectory: targetDirectory)
        when:
        exporter.write(user)
        then:
        targetFile.exists()
        def parsedUser = parser.parseText targetFile.text
        parsedUser == [location: [
                "Sierra Leone",
                "Northern Province",
                "Kambia District",
                "Kambia"
            ], organization: 'msf-spain']
        cleanup:
        targetFile.delete()
    }
}
