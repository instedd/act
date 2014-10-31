package org.instedd.act.models

import org.instedd.act.App;

import groovy.json.JsonBuilder

import com.google.common.base.Preconditions

class JsonDataStore implements DataStore {

	File targetDirectory
	
	JsonDataStore() {
		this(new File(App.JSON_SYNC_PATH))
	}
	
	JsonDataStore(File targetDirectory) {
		targetDirectory.mkdirs()
		this.targetDirectory = targetDirectory
	}
	
	@Override
	public boolean isUserRegistered() {
		def userFile = new File(targetDirectory, "user.json");
		userFile.exists() && userFile.isFile();
	}

	@Override
	public synchronized void register(User user) {
		Preconditions.checkState(!userRegistered)
		new File(targetDirectory, "user.json").withWriter('UTF-8') { out ->
			out.writeLine(this.userJson(user))
		}
	}
	
	def userJson(User user) {
		def json = new JsonBuilder()
		json organization: user.organization, location: user.location.id
		json.toString()
	}
}
