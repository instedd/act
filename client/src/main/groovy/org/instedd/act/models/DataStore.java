package org.instedd.act.models;

import java.util.List;
import java.util.Map;


public interface DataStore {

	boolean isDeviceRegistered();
	
	void register(Device device);

	void register(Case aCase);

	List<Case> listCases();
	
	boolean needsSyncDeviceInfo();
	
	Map<String, Object> deviceInfo();
	
	void registerDeviceInfoSynced();
	
	List<Case> unsyncedCases();
	
	void registerCaseSynced(String guid);
	
	void updateSickCase(String guid, Boolean isSick);
	
}
