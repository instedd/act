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
	
	void "test saves a json file with device information"(){
		dataStore.register(new Device("instedd", new Location(1L, "Buenos Aires"), "John", "+5491155555555"))
		def savedFiles = tmpDir.listFiles()
		savedFiles.size() == 1
		def deviceFile = savedFiles[0]
		assert deviceFile.isFile()
		assert deviceFile.name == "device.json"
		
		def json = new JsonSlurper().parse(deviceFile)
		assert json.location == 1
		assert json.organization == "instedd"
		assert json.supervisorNumber == "+5491155555555"
	}

	void "test does not allow to register more than once"() {
		def device = new Device("instedd", new Location(1L, "Buenos Aires"), "John", "+5491155555555")
		
		dataStore.register(device)
		shouldFail {
			dataStore.register(device)
		}
	}
	
	void "test informs no device was registered before registration occurs"() {
		assert dataStore.deviceRegistered == false
	}
	
	void "test informs that a device was registered after registration"() {
		dataStore.register(new Device("instedd", new Location(1L, "Buenos Aires"), "John", "+5491155555555"))
		assert dataStore.deviceRegistered == true
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
