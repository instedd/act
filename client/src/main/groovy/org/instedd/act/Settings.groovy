package org.instedd.act

import java.io.InputStream
import java.util.Properties

import com.google.common.base.Optional;
import com.google.common.base.Strings

class Settings {

	Properties props;
	String dataDir
	
	Settings() {
		InputStream inputStream = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream("act.properties")
				
		props = new Properties()
		props.load(inputStream)
	}
	
	String get(String key) {
		props.getProperty(key)
	}
	
	String get(String key, String defaultValue) {
		Optional.fromNullable(get(key)).or(defaultValue)
	}
	
	Integer getInt(String key) {
		Integer.valueOf(get(key))
	}
	
	Integer getInt(String key, int defaultValue) {
		Optional.fromNullable(getInt(key)).or(defaultValue)
	}
	
	public String dataDir() {
		if(!dataDir) {
			dataDir = this.get('local.dir', './')
			if(dataDir.contains("~")) {
				String userHome = System.getProperty("user.home")
				if(userHome.endsWith("/")) {
					userHome = userHome[0..-2] // remove last character
				}
				dataDir = dataDir.replace("~", userHome)
			}
			if(!dataDir.endsWith("/")) {
				dataDir += "/"
			}
		}
		dataDir
	}

	public String databasePath() {
		"${this.dataDir()}local.db"
	}
	
	public String inboxDir() {
		"${this.dataDir()}inbox/"
	}
	
	public String outboxDir() {
		"${this.dataDir()}outbox/"
	}
	
	public String keyLocation() {
		this.dataDir()
	}
	
	public String serverSignatureUrl() {
		this.get("server.signatureUrl")
	}
	
	public String serverSignatureLocation() {
		"${this.dataDir()}server_signature.key"
	}
	
	public Boolean isUseServerSignature() {
		def signature = this.serverSignatureUrl()
		signature != null && !signature.isEmpty() 
	}
	
}
