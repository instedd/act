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
	
	void register(CasesFile file);

	List<Case> listCases();
	
	List<Case> unsyncedCases();
	
	List<Case> unreadCases();
	
	void registerCaseSynced(String guid);
	
	void updateSickCase(String guid, Boolean isSick);
	
	void markCaseAsRead(Case aCase);
	
	String[] availableDialects();
	
	String[] contactReasons();
	
	void associateCasesFile(String guid, List<String> document);
	
	List<CasesFile> listCasesFiles();
	
}
