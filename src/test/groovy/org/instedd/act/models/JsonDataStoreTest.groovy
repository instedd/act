package org.instedd.act.models

import groovy.json.JsonSlurper

import com.google.common.io.Files

class JsonDataStoreTest extends GroovyTestCase {

	File tmpDir
	DataStore dataStore
	
	void setUp() {
		tmpDir = Files.createTempDir()
		dataStore = new JsonDataStore(tmpDir)
	}
	
	void tearDown() {
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

	void "test informs no identifier was generated when first initialized"() {
		assert dataStore.deviceIdentifierGenerated == false
	}	

	void "test informs if identifier was previously generated"() {
		dataStore.saveDeviceIdentifier("device00001")
		assert dataStore.deviceIdentifierGenerated == true
		def id = dataStore.deviceIdentifier
		assert id == "device00001"
	}
	
	void "test doesn't allow to change device identifier"() {
		dataStore.saveDeviceIdentifier("device00001")
		shouldFail { dataStore.saveDeviceIdentifier("device00002") }
	}
	
	void "test doesn't allow setting null identifier"() {
		shouldFail(IllegalArgumentException.class) { dataStore.saveDeviceIdentifier(null) }
	}
	
	void "test doesn't allow setting empty identifier"() {
		shouldFail(IllegalArgumentException.class) { dataStore.saveDeviceIdentifier("") }
	}
}
