package org.instedd.act

import org.instedd.act.authentication.Credentials
import org.instedd.act.authentication.Registration
import org.instedd.act.db.DatabaseConnector
import org.instedd.act.db.Migrator
import org.instedd.act.db.SqliteConnector
import org.instedd.act.misc.DocumentExporter
import org.instedd.act.misc.SqliteToJsonExporter
import org.instedd.act.models.DataStore
import org.instedd.act.models.JsonLocationTree
import org.instedd.act.models.LocationTree
import org.instedd.act.models.SqliteDataStore
import org.instedd.act.sync.DocumentSynchronizer
import org.instedd.act.sync.RsyncSynchronizer

import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Module

class App {

	static main(args) {
		def settings = new Settings()
		def credentials = Credentials.initialize(settings.get("sync.keyLocation", "."))
		def connector = new SqliteConnector(settings)

		migrateDatabase(connector)
		
		def injector = Guice.createInjector(new ActModule([
			settings: settings,
			credentials: credentials,
			connector: connector
		]))
		
		injector.getInstance(DocumentExporter.class).exportDocuments()
		injector.getInstance(AppUI.class).start()
		injector.getInstance(Daemon.class).start()
		
		// resume if device is locally registered but information was
		// not sent to the server.
		injector.getInstance(Registration.class).ensureRegistered()
	}

	static void migrateDatabase(DatabaseConnector connector) {
		new Migrator(connector).migrate()
	}
	
	static class ActModule implements Module {

		Settings settings		
		Credentials credentials
		DatabaseConnector connector
		
		void configure(Binder binder) {
			binder.bind(Settings.class).toInstance(settings)
			binder.bind(Credentials.class).toInstance(credentials)
			binder.bind(DatabaseConnector.class).toInstance(connector)
			
			binder.bind(DataStore.class).to(SqliteDataStore.class).asEagerSingleton();
			binder.bind(DocumentExporter.class).to(SqliteToJsonExporter.class).asEagerSingleton()
			binder.bind(LocationTree.class).toInstance(new JsonLocationTree(new File('json/locations-packed.json')))
			binder.bind(DocumentSynchronizer.class).to(RsyncSynchronizer.class).asEagerSingleton();
		}

	}
}
