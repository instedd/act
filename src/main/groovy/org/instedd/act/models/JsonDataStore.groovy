package org.instedd.act.models

import groovy.json.JsonBuilder

import org.instedd.act.App
import org.instedd.act.Settings

import com.google.common.base.Preconditions
import com.google.inject.Inject

class JsonDataStore implements DataStore {

	File targetDirectory
	
	@Inject
	JsonDataStore(Settings settings) {
		this(new File(settings.get("sync.sourceDir")))
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
