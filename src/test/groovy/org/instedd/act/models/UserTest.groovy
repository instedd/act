package org.instedd.act.models

import spock.lang.*

class UserTest extends Specification{
    def "user parses it's location"() {
        when:
        def user = new User('msf-spain', 'Sierra Leone/Northern Province/Kambia District/Kambia')
        then:
        user.organization == 'msf-spain'
        user.location == ['Sierra Leone', 'Northern Province', 'Kambia District', 'Kambia']
    }
}
