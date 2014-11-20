package org.instedd.act.models;

import java.util.List;
import java.util.Map;


public interface DataStore {

	void storeDeviceInfo(Device device);
	
	boolean userInfoCompleted();
	
	Map<String, Object> deviceInfo();
	
	boolean isDeviceRegistered();

	void markDeviceRegistered();
	
	void register(Case aCase);

	List<Case> listCases();
	
	List<Case> unsyncedCases();
	
	void registerCaseSynced(String guid);
	
	void updateSickCase(String guid, Boolean isSick);
	
}
