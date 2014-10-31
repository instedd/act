package org.instedd.act.misc

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.*

class LocationsCrawler {
    HTTPBuilder http
    String path
    String username
    File outputDir
    Boolean packed
    
    static main(args) {
        def crawler = new LocationsCrawler(http: 'http://api.geonames.org/', path: 'childrenJSON', username: 'demo', outputDir: new File('json/geonames'), packed: true)
        def baseLocation = [geonameId: 6255146, name: 'Africa']
        crawler.crawlChildren(baseLocation)
        crawler.saveTree(baseLocation.children, "json/geonames/locations${crawler.packed ? '-packed' : ''}.json")
        println "Done crawling ${baseLocation.name} children"
    }
    
    def saveTree(tree, outputFile) {
        new File(outputFile).withWriter('UTF-8') { out ->
            def builder = new JsonBuilder(tree)
            out.write(packed ? builder.toString() : builder.toPrettyString())
        }
    }
    
    def setHttp(String url) {
        this.http = new HTTPBuilder(url)
    }
    
    def setOutputDir(File outputDir) {
        outputDir.mkdirs()
        this.outputDir = outputDir
    }
    
    def setOutputDir(String outputDir) {
        this.setOutputDir(new File(outputDir))
    }
    
    def isStatusMessageError(message) {
        message && !message.contains("no children for")
    }
    
    def save(id, data) {
        def isError = this.isStatusMessageError(data.status?.message)
        def newFile = new File(this.outputDir, "${ isError ? 'errored-' : ''}${id}.json")
        newFile.withWriter('UTF-8') { out ->
            out.writeLine(data.toString())
        }
        println "Writting ${id} to ${newFile.path}"
    }
    
    def toJSON(json) {
        return new JsonSlurper().parseText(json)
    }
    
    def jsonFile(geonameId) {
        new File(outputDir, "${geonameId}.json")
    }
    
    def crawlChildren(parent) {
        parent.children = []
        if(parent.numberOfChildren == 0) {
            println "Skipping location with no children"
            return
        } 
        File cachedFile = jsonFile(parent.geonameId)
        def cachedJson
        def cached = false
        if(cachedFile.exists()) {
            cachedJson = toJSON(cachedFile.text)
            def status = cachedJson.status?.message
            if(status) {
                if (!this.isStatusMessageError(status)) {
                    cached = true
                } else {
                    println "WARN: ignoring cached file because of status: ${cachedJson.status}"
                    cachedFile.delete()
                }
            } else {
                cached = true
            }
        }
        if (cached) {
            println "${parent.geonameId} cached in ${cachedFile.path}"
            this.parseChildren(cachedJson, parent)
        } else {
            http.request( GET, JSON ) {
                uri.path = path
                uri.query = [ geonameId: parent.geonameId, username: username ]
                
                response.success = { resp, json ->
                    this.save(parent.geonameId, json)
                    println "${uri} --> ${json}"
                
                    // parse the JSON response object:
                    this.parseChildren(json, parent)
                  }
                
                  // handler for any failure status code:
                  response.failure = { resp ->
                    println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
                  }
            }
        }
    }

    private parseChildren(json, parent) {
        json.geonames.each { data ->
            parent.children.add(data)
        }

        parent.children.each { child ->
            this.crawlChildren(child)
        }
    }
}
