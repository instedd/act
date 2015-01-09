package org.instedd.act.sync

import groovy.json.JsonSlurper

import java.nio.file.Files
import java.nio.file.Paths

import org.apache.commons.lang.StringUtils
import org.instedd.act.Settings
import org.instedd.act.authentication.Credentials
import org.instedd.act.events.CaseUpdatedEvent
import org.instedd.act.events.CasesFileUpdatedEvent
import org.instedd.act.models.Case
import org.instedd.act.models.CasesFile
import org.instedd.act.models.DataStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.eventbus.EventBus
import com.google.inject.Inject

class RsyncSynchronizer implements DocumentSynchronizer {
    
	Logger logger = LoggerFactory.getLogger(RsyncSynchronizer.class)
	
	DataStore dataStore
	Credentials credentials
	@Inject EventBus eventBus
	
	RsyncCommandBuilder commandBuilder
	
	@Inject
	RsyncSynchronizer(Credentials credentials, Settings settings, DataStore dataStore) {
		this.dataStore = dataStore
		
		this.commandBuilder = new RsyncCommandBuilder([
			remoteHost: settings.get('sync.remoteHost'),
			remotePort: settings.get('sync.remotePort'),
			remoteUser: settings.get('sync.remoteUser'),
			remoteKey: credentials.privateKeyPath(),
			
			knownHostsFilePath: settings.useServerSignature ? settings.serverSignatureLocation() : "",
			
			inboxLocalDir: settings.inboxDir(),
			outboxLocalDir: settings.outboxDir()
		])
		
		new File(commandBuilder.outboxLocalDir).mkdirs()
		new File(commandBuilder.inboxLocalDir).mkdirs()
		
		checkRsyncAvailable()
		
		logger.info("Will sync files from ${commandBuilder.outboxLocalRoute()} to ${commandBuilder.outboxRemoteRoute()}")
		logger.info("Will sync files from ${commandBuilder.inboxRemoteRoute()} to ${commandBuilder.inboxLocalRoute()}")
	}
		
	@Override
	public void syncDocuments() {
		this.uploadDocuments()
		this.downloadDocuments()
	}
	
	public synchronized void sync(command, onFilesTransfered) {
		def stdoutBuffer = new StringBuffer()
		def stderrBuffer = new StringBuffer()
		
		logger.debug("Running rsync: {}", command.toString())
		
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
		
		def transferredLines = stdout.readLines().findAll({ line ->
			line.matches(/^[<>].*/) // starts with < or >
		})
		def transferredFilenames = transferredLines.collect({ line -> line.split(" ", 2)[1]})
		if(!transferredFilenames.empty) {
			onFilesTransfered(transferredFilenames)
		}
	}
	
	public void uploadDocuments() {
		this.sync(commandBuilder.buildUploadCommand(), { files ->
			files.each { String filename ->
				logger.trace "Uploaded ${filename}"
				def caseMatcher = filename =~ /case-(.+)\.json/
				if (caseMatcher.matches()) {
					dataStore.registerCaseSynced(caseMatcher[0][1])
				} else {
					if(filename.length() >= 36) {
						def possibleGuid = filename[0..35]
						dataStore.registerFileSynced(possibleGuid)
						eventBus.post([guid: possibleGuid] as CasesFileUpdatedEvent)
					} else {
						logger.warn("Uploaded unknown file named ${filename}")
					}
				}
			}
		})
	}
	
	public void downloadDocuments() {
		this.sync(commandBuilder.buildDownloadCommand(), {files ->
			files.each { filename ->
				logger.trace "Downloaded ${filename}"
				def matcher = filename =~ /^cases?-(file-)?(.+)\.json$/
				if(matcher.matches()) {
					String guid = matcher[0][2]
					File downloadedFile = this.downloadedFile(filename)
					def document = new JsonSlurper().parseText(downloadedFile.text)
					switch(matcher[0][1]) {
						case null:
							if(document.containsKey("guid")) {
								dataStore.register(new Case([name: document.patient_name, phone: document.patient_phone, age: document.patient_age, gender: document.patient_gender, preferredDialect: document.dialect_code, reasons: document.symptoms, notes: document.note, id: document.guid, synced: true, updated: true]))
								dataStore.registerCasesFileCaseReceived(document.guid)
							}
							if(document.containsKey("sick")) {
								dataStore.updateSickCase(guid, document.sick)
							}
							eventBus.post([guid: guid] as CaseUpdatedEvent)
							break
						case "file-":
							dataStore.associateCasesFile(guid, document)
							eventBus.post([guid: guid] as CasesFileUpdatedEvent)
							break
					}
					downloadedFile.delete()
				} else {
					def errorMatcher = filename =~ /^errored-(cases-)?file-(.+)\.json$/
					if(errorMatcher.matches()) {
						File downloadedFile = this.downloadedFile(filename)
						if(errorMatcher[0][1]) {
							String casesFileGuid = errorMatcher[0][2]
							dataStore.registerFailedCasesFileByGuid(casesFileGuid)
						} else {
							String failedFileName = errorMatcher[0][2]
							dataStore.registerFailedCasesFileByFileName(failedFileName)
						}
						downloadedFile.delete()
					}
				}
			}
		})
	}
	
	File downloadedFile(filename) {
		new File(new File(commandBuilder.inboxLocalDir), filename)
	}
	
	@Override
	public void queueForSync(String documentName, String content) {
		new File(commandBuilder.outboxLocalDir, documentName).withWriter('UTF-8') { out ->
			out.writeLine(content)
		}
	}
	
	void checkRsyncAvailable() {
		try {
			commandBuilder.buildTestCommand().execute()
			logger.info("Rsync presence test successful")
		} catch (Exception e) {
			logger.warn("Could not run test rsync command. Please check that the executable is available.", e)
			throw new IllegalStateException("Could not run test rsync command. Please check that the executable is available.", e) 
		}
	}

	@Override
	public void queueForSync(String guid, File document) {
		Files.copy(Paths.get(document.absolutePath), Paths.get(new File(new File(commandBuilder.outboxLocalDir), "${guid}-${document.name}").absolutePath))
	}
	
}
