package org.instedd.act.sync

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import org.instedd.act.Settings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.util.concurrent.AbstractScheduledService
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject

class Daemon extends AbstractScheduledService {

	protected final static Logger logger = LoggerFactory.getLogger(Daemon.class)
	
	@Inject DocumentSynchronizer synchronizer
	
	Scheduler scheduler
	
	ExecutorService manualSyncExecutor
	AtomicBoolean waitingForSync = new AtomicBoolean(false)
	
	@Inject
	Daemon(Settings settings) {
		scheduler = Scheduler.newFixedDelaySchedule(0, settings.getInt("sync.interval.seconds"), TimeUnit.SECONDS)
		
		def threadFactory = new ThreadFactoryBuilder().setNameFormat("manual-sync").build()
		manualSyncExecutor = Executors.newSingleThreadExecutor(threadFactory)
	}
	
	// If two consecutive manual syncs arrive, the second one will be discarded
	// We assume that the second update will be covered by the periodic synchronization
	// process.
	public void requestSync() {
		if (!waitingForSync.getAndSet(true)) {
			logger.info("Submitting request for synchronization (triggered by application event)")
			manualSyncExecutor.submit {
				synchronizer.syncDocuments()
				waitingForSync.set(false)
			}
		} else {
			logger.info("There is already a pending synchronization process, will not trigger another one")
		}
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		try {
			logger.debug("Running document synchronizer")
			synchronizer.syncDocuments();
		} catch (Exception e) {
			logger.warn("An error occurred synchronizing documents with server", e)
		}
	}

	@Override
	protected Scheduler scheduler() {
		return scheduler;
	}
	
}
