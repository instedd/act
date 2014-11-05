package org.instedd.act.models

import groovy.json.JsonBuilder

class User {

	String organization
    Location location

	def User(String organization, Location location) {
        this.organization = organization
        this.location = location
    }

	def asJson() {
		def json = new JsonBuilder()
		json organization: organization, location: location.id
		json
	}
	
}
