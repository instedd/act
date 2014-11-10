package org.instedd.act.sync

import org.instedd.act.sync.RsyncSynchronizer;

import com.google.common.io.Files;

import spock.lang.*

class RsyncSynchronizerTest extends GroovyTestCase {

    void "ignore: run synchronizer"() {
        new RsyncSynchronizer([localOutboxDir: '/tmp/dir1/', remoteOutboxDir: '/tmp/dir2']).syncDocuments()
    }

    void "test supports only dirs"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([localOutboxDir: 'src/', remoteOutboxDir: 'tmp/'])
        assert rsync.uploadCommandLine() == "rsync -iaz --remove-source-files src/ tmp/"
    }
	
	void "test adds trailing slash to sourceDir if not present to avoid recreating root directory on sync"() {
		RsyncSynchronizer rsync = new RsyncSynchronizer([localOutboxDir: 'src', remoteOutboxDir: 'tmp/'])
		assert rsync.uploadCommandLine() == "rsync -iaz --remove-source-files src/ tmp/"
	}

    void "test with remote host"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([localOutboxDir: 'src/', remoteOutboxDir: 'tmp/', outboxHost: 'someone@example.com'])
        assert rsync.uploadCommandLine() == "rsync -iaz --remove-source-files src/ someone@example.com:tmp/"
    }

    void "test with local and remote host"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([localOutboxDir: 'src/', remoteOutboxDir: 'tmp/', outboxHost: 'someone@example.com'])
        assert rsync.uploadCommandLine() == "rsync -iaz --remove-source-files src/ someone@example.com:tmp/"
    }

    void "test custom command"() {
        RsyncSynchronizer rsync = new RsyncSynchronizer([baseCommand: 'rsync', localOutboxDir: 'src/', remoteOutboxDir: 'tmp/', outboxHost: 'someone@example.com'])
        assert rsync.uploadCommandLine() == "rsync src/ someone@example.com:tmp/"
    }

}
