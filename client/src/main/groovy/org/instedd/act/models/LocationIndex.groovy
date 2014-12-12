package org.instedd.act.models

import groovy.json.JsonSlurper

import java.text.Normalizer
import java.util.regex.Pattern

import com.google.common.base.Splitter
import com.google.common.collect.FluentIterable

class LocationIndex {

	static final String DEFAULT_LOCATION_JSON = "json/locations-packed.json"

	/*
	 * each location is indexed by a list containing the normalized names of
	 * each component of its path. 
	 * 
	 * for example, Foo>Bar>Baz will be indexed by key ["FOO", "BAR", "BAZ"]
	 * 
	 * 
	 * during a lookup, the query is tokenized and normalized. the result will
	 * be the value for all entries where all terms of the query match any of 
	 * the entry components.
	 * 
	 * for example, the query "foo - bar" generates two terms: "FOO" and "BAR"
	 * 
	 * Foo>Bar>Baz will therefore be present in the result, since both terms
	 * are matched by the key ["FOO", "BAR", "BAZ"]
	 * 
	 */
	List<Entry> entries;

	LocationIndex(List<Entry> entries) {
		this.entries = entries
	}
			
	Collection<Location> matches(String query) {
		def terms = tokenize(query).collect { t -> normalize(t) }

		FluentIterable.from(entries)
				.filter     { e -> e.matchesAll(terms) }
				.transform  { e -> e.location }
				.toImmutableSortedSet(Location.listingComparator())
	}
	
	def tokenize(String query) {
		Splitter.onPattern(" |,|-")
				.omitEmptyStrings()
				.split(query)
	}
	
	static LocationIndex build() {
		def locationsJson = new JsonSlurper().parseText(new File(DEFAULT_LOCATION_JSON).text);
		return build(locationsJson)
	}
	
	static def build(locationsJson) {
		List<Entry<Collection<String>, Location>> index = []
		locationsJson.each { l -> addLocation(index, [], l, null) }
		
		new LocationIndex(index.toList())
	}
	
	static def addLocation(index, currentPath, json, parentLocation) {
		currentPath.add(normalize(json.name))
		
		def newLocation = new Location(json.geonameId, json.name, parentLocation)
		def newEntry = Entry.from(currentPath.clone(), newLocation)
		
		index.add(newEntry)
		
		if (json.containsKey("children")) {
			json.children.each { child -> addLocation(index, currentPath, child, newLocation) }
		}
		
		currentPath.remove(currentPath.size() - 1)
	}

	static def normalize(String key) {
		String temp = Normalizer.normalize(key, Normalizer.Form.NFD)
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
		return pattern.matcher(temp).replaceAll("").toUpperCase()
	}
	
	static class Entry {
		List<String> key
		Location location
		
		def matches(String term) {
			key.any { normalizedName -> normalizedName.contains(term) }
		}
		
		def matchesAll(Iterable<String> terms) {
			terms.every { t -> this.matches(t) }
		}
		
		static Entry from(List<String> key, Location location) {
			new Entry([key: key, location: location])
		}
	}	
}
