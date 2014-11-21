

package org.instedd.act.authentication

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.common.base.Preconditions

class Credentials {

	static Logger logger = LoggerFactory.getLogger(Credentials.class)
	
	File privateKey
	File publicKey
	
	Credentials(File privateKey, File publicKey) {
		Preconditions.checkNotNull(publicKey)
		Preconditions.checkArgument(publicKey.exists() && publicKey.isFile())
		
		// private key file is not readable, we cannot validate it more than this.
		Preconditions.checkNotNull(privateKey)
		
		this.privateKey = privateKey
		this.publicKey = publicKey
	}
	
	String privateKeyPath() {
		privateKey.absolutePath
	}
	
	String publicKeyText() {
		publicKey.text
	}
	
	
	/** Initializes a new pair of SSH keys if necessary. */
	static Credentials initialize(String keysDirectoryPath) {
		def dir = new File(keysDirectoryPath)
		Preconditions.checkArgument(dir.exists() && dir.isDirectory(), "The directory ${keysDirectoryPath} should exist")
		
		def privateKey = new File(dir, "act_key")
		if (!privateKey.exists()) {
			def stdoutBuffer = new StringBuffer()
			def stderrBuffer = new StringBuffer()
			try {
				logger.info("Generating a new pair of SSH keys [{}]", privateKey.absolutePath)
				Process process = ["ssh-keygen", "-t", "rsa", "-N", "", "-f", privateKey.path].execute()
				process.consumeProcessOutput(stdoutBuffer, stderrBuffer)
				process.waitForOrKill(5000)
			} catch (Exception e) {
				logger.error("A problem occurred while generating ssh keys. Process stdout:\n{}\n Process stdin:\n{}\n",
							[stdoutBuffer.toString(), stderrBuffer.toString()])
			}
		}

		// TODO: register keys in server
				
		return new Credentials(privateKey, new File(dir, "act_key.pub"));
	}
	
		
}
