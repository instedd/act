package org.instedd.act.sync

import org.apache.commons.lang.StringUtils

import com.google.common.base.Preconditions;

class RsyncCommandBuilder {

	String remoteHost
	String remotePort
	String remoteUser
	String remoteKey

	String knownHostsFilePath
	
	String inboxLocalDir
	String outboxLocalDir
	
	def buildUploadCommand() {
		validate()
		["rsync", "-iaz", "--remove-source-files", "-e", shellCommand(), outboxLocalRoute(), outboxRemoteRoute()]
	}
	
	def buildDownloadCommand() {
		validate()
		["rsync", "-iaz", "--remove-source-files", "-e", shellCommand(), inboxRemoteRoute(), inboxLocalRoute()]
	}

	def buildTestCommand() {
		"rsync --help"
	}
	
	def outboxLocalRoute() { localRoute(outboxLocalDir) }
	def outboxRemoteRoute() { "${remoteHost}:/inbox" }
	
	def inboxLocalRoute() { localRoute(inboxLocalDir) }
	def inboxRemoteRoute() { "${remoteHost}:/outbox/" } //trailing slash prevents an 'outbox' directory to be created
	
	def shellCommand() {
		def userParam = StringUtils.isEmpty(remoteUser) ? "" : "-l ${remoteUser}"
		def knownHostsParam = StringUtils.isEmpty(knownHostsFilePath) ? "" : "-oUserKnownHostsFile=\'${knownHostsFilePath}\'"  
		"ssh -p ${remotePort} ${userParam} -i ${remoteKey} ${knownHostsParam} -oBatchMode=yes"
	}

	def localRoute(String dir) {
		dir = dir.endsWith("/") ? dir : "${dir}/"
	}
	
	def validate() {
		[remoteHost, remotePort, remoteKey].each { f ->
			Preconditions.checkNotNull(f, "Remote host settings missing (required: host, port, user and path to ssh key")
		}
		[inboxLocalDir, outboxLocalDir].each { f ->
			Preconditions.checkNotNull(f, "Not all sync paths are configured")
		}
	}
}
