package org.instedd.act.models

import com.google.common.base.Preconditions
import com.google.common.collect.Ordering


class Location {

	String id;
	String name;
	Location parent
	String pathString;
	
	Location(String id, String name) {
		this(id, name, null)
	}
	
	Location(String id, String name, Location parent) {
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

	def buildPathString() {
		def path = [this.name]
		def p = parent
		while (p != null) {
			path.add(p.name)
			p = p.parent
		}
		path.reverse().join(" - ")
	}

	
	static Comparator listingComparator() {
		Ordering.natural().compound([
			Ordering.natural().onResultOf({l -> l.pathString }),
			Ordering.natural().onResultOf({l -> l.id }),
		])
	}
	
}
