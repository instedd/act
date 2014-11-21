package org.instedd.act.authentication

import java.nio.charset.Charset;

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.instedd.act.Settings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.io.Files
import com.google.inject.Inject
class ServerSignatureDownload implements AuthenticationStep {

	Logger logger = LoggerFactory.getLogger(ServerSignatureDownload.class) 
	
	boolean skip
	
	File signatureFile
	URL signatureUrl
	HTTPBuilder http
	
	@Inject
	ServerSignatureDownload(Settings settings) {
		String urlSetting = settings.get("server.signatureUrl")
		skip = StringUtils.isEmpty(urlSetting)
		
		if (skip) {
			logger.info "Using default SSH known_hosts location"
		} else {
			this.signatureUrl = new URL(settings.get("server.signatureUrl"))
			this.http = new HTTPBuilder(signatureUrl)
			this.signatureFile = new File(settings.get("sync.serverSignatureLocation"))
		}
	}
	
	@Override
	public boolean ensureDone() {
		isDone() || attemptDownload()
	}

	@Override
	public boolean isDone() {
		skip || signatureFile.exists()
	}

	private boolean attemptDownload() {
		def success = false
		try {
			// TODO: set file charset
			http.request(Method.GET) {
				requestContentType = ContentType.TEXT
				response.success = { resp, reader ->
					def known_host_entry = "${signatureUrl.host} ${reader.text}"
					signatureFile.withWriter { out -> out.write known_host_entry }
					success = true
					logger.info "Successfully downloaded server signature"
				}
				response.failure = { r ->
					logger.warn("Server signature download was not successful. Server returned status code {}", r.status)
			   }
			}
		} catch (Exception e) {
			logger.warn("A problem occured communicating with the server to get its signature.", e)
		}
		return success;
	}
}
