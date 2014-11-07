package org.instedd.act.models

import groovy.json.JsonBuilder

class Device {

	String organization
    Location location
	String supervisorNumber

	def Device(String organization, Location location, String supervisorNumber) {
        this.organization = organization
        this.location = location
		this.supervisorNumber = supervisorNumber
    }

	def asJson() {
		def json = new JsonBuilder()
		json organization: organization, location: location.id, supervisorNumber: supervisorNumber
		json
	}
	
}
