package org.instedd.act.models

import groovy.json.JsonBuilder

import com.google.common.base.Preconditions
import com.google.common.base.Strings

class Device {

	String organization
    Location location
	String supervisorName
	String supervisorNumber

	def Device(String organization, Location location, String supervisorName, String supervisorNumber) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(organization))
		Preconditions.checkNotNull(location)
		Preconditions.checkArgument(!Strings.isNullOrEmpty(supervisorName))
		Preconditions.checkArgument(!Strings.isNullOrEmpty(supervisorNumber))
        this.organization = organization
        this.location = location
        this.supervisorName = supervisorName
		this.supervisorNumber = supervisorNumber
    }

	def asJson() {
		def json = new JsonBuilder()
		json organization: organization, location: location.id, supervisorName: supervisorName, supervisorNumber: supervisorNumber
		json
	}
	
}
