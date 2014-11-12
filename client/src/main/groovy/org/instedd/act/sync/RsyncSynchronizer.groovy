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
	
	String baseCommand = "rsync -iaz --remove-source-files"
	
	String localOutboxDir
	String remoteOutboxDir
	String outboxHost
	
	String localInboxDir
	String remoteInboxDir
	String inboxHost

	def localOutboxRoute() { route("", localOutboxDir) }
	def remoteOutboxRoute() { route(outboxHost, remoteOutboxDir) }
	
	def localInboxRoute() { route("", localInboxDir) }
	def remoteInboxRoute() { route(inboxHost, remoteInboxDir) }
	
	def uploadCommandLine() { "${baseCommand} ${localOutboxRoute()} ${remoteOutboxRoute()}" }
	def downloadCommandLine() { "${baseCommand} ${remoteInboxRoute()} ${localInboxRoute()}" }
	
	RsyncSynchronizer() {
		checkRsyncAvailable()
	}
	
	@Inject
	RsyncSynchronizer(Settings settings, DataStore dataStore) {
		this()
		
		if (!dataStore.deviceIdentifierGenerated) {
			dataStore.saveDeviceIdentifier(UUID.randomUUID().toString());
		}
		
		localOutboxDir = settings.get("sync.outbox.localDir")
		new File(localOutboxDir).mkdirs()
		remoteOutboxDir = "${settings.get("sync.outbox.remoteDir")}/${dataStore.deviceIdentifier}"
		outboxHost = settings.get("sync.outbox.remoteHost")
		
		logger.info("Will sync files from ${localOutboxRoute()} to ${remoteOutboxRoute()}")
		
		localInboxDir = settings.get("sync.inbox.localDir")
		new File(localInboxDir).mkdirs()
		remoteInboxDir = "${settings.get("sync.inbox.remoteDir")}/${dataStore.deviceIdentifier}"
		inboxHost = settings.get("sync.inbox.remoteHost")
		
		logger.info("Will sync files from ${remoteInboxRoute()} to ${localInboxRoute()}")
	}
		
	@Override
	public synchronized void syncDocuments() {
		this.uploadDocuments()
		this.downloadDocuments()
	}
	
	public synchronized void uploadDocuments() {
        def stdoutBuffer = new StringBuffer()
        def stderrBuffer = new StringBuffer()
        
        def command = this.uploadCommandLine()
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
	
	public synchronized void downloadDocuments() {
		def stdoutBuffer = new StringBuffer()
		def stderrBuffer = new StringBuffer()
		
		def command = this.downloadCommandLine()
		logger.debug("Running rsync: {}", command)
		
		Process process = command.execute()
		process.consumeProcessOutput(stdoutBuffer, stderrBuffer)
		process.waitFor()

		String stdout = stdoutBuffer.toString()
		String stderr = stderrBuffer.toString()
		if (StringUtils.isEmpty(stderr)) {
			logger.trace("Standard output for rsync was:\n{}", stdout)
		} else {
			logger.warn("Standard output for rsync was:\n{}", stdout)
			logger.warn("Error output for rsync was:\n{}", stderr)
		}
		
		def inlines = stdout.readLines().findAll({ line ->
			line.startsWith(">")
		})
		def receivedFilenames = inlines.collect({ line -> line.split(" ", 2)[1]})
		if(!receivedFilenames.empty) {
			this.onFilesReceived(receivedFilenames)
		}
	}

	@Override
	public void queueForSync(String documentName, String content) {
		new File(localOutboxDir, documentName).withWriter('UTF-8') { out ->
			out.writeLine(content)
		}
	}
	
	def onFilesReceived(filenames) {
		filenames.each { filename -> println "Received ${filename}" }
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
