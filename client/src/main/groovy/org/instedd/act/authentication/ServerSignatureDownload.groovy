package org.instedd.act.authentication

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

class ServerSignatureDownload implements AuthenticationStep {

	Logger logger = LoggerFactory.getLogger(ServerSignatureDownload.class) 
	
	@Override
	public boolean ensureDone() {
		logger.warn("SKIPPING SERVER SIGNATURE DOWNLOAD")
		return true;
	}

	@Override
	public boolean isDone() {
		return true;
	}

}
