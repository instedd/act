package org.instedd.act.models;


public interface DataStore {

	boolean isUserRegistered();
	
	void register(User user);
	
}
