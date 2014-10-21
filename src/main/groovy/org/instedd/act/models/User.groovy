package org.instedd.act.models

class User {
    String organization
    List<String> location
    def User() {
        //default constructor
    }
    def User(organization, locationList) {
        this.organization = organization
        this.location = locationList.split('/')
    }
}
