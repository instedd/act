package org.instedd.act

import org.instedd.act.models.DataStore
import org.instedd.act.models.JsonDataStore
import org.instedd.act.models.JsonLocationTree
import org.instedd.act.models.LocationTree
import org.instedd.act.sync.Daemon
import org.instedd.act.sync.DocumentSynchronizer
import org.instedd.act.sync.RsyncSynchronizer

import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Module

class App {

	static main(args) {
		def injector = Guice.createInjector(new ActModule())
		
		injector.getInstance(AppUI.class).start()
		injector.getInstance(Daemon.class).start()
	}

	static class ActModule implements Module {
		
		void configure(Binder binder) {
			binder.bind(Settings.class).asEagerSingleton();
			binder.bind(DataStore.class).to(JsonDataStore.class).asEagerSingleton();
			binder.bind(LocationTree.class).toInstance(new JsonLocationTree(new File('json/locations-packed.json')))
			binder.bind(DocumentSynchronizer.class).to(RsyncSynchronizer.class).asEagerSingleton();
		}

	}
}
