package org.instedd.act

import org.instedd.act.authentication.Credentials
import org.instedd.act.sync.SynchronizationProcess
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.inject.Inject

class Daemon {

	protected final static Logger logger = LoggerFactory.getLogger(Daemon.class)

	@Inject SynchronizationProcess syncProcess
	
	public void start() {
		syncProcess.start()
	}

}
