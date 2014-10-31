package org.instedd.act.sync

import org.instedd.act.sync.RsyncSynchronizer;

import spock.lang.*

class RsyncSynchronizerTest extends GroovyTestCase {

    void "ignore: run synchronizer"() {
        new RsyncSynchronizer([sourceDir: '/tmp/dir1/', targetDir: '/tmp/dir2']).syncDocuments()
    }

    void "test supports only dirs"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([sourceDir: 'src/', targetDir: 'tmp/'])
        assert rsync.commandLine() == "rsync -avz src/ tmp/"
    }

    void "test with remote host"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([sourceDir: 'src/', targetDir: 'tmp/', targetHost: 'someone@example.com'])
        rsync.commandLine() == "rsync -avz src/ someone@example.com:tmp/"
    }

    void "test with local and remote host"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([sourceDir: 'src/', sourceHost: 'myself@192.168.1.4', targetDir: 'tmp/', targetHost: 'someone@example.com'])
        rsync.commandLine() == "rsync -avz myself@192.168.1.4:src/ someone@example.com:tmp/"
    }

    void "test custom command"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([baseCommand: 'rsync', sourceDir: 'src/', sourceHost: 'myself@192.168.1.4', targetDir: 'tmp/', targetHost: 'someone@example.com'])
        rsync.commandLine() == "rsync myself@192.168.1.4:src/ someone@example.com:tmp/"
    }

}
