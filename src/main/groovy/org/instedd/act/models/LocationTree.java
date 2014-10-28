package org.instedd.act.models;

import java.util.List;

public interface LocationTree {

	List<String> rootLocations();

	List<String> children(String[] path);
	
}
