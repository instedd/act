package org.instedd.act.sync

import org.apache.commons.lang.StringUtils

import com.google.common.base.Preconditions;

class RsyncCommandBuilder {

	String remoteHost
	String remotePort
	String remoteUser
	String remoteKey
		
	String inboxLocalDir
	String inboxRemoteDir
	
	String outboxLocalDir
	String outboxRemoteDir
	
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
	
	def outboxLocalRoute() { route("", outboxLocalDir) }
	def outboxRemoteRoute() { route(remoteHost, outboxRemoteDir) }
	
	def inboxLocalRoute() { route("", inboxLocalDir) }
	def inboxRemoteRoute() { route(remoteHost, inboxRemoteDir) }
	
	def shellCommand() {
		def userParam = StringUtils.isEmpty(remoteUser) ? "" : "-l ${remoteUser}" 
		"ssh -p ${remotePort} ${userParam} -i ${remoteKey} -oStrictHostKeyChecking=no -oBatchMode=yes"
	}

	def route(String host, String dir) {
		def prefix = StringUtils.isEmpty(host) ? "" : "${host}:"
		dir = dir.endsWith("/") ? dir : "${dir}/"
		"${prefix}${dir}"
	}
	
	def validate() {
		[remoteHost, remotePort, remoteKey].each { f ->
			Preconditions.checkNotNull(f, "Remote host settings missing (required: host, port, user and path to ssh key")
		}
		[inboxLocalDir, inboxRemoteDir, outboxLocalDir, outboxRemoteDir].each { f ->
			Preconditions.checkNotNull(f, "Not all sync paths are configured")
		}
	}
}
