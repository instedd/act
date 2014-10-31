package org.instedd.act.sync

import java.util.concurrent.TimeUnit

import org.instedd.act.App
import org.instedd.act.Settings;

import sun.org.mozilla.javascript.internal.Synchronizer;

import com.google.common.util.concurrent.AbstractScheduledService
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.inject.Inject;

class Daemon extends AbstractScheduledService {

	@Inject DocumentSynchronizer synchronizer
	
	Scheduler scheduler

	@Inject
	Daemon(Settings settings) {
		scheduler = Scheduler.newFixedDelaySchedule(0, settings.getInt("sync.interval.seconds"), TimeUnit.SECONDS)
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
