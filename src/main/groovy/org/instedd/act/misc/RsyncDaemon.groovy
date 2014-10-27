package org.instedd.act.misc


class RsyncDaemon {
    String baseCommand = "rsync -avz --remove-source-files"
    String sourceDir
    String targetDir
    String sourceHost
    String targetHost
    
    def watch() {
        while (true) {
            try {
                this.sync()
                System.sleep(5000)
            } catch (Exception exception) {
                println("Exception while rsync watching:")
                exception.printStackTrace();
            }
        }
    }

    def sync() {
        def command = this.commandLine()
        println(command)
        def process = command.execute()
        println(process.text)
        println(process.err.text)
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
