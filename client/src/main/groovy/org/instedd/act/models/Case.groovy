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
	String id = UUID.randomUUID().toString()

	def asJson() {
		def json = new JsonBuilder()
		json guid: id,
		name: name,
		phone_number: phone,
		age: age,
		gender: gender,
		dialect_code: preferredDialect,
		symptoms: reasons,
		note: notes
		json
	}
}
