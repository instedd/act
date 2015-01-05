package org.instedd.act.sync;

import java.io.File;


public interface DocumentSynchronizer {

	public void syncDocuments();
	
	public void queueForSync(String documentName, String content);
	
	public void queueForSync(File document);
}
