package org.instedd.act.models

import groovy.json.JsonBuilder
import groovy.transform.EqualsAndHashCode;


@EqualsAndHashCode
class Case {
    String name
    String phone
    Integer age
    String gender
    String preferredDialect
    Collection reasons
    String notes
    
    def asJson() {
        def json = new JsonBuilder()
        json name: name,
            phone: phone,
            age: age,
            gender: gender,
            preferredDialect: preferredDialect,
            reasons: reasons,
            notes: notes
        json
    }
}
