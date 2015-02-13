package org.instedd.act.models

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;

/**
 * Parses the location json file.
 * Uses Jackson's low level streaming parsing API to reduce memory footprint.
 * 
 * Instances of this class are not reusable.
 */
class LocationParser {

	JsonParser stream
	
	def parse(InputStream inputStream) {
		stream = new JsonFactory().createParser(inputStream)
		checkToken JsonToken.START_ARRAY
		parseArray()
	}
	
	def parseLocation() {
		String id
		String name
		JsonLocation[] children
		
		while(stream.nextToken() != JsonToken.END_OBJECT) {
			def key = stream.getText()
			Preconditions.checkNotNull(key)
			
			if (key == "gadm_geo_id") {
				id = stream.nextTextValue()
			} else if (key == "name") {
				name = stream.nextTextValue()
			} else if (key == "children"){
				checkToken JsonToken.START_ARRAY
				children = parseArray()
			} else {
				// skip value for unknown keys
				stream.nextToken()
			}
			
		}
		new JsonLocation([id: id, name: name, children: children])
	}
	
	def parseArray() {
		def ret = []
		def nextToken
		
		while((nextToken = stream.nextToken()) != JsonToken.END_ARRAY) {
			checkToken(nextToken, JsonToken.START_OBJECT)
			ret.add parseLocation()
		}
		
		ret
	}
	
	def checkToken(JsonToken expected) {
		checkToken stream.nextToken(), expected
	}
	
	def checkToken(JsonToken actual, JsonToken expected) {
		Preconditions.checkState(actual == expected, "Expected ${expected} but got ${actual}")
	}
	
	static class JsonLocation {
		String id
		String name
		JsonLocation[] children
		
		def hasChildren() { children != null }
	}
	
}
