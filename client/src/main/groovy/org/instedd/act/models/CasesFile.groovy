package org.instedd.act.models

import groovy.json.JsonBuilder
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class CasesFile {
	String name
	String path
	String guid
	Status status = Status.WAITING_UPLOAD

	public enum Status {
		WAITING_UPLOAD,
		UPLOADED,
		PROCESSING,
		IMPORTED,
		ERROR
	}
}
