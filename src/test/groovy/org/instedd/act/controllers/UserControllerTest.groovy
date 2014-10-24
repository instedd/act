package org.instedd.act.controllers

import groovy.json.JsonSlurper
import org.instedd.act.misc.JSONExporter
import spock.lang.*

class UserControllerTest extends Specification{
    
    def controller = new UserController()
    
    def "controller indexes no users"() {
        when:
        def users = controller.index()
        then:
        users.isEmpty()
    }
    
    def "controller indexes users"() {
        setup:
        controller.create(organization: 'msf-spain', location: 'Sierra Leone/Northern Province/Kambia District/Kambia')
        when:
        def users = controller.index()
        then:
        users.size() == 1
        users[0].organization == 'msf-spain'
        users[0].location == ['Sierra Leone', 'Northern Province', 'Kambia District', 'Kambia']
    }
    
    def "exports new users"() {
        setup:
        def parser = new JsonSlurper()
        def targetDirectory = new File("tmp/dir")
        targetDirectory.mkdirs()
        def initialFilesCount = targetDirectory.listFiles().size()
        controller.exporter = new JSONExporter(targetDirectory: targetDirectory)
        when:
        controller.create(organization: 'msf-spain', location: 'Sierra Leone/Northern Province/Kambia District/Kambia')
        def users = controller.index()
        then:
        targetDirectory.listFiles().size() == initialFilesCount + 1
        users.size() == 1
        def user = users[0]
        def outputFile = new File(targetDirectory, user.hashCode() + ".json")
        outputFile.deleteOnExit()
        def parsedUser = parser.parseText(outputFile.text)
        
        user.organization == parsedUser.organization
        user.location == parsedUser.location
    }
}
