package org.instedd.act.sync;


public interface DocumentSynchronizer {

	public void syncDocuments();
	
	public void queueForSync(String documentName, String content);
}
