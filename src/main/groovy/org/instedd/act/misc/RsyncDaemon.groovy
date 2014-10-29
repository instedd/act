package org.instedd.act.misc


class RsyncDaemon {
    String baseCommand = "rsync -avz --remove-source-files"
    String sourceDir
    String targetDir
    String sourceHost
    String targetHost
    Boolean waitingForSync = false
    
    def requestSync() {
        if (!waitingForSync) {
            this.waitingForSync = true
            Thread.start {
                this.sync()
                this.waitingForSync = false
            }
        }
    }

    synchronized sync() {
        def stdout = new StringBuffer()
        def stderr = new StringBuffer()
        
        def command = this.commandLine()
        println(command)
        
        Process process = command.execute()
        process.consumeProcessOutput(stdout, stderr)
        process.waitFor()
        
        println(stdout)
        println(stderr)
    }

    def commandLine() {
        String command = "${baseCommand} "
        if (sourceHost) {
            command += "${sourceHost}:"
        }
        command += "${sourceDir} "
        if (targetHost) {
            command += "${targetHost}:"
        }
        command += "${targetDir}"
    }
}
