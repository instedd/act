package org.instedd.act.controllers

import org.instedd.act.models.User

class UserController {
    
    def users = []
    
    def index() {
        users
    }
    
    def create(params) {
        users.add(new User(params.organization, params.location))
    }

}
