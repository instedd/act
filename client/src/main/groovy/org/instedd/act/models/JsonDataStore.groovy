package org.instedd.act.models

import groovy.json.JsonSlurper

import org.instedd.act.Settings

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.inject.Inject

class JsonDataStore implements DataStore {

	File targetDirectory
	
	@Inject
	JsonDataStore(Settings settings) {
		this(new File(settings.get("jsonStore.path")))
	}
	
	JsonDataStore(File targetDirectory) {
		targetDirectory.mkdirs()
		this.targetDirectory = targetDirectory
	}
	
	@Override
	public boolean isDeviceRegistered() {
		def deviceFile = new File(targetDirectory, "device.json");
		deviceFile.exists() && deviceFile.isFile();
	}

	@Override
	public synchronized void register(Device device) {
		Preconditions.checkState(!deviceRegistered, "Device is already registered")
		new File(targetDirectory, "device.json").withWriter('UTF-8') { out ->
			out.writeLine(device.asJson().toString())
		}
	}
    
    @Override
    public synchronized void register(Case aCase) {
        new File(targetDirectory, "case-${aCase.id}.json").withWriter('UTF-8') { out ->
            out.writeLine(aCase.asJson().toString())
        }
    }
	
	@Override
	public boolean isDeviceIdentifierGenerated() {
		return deviceIdentityFile().isFile();
	}

	@Override
	public synchronized void saveDeviceIdentifier(String identifier) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(identifier), "Invalid identifier")
		Preconditions.checkState(!isDeviceIdentifierGenerated(), "Device identifier was already generated")

		deviceIdentityFile().withWriter('UTF-8') { out ->
			out.writeLine(identifier)
		}
	}

	@Override
	public String getDeviceIdentifier() {
		return deviceIdentityFile().text.trim();
	}
	
	def deviceIdentityFile() { new File(targetDirectory, "device_id") }
	
	@Override
	public List<Case> listCases() {
		targetDirectory.listFiles()
					   .findAll { f -> f.name.startsWith("case") }
					   .collect { f -> Case.fromJson(new JsonSlurper().parseText(f.text)) }
					   .sort { c -> c.name }
	}
}
