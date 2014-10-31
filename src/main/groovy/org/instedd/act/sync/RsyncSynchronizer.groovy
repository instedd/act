package org.instedd.act.sync

import com.google.common.base.Preconditions;

class RsyncSynchronizer implements DocumentSynchronizer {
    
	String baseCommand = "rsync -avz --remove-source-files"
    String sourceDir
    String targetDir
    String sourceHost
    String targetHost
    Boolean waitingForSync = false

	RsyncSynchronizer() {
		checkRsyncAvailable()
	}
	
	@Override
	public synchronized void syncDocuments() {
        def stdout = new StringBuffer()
        def stderr = new StringBuffer()
        
        def command = this.commandLine()
        println(command)
        
        Process process = command.execute()
        process.consumeProcessOutput(stdout, stderr)
        process.waitFor()
        
        println(stdout)
        println(stderr)		
	}
	
	void checkRsyncAvailable() {
		try {
			"rsync --help"
		} catch (Exception e) {
			throw new IllegalStateException("Could not run test rsync command. Please check that the executable is available.", e) 
		}
	}
	
	def requestSync() {
		if (!waitingForSync) {
			this.waitingForSync = true
			Thread.start {
				this.syncDocuments()
				this.waitingForSync = false
			}
		}
	}

	def commandLine() {
		String command = "${baseCommand} "
		if (sourceHost) {
			command += "${sourceHost}:"
		}
		command += "${sourceDir} "
		if (targetHost) {
			command += "${targetHost}:"
		}
		command += "${targetDir}"
	}
}
