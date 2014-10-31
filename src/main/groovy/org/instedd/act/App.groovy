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

	static String JSON_SYNC_PATH = "store"
	
	/** time in minutes between the end of a synchronization task and the beginning of the next one */
	static int JSON_SYNC_INTERVAL = 1
	
	static main(args) {
		def injector = Guice.createInjector(new ActModule())
		
		injector.getInstance(AppUI.class).start()
		injector.getInstance(Daemon.class).start()
	}

	static class ActModule implements Module {
		
		void configure(Binder binder) {
			binder.bind(DataStore.class).to(JsonDataStore.class).asEagerSingleton();
			binder.bind(LocationTree.class).toInstance(new JsonLocationTree(new File('json/locations-packed.json')))
			binder.bind(DocumentSynchronizer.class).toInstance(new RsyncSynchronizer([sourceDir: 'store/', targetDir: '/tmp/store']))
		}

	}
}
