package org.instedd.act.models

import groovy.json.JsonSlurper

import java.text.Normalizer
import java.util.Map.Entry
import java.util.regex.Pattern

import com.google.common.base.Splitter;

class LocationIndex {

	static final String DEFAULT_LOCATION_JSON = "json/locations-packed.json"

	List entries;

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
			def include = terms.every { t ->
							entry.key.any { l -> l.contains(t) }
							}
			
			if (include) { matches.addAll(entry.value) }
		}
		
		new TreeSet(matches)
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
