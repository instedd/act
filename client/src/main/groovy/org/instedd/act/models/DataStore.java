package org.instedd.act.models;


public interface DataStore {

	boolean isDeviceIdentifierGenerated();
	
	String getDeviceIdentifier();
	
	void saveDeviceIdentifier(String identifier);
	
	boolean isUserRegistered();
	
	void register(User user);
	
}
