package org.instedd.act.sync

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.SystemUtils;

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
		def knownHostsParam = StringUtils.isEmpty(knownHostsFilePath) ? "" : "-oUserKnownHostsFile=\"${cygwinPath(knownHostsFilePath)}\""
		"ssh -p ${remotePort} ${userParam} -i \"${cygwinPath(remoteKey)}\" ${knownHostsParam} -oBatchMode=yes"
	}

	def localRoute(String dir) {
		dir = dir.endsWith("/") ? dir : "${dir}/"
		cygwinPath(dir)
	}
	
	def cygwinPath(String path) {
		if (SystemUtils.IS_OS_WINDOWS) {
			path = path.replaceFirst(/^(.):\/*/, '/cygdrive/$1/') // replace "C:/something" with "/cygdrive/c/something" for rsync to understand it
			path = path.replace("\\", "/")
		}
		path
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
