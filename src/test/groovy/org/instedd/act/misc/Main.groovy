package org.instedd.act.misc

import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.AbstractScheduledService
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler

class Main {

    static main(args) {
        def service = new AbstractScheduledService() {
            Scheduler scheduler = Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.SECONDS)
            // fake command line for testing
            RsyncDaemon rsync = [ commandLine: { "date" }] as RsyncDaemon
    
            protected Scheduler scheduler() {
                this.scheduler
            };
            protected void runOneIteration() throws Exception {
                try {
                    this.rsync.sync()
                } catch (Exception exception) {
                    exception.printStackTrace()
                }
            };
    
            void requestSync() {
                this.rsync.requestSync()
            }
        }

        service.startAndWait()
        service.requestSync()
    }
}
