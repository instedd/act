package org.instedd.act.models;


public interface DataStore {

	boolean isDeviceIdentifierGenerated();
	
	String getDeviceIdentifier();
	
	void saveDeviceIdentifier(String identifier);
	
	boolean isDeviceRegistered();
	
	void register(Device device);
	
	void register(Case aCase);
	
}
