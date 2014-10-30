package org.instedd.act.models

class User {

	String organization
    Location location

	def User(String organization, Location location) {
        this.organization = organization
        this.location = location
    }

}
