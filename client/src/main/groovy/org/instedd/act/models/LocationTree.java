package org.instedd.act.models;

import java.util.List;

public interface LocationTree {

	List<Location> rootLocations();

	List<Location> children(List<Location> path);
	
}
