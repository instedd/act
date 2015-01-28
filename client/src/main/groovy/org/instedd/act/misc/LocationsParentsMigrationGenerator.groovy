package org.instedd.act.misc

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.*

class LocationsParentsMigrationGenerator {
    File dataDir
    Boolean packed
    
    static main(args) {
        def generator = new LocationsParentsMigrationGenerator(dataDir: new File('/Users/mgarcia/Documents/workspace/geonames/children'), packed: false)
        def list = generator.listFrom([geonameId: 6255146, name: 'Africa'])
		generator.saveList(list, "json/locations/locations-hierarchy.json")
        println "Done generating the list"
    }
    
    def listFrom(location) {
		listFrom(location, [], [:])
    }
	
	def listFrom(location, parents, locations) {
        File cachedFile = jsonFile(location.geonameId)
        def childrenData = toJSON(cachedFile.text)
		childrenData.geonames.each { child ->
        	locations[child.geonameId.toString()] = parents
			listFrom(child, [child.geonameId.toString()] + parents, locations)
		}
        locations
    }
	
    def saveList(list, outputFile) {
        new File(outputFile).withWriter('UTF-8') { out ->
            def builder = new JsonBuilder(list)
            out.write(packed ? builder.toString() : builder.toPrettyString())
        }
    }
    
    def toJSON(json) {
        return new JsonSlurper().parseText(json)
    }
    
    def jsonFile(geonameId) {
        new File(dataDir, "${geonameId}.json")
    }
}
