package org.instedd.act

import org.instedd.act.models.DataStore
import org.instedd.act.models.JsonDataStore;
import org.instedd.act.models.JsonLocationTree
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
		
		void configure(Binder binder) {
			binder.bind(DataStore.class).to(JsonDataStore.class).asEagerSingleton();
			binder.bind(LocationTree.class).toInstance(new JsonLocationTree(new File('json/locations-packed.json')))
		}

	}
}
