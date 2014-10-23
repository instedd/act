package org.instedd.act.controllers

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
}
