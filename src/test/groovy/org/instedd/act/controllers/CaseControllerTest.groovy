package org.instedd.act.controllers

import groovy.json.JsonSlurper
import org.instedd.act.misc.JSONExporter
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
    
    def "exports new cases"() {
        setup:
        def parser = new JsonSlurper()
        def targetDirectory = new File("tmp/dir")
        targetDirectory.mkdirs()
        def initialFilesCount = targetDirectory.listFiles().size()
        controller.exporter = new JSONExporter(targetDirectory: targetDirectory)
        when:
        controller.create([patientName: 'John Doe', patientPhone: '55513562', patientAge: 42, patientSex: 'Male', reason: 'Has fever'])
        def cases = controller.index()
        then:
        targetDirectory.listFiles().size() == initialFilesCount + 1
        cases.size() == 1
        def aCase = cases[0]
        def outputFile = new File(targetDirectory, aCase.hashCode() + ".json")
        outputFile.deleteOnExit()
        def parsedCase = parser.parseText(outputFile.text)
        
        aCase.patientName == parsedCase.name
        aCase.patientPhone == parsedCase.phone
        aCase.patientAge == parsedCase.age
        aCase.patientSex == parsedCase.sex
        aCase.reason == parsedCase.reason
    }
}
