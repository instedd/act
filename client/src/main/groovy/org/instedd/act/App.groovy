package org.instedd.act

import org.instedd.act.db.DatabaseConnector
import org.instedd.act.db.Migrator
import org.instedd.act.db.SqliteConnector
import org.instedd.act.misc.DocumentExporter
import org.instedd.act.misc.SqliteToJsonExporter
import org.instedd.act.models.DataStore
import org.instedd.act.models.JsonLocationTree
import org.instedd.act.models.LocationTree
import org.instedd.act.models.SqliteDataStore
import org.instedd.act.sync.Daemon
import org.instedd.act.sync.DocumentSynchronizer
import org.instedd.act.sync.RsyncSynchronizer

import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Module

class App {

	static main(args) {
		def injector = Guice.createInjector(new ActModule())
		
		injector.getInstance(DocumentExporter.class).exportDocuments()
		injector.getInstance(AppUI.class).start()
		injector.getInstance(Daemon.class).start()
	}

	static class ActModule implements Module {
		
		void configure(Binder binder) {
			Settings settings = new Settings()
			binder.bind(Settings.class).toInstance(settings)
			DatabaseConnector connector = new SqliteConnector(settings)
			binder.bind(DatabaseConnector.class).toInstance(connector)
			migrateDatabase(connector)
			binder.bind(DataStore.class).to(SqliteDataStore.class).asEagerSingleton();
			binder.bind(DocumentExporter.class).to(SqliteToJsonExporter.class).asEagerSingleton()
			binder.bind(LocationTree.class).toInstance(new JsonLocationTree(new File('json/locations-packed.json')))
			binder.bind(DocumentSynchronizer.class).to(RsyncSynchronizer.class).asEagerSingleton();
		}
		
		void migrateDatabase(DatabaseConnector connector) {
			new Migrator(connector).migrate()
		}

	}
}
