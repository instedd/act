package org.instedd.act.models

import groovy.json.JsonSlurper

class JsonLocationTree implements LocationTree {

	File locationsDir
    List<Location> rootLocations
    Map<Location, Collection<Location>> hierarchy = [:]

    JsonLocationTree(File locationsDir) {
		this.locationsDir = locationsDir
    }
	
	def parseLocations(jsonLocations) {
        jsonLocations.collect { jsonLocation ->
            def location = new Location(jsonLocation.geonameId, jsonLocation.name)
			if(jsonLocation.containsKey("children")) {
				hierarchy[location] = this.parseLocations(jsonLocation.children)
			}
			location
        }
    }

    @Override
    public List<Location> rootLocations() {
		if(!rootLocations) {
			this.rootLocations = parseLocations(new JsonSlurper().parseText(new File(this.locationsDir, "locations.json").text))
		}
        rootLocations
    }

    @Override
    public List<Location> children(List<Location> path) {
		def parent = path.last()
		if(!hierarchy.containsKey(parent)) {
			hierarchy[parent] = parseLocations(new JsonSlurper().parseText(new File(this.locationsDir, "${parent.id}.json").text))
		}
        hierarchy[parent]
    }
}
