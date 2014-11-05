package org.instedd.act.sync

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.instedd.act.Settings;
import org.instedd.act.models.DataStore;
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

class RsyncSynchronizer implements DocumentSynchronizer {
    
	Logger logger = LoggerFactory.getLogger(RsyncSynchronizer.class)
	
	String baseCommand = "rsync -avz --remove-source-files"
    String sourceDir
    String targetDir
    String sourceHost
    String targetHost

	def sourceRoute() { route(sourceHost, sourceDir) }
	def targetRoute() { route(targetHost, targetDir) }
	def commandLine() { "${baseCommand} ${sourceRoute()} ${targetRoute()}" }
	
	RsyncSynchronizer() {
		checkRsyncAvailable()
	}
	
	@Inject
	RsyncSynchronizer(Settings settings, DataStore dataStore) {
		this()
		
		if (!dataStore.deviceIdentifierGenerated) {
			dataStore.saveDeviceIdentifier(UUID.randomUUID().toString());
		}
		
		sourceDir  = settings.get("sync.sourceDir")
		sourceHost = settings.get("sync.sourceHost")
		targetDir  = "${settings.get("sync.targetDir")}/${dataStore.deviceIdentifier}"
		targetHost = settings.get("sync.targetHost")
		
		logger.info("Will sync files in ${sourceRoute()} to ${targetRoute()}")
	}
		
	@Override
	public synchronized void syncDocuments() {
        def stdoutBuffer = new StringBuffer()
        def stderrBuffer = new StringBuffer()
        
        def command = this.commandLine()
        logger.debug("Running rsync: {}", command) 
        
        Process process = command.execute()
        process.consumeProcessOutput(stdoutBuffer, stderrBuffer)
        process.waitFor()

		def stdout = stdoutBuffer.toString()
		def stderr = stderrBuffer.toString()
		if (StringUtils.isEmpty(stderr)) {
			logger.trace("Standard output for rsync was:\n{}", stdout)        
		} else {
			logger.warn("Standard output for rsync was:\n{}", stdout)
			logger.warn("Error output for rsync was:\n{}", stderr)
		}
	}

	@Override
	public void queueForSync(String documentName, String content) {
		new File(sourceDir, documentName).withWriter('UTF-8') { out ->
			out.writeLine(content)
		}
	}
	
	void checkRsyncAvailable() {
		try {
			"rsync --help".execute()
			logger.info("Rsync presence test successful")
		} catch (Exception e) {
			logger.warn("Could not run test rsync command. Please check that the executable is available.", e)
			throw new IllegalStateException("Could not run test rsync command. Please check that the executable is available.", e) 
		}
	}
	
	def route(String host, String dir) {
		def prefix = StringUtils.isEmpty(host) ? "" : "${host}:"
		dir = dir.endsWith("/") ? dir : "${dir}/"
		"${prefix}${dir}"
	}
}
