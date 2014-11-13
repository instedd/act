package org.instedd.act.misc;

import org.instedd.act.models.Case;

public interface DocumentExporter {

	void exportDocuments();

	void exportDeviceInfo();
	
	void exportCase(Case aCase);
}
