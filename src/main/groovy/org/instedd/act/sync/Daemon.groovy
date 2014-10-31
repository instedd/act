package org.instedd.act.sync

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import org.instedd.act.Settings

import com.google.common.util.concurrent.AbstractScheduledService
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.inject.Inject

class Daemon extends AbstractScheduledService {

	@Inject DocumentSynchronizer synchronizer
	
	Scheduler scheduler
	
	ExecutorService manualSyncExecutor
	AtomicBoolean waitingForSync = new AtomicBoolean(false)
	
	@Inject
	Daemon(Settings settings) {
		scheduler = Scheduler.newFixedDelaySchedule(0, settings.getInt("sync.interval.seconds"), TimeUnit.SECONDS)
		manualSyncExecutor = Executors.newSingleThreadExecutor()
	}
	
	// If two consecutive manual syncs arrive, the second one will be discarded
	// We assume that the second update will be covered by the periodic synchronization
	// process.
	public void requestSync() {
		if (!waitingForSync.getAndSet(true)) {
			manualSyncExecutor.submit {
				synchronizer.syncDocuments()
				waitingForSync.set(false)
			}
		}
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		try {
			synchronizer.syncDocuments();
		} catch (Exception e) {
			// TODO: log!
		}
	}

	@Override
	protected Scheduler scheduler() {
		return scheduler;
	}

}
