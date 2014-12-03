package org.instedd.act

import org.instedd.act.authentication.AuthenticationProcess;
import org.instedd.act.authentication.Credentials
import org.instedd.act.authentication.DeviceKeyRegistration
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
import org.instedd.act.sync.SynchronizationProcess;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Module

class App {

	static def settings = new Settings();
	
	static main(args) {
		def credentials = Credentials.initialize(settings.keyLocation())
		def connector = new SqliteConnector(settings)

		migrateDatabase(connector)
		
		def injector = Guice.createInjector(new ActModule([
			settings: settings,
			credentials: credentials,
			connector: connector
		]))
		
		injector.getInstance(AuthenticationProcess.class).ensureDone()
		injector.getInstance(DocumentExporter.class).exportDocuments()
		injector.getInstance(AppUI.class).start()
		injector.getInstance(SynchronizationProcess.class).start()
		
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
			
			binder.bind(EventBus.class).toInstance(new EventBus())
			binder.bind(DataStore.class).to(SqliteDataStore.class).asEagerSingleton();
			binder.bind(DocumentExporter.class).to(SqliteToJsonExporter.class).asEagerSingleton()
			binder.bind(LocationTree.class).toInstance(new JsonLocationTree(new File('json/locations/')))
			binder.bind(DocumentSynchronizer.class).to(RsyncSynchronizer.class).asEagerSingleton();
		}

	}
}
