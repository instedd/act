package org.instedd.act.models

import groovy.json.JsonSlurper

import com.google.common.io.Files

class JsonDataStoreTest extends GroovyTestCase {

	def tmpDir = Files.createTempDir()
	def dataStore = new JsonDataStore(tmpDir)
	
	def cleanup() {
		tmpDir.delete()
	}
	
	void "test saves a json file with user information"(){
		dataStore.register(new User("instedd", new Location(1L, "Buenos Aires")))
		def savedFiles = tmpDir.listFiles()
		savedFiles.size() == 1
		def userFile = savedFiles[0]
		assert userFile.isFile()
		assert userFile.name == "user.json"
		
		def json = new JsonSlurper().parse(userFile)
		assert json.location == 1
		assert json.organization == "instedd"
	}

	void "test does not allow to register more than once"() {
		def user = new User("instedd", new Location(1L, "Buenos Aires"))
		
		dataStore.register(user)
		shouldFail {
			dataStore.register(user)
		}
	}
	
	void "test informs no user was registered before registration occurs"() {
		assert dataStore.userRegistered == false
	}
	
	void "test informs that a user was registered after registration"() {
		dataStore.register(new User("instedd", new Location(1L, "Buenos Aires")))
		assert dataStore.userRegistered == true
	}
	
}
