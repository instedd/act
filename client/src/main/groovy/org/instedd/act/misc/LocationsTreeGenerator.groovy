package org.instedd.act.misc

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.*

class LocationsTreeGenerator {
    File dataDir
    Boolean packed
    
    static main(args) {
        def generator = new LocationsTreeGenerator(dataDir: new File('../json/geonames'), packed: false)
        def tree = generator.treeFrom([geonameId: 6255146, name: 'Africa'])
		tree.children.each { firstLevelDivision ->
			firstLevelDivision.children.each { secondLevelDivision ->
				generator.saveTree(secondLevelDivision.children, "json/locations/${secondLevelDivision.geonameId}.json")
				secondLevelDivision.remove("children")
			}
		}
		generator.saveTree(tree.children, "json/locations/locations.json")
        println "Done generating the tree"
    }
    
    def treeFrom(location) {
        File cachedFile = jsonFile(location.geonameId)
        def childrenData = toJSON(cachedFile.text)
        location.children = childrenData.geonames.collect { child ->
            treeFrom([geonameId: child.geonameId, name: child.name])
        }
        location
    }
    
    def saveTree(tree, outputFile) {
        new File(outputFile).withWriter('UTF-8') { out ->
            def builder = new JsonBuilder(tree)
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
