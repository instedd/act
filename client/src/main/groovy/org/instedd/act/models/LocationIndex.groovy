package org.instedd.act.models

import groovy.json.JsonSlurper

import java.text.Normalizer
import java.util.Map.Entry
import java.util.regex.Pattern

import com.google.common.base.Splitter;

class LocationIndex {

	static final String DEFAULT_LOCATION_JSON = "json/locations-packed.json"

	/*
	 * each location is indexed in a list containing the
	 * normalized names of each component of its path. 
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
	List<Entry<Collection<String>, Collection<Location>>> entries;

	LocationIndex(List entries) {
		this.entries = entries
	}
			
	Collection<Location> matches(String query) {
		def terms = Splitter.onPattern(" |,|-")
							.omitEmptyStrings()
							.split(query)
							.collect { t -> normalize(t) }

				
		def matches = []
		
		for(Entry entry : entries) {
			def include = terms.every { t -> keyMatches(entry.key, t) }
			if (include) { matches.addAll(entry.value) }
		}

		def resultSet = new TreeSet(Location.listingComparator())
		resultSet.addAll matches
		resultSet
	}
	
	def keyMatches(Collection<String> key, String queryTerm) {
		key.any { l -> l.contains(queryTerm) }
	}
	
	static LocationIndex build() {
		def locationsJson = new JsonSlurper().parseText(new File(DEFAULT_LOCATION_JSON).text);
		return build(locationsJson)
	}
	
	static def build(locationsJson) {
		Map<List<String>, List<Location>> index = [:]
		locationsJson.each { l -> addLocation(index, [], l, null) }
		
		new LocationIndex(index.entrySet().toList())
	}
	
	static def addLocation(index, currentPath, json, parentLocation) {
		currentPath.add(normalize(json.name))
		
		def newPath = currentPath.clone();
		def currentMatches = index[newPath]
		if (currentMatches == null) {
			currentMatches = []
			index[newPath] = currentMatches
		}
		def newLocation = new Location(json.geonameId, json.name, parentLocation)
		currentMatches.add(newLocation)
		
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
}
