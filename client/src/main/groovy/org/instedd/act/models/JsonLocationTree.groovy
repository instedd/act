package org.instedd.act.models

import groovy.json.JsonSlurper

class JsonLocationTree implements LocationTree {

    List<Location> rootLocations
    Map<Location, Location> hierarchy = [:]

    JsonLocationTree(File locationsFile) {
        if (!locationsFile.exists()) {
            throw new RuntimeException("Locations file ${locationsFile.absolutePath} doesn't exist")
        }
        this.rootLocations = parseLocations(new JsonSlurper().parseText(locationsFile.text))
    }
    
    def parseLocations(jsonLocations) {
        def locations = []
        jsonLocations.collect { jsonLocation ->
            def location = new Location(jsonLocation.geonameId, jsonLocation.name)
            hierarchy[location] = []
            locations.add(location)
            this.parseLocations(jsonLocation.children).each { child ->
                hierarchy[location].add(child)
            }
        }
        locations
    }

    @Override
    public List<Location> rootLocations() {
        rootLocations
    }

    @Override
    public List<Location> children(List<Location> path) {
        hierarchy[path.last()]
    }
}
