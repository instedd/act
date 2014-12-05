package org.instedd.act.models

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;


class Location implements Comparable<Location>{

	long id;
	String name;
	Location parent
	String pathString;
	
	Location(long id, String name, Location parent) {
		Preconditions.checkNotNull(id)
		Preconditions.checkNotNull(name)
		this.id = id
		this.name = name
		this.parent = parent
		this.pathString = buildPathString()
	}

	String toString() {
		pathString
	}

	@Override
	public int compareTo(Location o) {
		name.compareTo(o.name)
	}

	def buildPathString() {
		def path = [this.name]
		def p = parent
		while (p != null) {
			path.add(p.name)
			p = p.parent
		}
		path.reverse().join(" - ")
	}
			
}
