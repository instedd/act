package org.instedd.act.authentication

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import com.google.inject.Inject

class AuthenticationProcess {

	Logger logger = LoggerFactory.getLogger(AuthenticationProcess.class)
	
	@Inject ServerSignatureDownload signatureDownload
	@Inject DeviceKeyRegistration keyRegistration
	
	void ensureDone() {
		if (done) {
			logger.info "Device is already authenticated with the server"
		} else {
			logger.info "Will begin authentication process"
			new Thread({ runStep(signatureDownload) }).start()
		}
	}
	
	boolean isDone() {
		signatureDownload.done && keyRegistration.done
	}
	
	void runStep(AuthenticationStep step) {
		while(!step.ensureDone()) {
			logger.warn("Will retry last authentication step in 15 seconds") // TODO: move to settings?
			Thread.sleep(15000)
		}
		stepDone(step)
	}

	void stepDone(AuthenticationStep step) {
		if (step == signatureDownload) {
			runStep(keyRegistration)
		} else {
			logger.info "Authentication process finished. Device will be able to sync files when confirmed by the server."
		}
	}
}
