package org.instedd.act.controllers

import spock.lang.*

class UserControllerTest extends Specification{
    def "controller indexes no users"() {
        setup:
        def controller = new UserController()
        when:
        def users = controller.index()
        then:
        users.isEmpty()
    }
}
