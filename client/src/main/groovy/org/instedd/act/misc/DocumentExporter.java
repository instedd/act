package org.instedd.act.misc;

import org.instedd.act.models.Case;

public interface DocumentExporter {

	void exportDocuments();

	void exportCase(Case aCase);
}
