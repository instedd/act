package org.instedd.act.models

import spock.lang.*

class UserTest extends Specification{
    def "user parses it's location"() {
		def locationPath = ['Sierra Leone', 'Northern Province', 'Kambia District', 'Kambia']
        when:
        def user = new User('msf-spain', locationPath)
        then:
        user.organization == 'msf-spain'
        user.location == locationPath
    }
}
