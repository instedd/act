package org.instedd.act.models

class User {
    String organization
    List<String> location
    def User() {
        //default constructor
    }
    def User(organization, location) {
        this.organization = organization
        this.location = location
    }
}
