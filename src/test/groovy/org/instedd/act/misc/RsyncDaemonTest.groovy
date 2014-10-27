package org.instedd.act.misc

import spock.lang.*

class RsyncDaemonTest extends Specification{

    def "supports only dirs"() {
        when:
        RsyncDaemon rsync = new RsyncDaemon([sourceDir: 'src/', targetDir: 'tmp/'])
        then:
        rsync.commandLine() == "rsync -avz --remove-source-files src/ tmp/"
    }

    def "with remote host"() {
        when:
        RsyncDaemon rsync = new RsyncDaemon([sourceDir: 'src/', targetDir: 'tmp/', targetHost: 'someone@example.com'])
        then:
        rsync.commandLine() == "rsync -avz --remove-source-files src/ someone@example.com:tmp/"
    }
    
    def "with local and remote host"() {
        when:
        RsyncDaemon rsync = new RsyncDaemon([sourceDir: 'src/', sourceHost: 'myself@192.168.1.4', targetDir: 'tmp/', targetHost: 'someone@example.com'])
        then:
        rsync.commandLine() == "rsync -avz --remove-source-files myself@192.168.1.4:src/ someone@example.com:tmp/"
    }
    
    def "custom command"() {
        when:
        RsyncDaemon rsync = new RsyncDaemon([baseCommand: 'rsync', sourceDir: 'src/', sourceHost: 'myself@192.168.1.4', targetDir: 'tmp/', targetHost: 'someone@example.com'])
        then:
        rsync.commandLine() == "rsync myself@192.168.1.4:src/ someone@example.com:tmp/"
    }
}
