package org.instedd.act

import java.io.InputStream
import java.util.Properties

import com.google.common.base.Optional;
import com.google.common.base.Strings

class Settings {

	Properties props;
	
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
	
}
