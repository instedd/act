package org.instedd.act.controllers

import org.instedd.act.models.User

class UserController {
    
    def users = []
    
    def index() {
        users
    }
    
    def create(params) {
        def user = new User(params.organization, params.location)
        users.add(user)
    }

}
