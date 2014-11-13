package org.instedd.act.models;

import java.util.List;
import java.util.Map;


public interface DataStore {

	boolean isDeviceIdentifierGenerated();
	
	String getDeviceIdentifier();
	
	void saveDeviceIdentifier(String identifier);
	
	boolean isDeviceRegistered();
	
	void register(Device device);
	
	void register(Case aCase);

	List<Case> listCases();
	
	boolean needsSyncDeviceInfo();
	
	Map<String, Object> deviceInfo();
	
	void registerDeviceInfoSynced();
}
