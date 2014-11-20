package org.instedd.act.authentication

import groovy.json.JsonBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient

import org.instedd.act.Settings
import org.instedd.act.models.DataStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.inject.Inject

/**
 * This is the process of registering the client public key
 * to the server.
 * 
 * It needs to be performed after the user submits the basic
 * device information, so that the server administrator can
 * decide whether to confirm this key or not.
 */
class DeviceKeyRegistration implements AuthenticationStep {

	Logger logger = LoggerFactory.getLogger(DeviceKeyRegistration.class)

	HTTPBuilder http
	DataStore dataStore
	String publicKey
	
	@Inject
	DeviceKeyRegistration(Settings settings,
				 Credentials credentials,
				 DataStore dataStore) {
		this.http = new HTTPBuilder(settings.get("server.apiUrl"))
		this.publicKey = credentials.publicKeyText()
		this.dataStore = dataStore
	}

	@Override
	public boolean ensureDone() {
		isDone() || attemptRegister()
	}

	@Override
	public boolean isDone() {
		dataStore.isDeviceRegistered()
	}

	private boolean attemptRegister() {
		if (!dataStore.userInfoCompleted()) {
			logger.info "Will skip device key registration until user completes device registration information"
			return false
		}
		
		logger.info "Attempting to register device public key with server"
		
		def success = false
		try {
			http.request(Method.POST) {
				uri.path = "registration"
				requestContentType = ContentType.JSON
				body = [publicKey: publicKey, deviceInfo: dataStore.deviceInfo()]
				
				response.success = { r ->
					dataStore.markDeviceRegistered()
					success = true
					logger.info("Successfully registered public key with server")
				}
				response.failure = { r ->
					 logger.warn("Public key registration was not successful. Server returned status code {}", r.status)
				}
			}
		} catch (Exception e) {
			logger.warn("A problem occured communicating with the server to register publick key.", e)
		}
		return success
	}
				
}
