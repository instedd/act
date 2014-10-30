package org.instedd.act

import org.instedd.act.models.DataStore
import org.instedd.act.models.Location
import org.instedd.act.models.LocationTree

import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Module

class App {

	static main(args) {
		def injector = Guice.createInjector(new ActModule())
		
		injector.getInstance(AppUI.class).start()
	}

	
	static class ActModule implements Module {

		def mockLocationTree = [
			rootLocations: {
				["Guinea", "Liberia", "Nigeria", "Sierra Leone"].collect { name -> new Location(1L, name) }
			},
			children: { path ->
				if (path.size() < 3) {
					[1,2,3].collect { i -> new Location(Long.valueOf(i), "${path.last()} > ${i}") }
				} else {
					[]
				}
			}
		] as LocationTree
		
		def mockDataStore = [ 
			isUserRegistered: { false },
			register: { user -> println "User for organization ${user.organization} registered in ${user.location}" }
		] as DataStore
	
		void configure(Binder binder) {
			binder.bind(DataStore.class).toInstance(mockDataStore)
			binder.bind(LocationTree.class).toInstance(mockLocationTree)
		}

	}
}
