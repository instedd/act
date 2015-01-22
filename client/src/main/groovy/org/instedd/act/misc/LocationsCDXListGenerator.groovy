package org.instedd.act.misc

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.*

class LocationsCDXListGenerator {
    File dataDir
    Boolean packed
    
    static main(args) {
        def generator = new LocationsCDXListGenerator(dataDir: new File('/Users/mgarcia/Documents/workspace/geonames/children'), packed: true)
        def list = generator.listFrom([geonameId: 6255146, name: 'Africa'])
		generator.saveList(list, "json/locations/cdx-locations-packed.json")
        println "Done generating the list"
    }
    
    def listFrom(location) {
		listFrom(location, 0, [:])
    }
	
	def listFrom(location, administrativeLevel, locations) {
        File cachedFile = jsonFile(location.geonameId)
        def childrenData = toJSON(cachedFile.text)
		childrenData.geonames.each { child ->
        	locations[child.geonameId.toString()] = toChildLocation(child, location, administrativeLevel)
			listFrom(child, administrativeLevel + 1, locations)
		}
        locations
    }
	
	def toChildLocation(child, parent, administrativeLevel) {
		def childLocation = [name: child.name, lat: child.lat, lng: child.lng, level: administrativeLevel]
		if(administrativeLevel > 0) {
			childLocation.parent_id = parent.geonameId.toString()
		}
		childLocation
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
