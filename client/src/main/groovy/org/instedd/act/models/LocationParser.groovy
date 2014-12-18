package org.instedd.act.models

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Preconditions;

class LocationParser {

	def parse(InputStream inputStream) {
		def parser = new JsonFactory().createParser(inputStream)
		checkToken parser, JsonToken.START_ARRAY
		parseArray(parser)
	}
	
	def parseLocation(JsonParser parser) {
		Integer id
		String name
		JsonLocation[] children
		
		while(parser.nextToken() != JsonToken.END_OBJECT) {
			def key = parser.getText()
			
			Preconditions.checkNotNull(key)
			
			if (key == "geonameId") {
				id = parser.nextIntValue(0)
			} else if (key == "name") {
				name = parser.nextTextValue()
			} else if (key == "children"){
				checkToken parser, JsonToken.START_ARRAY
				children = parseArray(parser)
			} else {
				// skip value for unknown keys
				parser.nextToken()
			}
			
		}
		new JsonLocation([id: id, name: name, children: children])
	}
	
	def parseArray(JsonParser parser) {
		def ret = []
		while(true) {
			// we use this token's value to determine
			// whether the child array is finished or not
			def nextToken = parser.nextToken()
			
			if (nextToken == JsonToken.START_OBJECT) {
				ret.add parseLocation(parser)
			} else if (nextToken == JsonToken.END_ARRAY) {
				break
			} else {
				throw new IllegalStateException("Unexpectec token ${nextToken}. Expected ${JsonToken.START_OBJECT} or ${JsonToken.END_ARRAY}")
			}
		}
		ret
	}
	
	def checkToken(JsonParser parser, JsonToken expected) {
		checkToken parser.nextToken(), expected
	}
	
	def checkToken(JsonToken actual, JsonToken expected) {
		Preconditions.checkState(actual == expected, "Expected ${expected} but got ${actual}")
	}
	
	static class JsonLocation {
		int id
		String name
		JsonLocation[] children
		
		def hasChildren() { children != null }
	}
	
}
