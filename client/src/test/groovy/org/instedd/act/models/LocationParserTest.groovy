package org.instedd.act.models

import groovy.json.JsonOutput;

import org.apache.commons.io.IOUtils;
import org.instedd.act.models.LocationParser.JsonLocation;

class LocationParserTest extends GroovyTestCase {
	
	void "test parses a single plain location" () {
		def locations = parseSingle([gadm_geo_id: "1", name: "Liberia"])

		assert locations.id == "1"
		assert locations.name == "Liberia"
	}
	
	void "test doesn't depend on key order" () {
		def location = parseSingle "{ \"name\": \"Liberia\", \"gadm_geo_id\": \"1\" }"

		assert location.id == "1"
		assert location.name == "Liberia"
	}
	
	void "test ignores unknown properties" () {
		def location = parseSingle([gadm_geo_id: "1", foo: "bar", name: "Liberia"])

		assert location.id == "1"
		assert location.name == "Liberia"
	}

	void "test explicitly skips values from unknown properties" () {
		def location = parseSingle([gadm_geo_id: "1", foo: "name", name: "Liberia"])

		assert location.id == "1"
		assert location.name == "Liberia"
	}
	
	void "test parses single location with single child" () {
		def result = parseSingle([
			gadm_geo_id: "0",
			name: "Liberia",
			children: [
				[ gadm_geo_id: "1", name: "Monrovia" ]
			]
		])
		
		assert attrs(result) == [
			"0",
			"Liberia",
			[
				["1", "Monrovia", null]
			]
		]
	}
	
	void "test parses single location with multiple children" () {
		def result = parseSingle([
			gadm_geo_id: "0",
			name: "Liberia",
			children: [
				[ gadm_geo_id: "1", name: "Monrovia" ],
				[ gadm_geo_id: "2", name: "Bopolu" ],
			]
		])
		
		assert attrs(result) == [
			"0",
			"Liberia",
			[
				["1", "Monrovia", null],
				["2", "Bopolu", null]
			]
		]
	}
	
	void "test parses single location with nested children" () {
		def result = parseSingle([
			gadm_geo_id: "0",
			name: "Liberia",
			children: [
				[
				gadm_geo_id: "1",
					name: "Monrovia",
					children: [
						[ gadm_geo_id: "2", name: "Matadi" ],
						[ gadm_geo_id: "3", name: "Tomo" ],
					]
				],
				[
				gadm_geo_id: "4",
					name: "Bopolu"
				],
			]
		])
		
		assert attrs(result) == [
			"0",
			"Liberia",
			[
				[
					"1",
					"Monrovia",
					[
						["2", "Matadi", null],
						["3", "Tomo", null]
					]
				],
				["4", "Bopolu", null]
			]
		]
	}
	
	void "test parses multiple locations" () {
		def result = parse([
			[ gadm_geo_id: "0", name: "Liberia" ],
			[ gadm_geo_id: "1", name: "Sierra Leone" ]
		])
		
		assert result.size() == 2
		

		assert attrs(result[0]) == ["0", "Liberia", null]
		assert attrs(result[1]) == ["1", "Sierra Leone", null]
	}
	
	void "test children can appear before parent attributes" () {
		def result = parseSingle([
			children: [
				[ gadm_geo_id: "1", name: "Sierra Leone" ]
			],
			gadm_geo_id: "0",
			name: "Liberia"
		])
		
		assert attrs(result) == [
			"0",
			"Liberia",
			[ [ "1", "Sierra Leone",null] ]
		]
	}
	
	void "test parse full tree" () {
		def inputStream = new FileInputStream(LocationIndex.DEFAULT_LOCATION_JSON)
		try {
			def result = new LocationParser().parse(inputStream)
			assert result.size == 3
		} finally {
			IOUtils.closeQuietly(inputStream)
		}
	}
	
	def attrs(JsonLocation ret) {
		[
			ret.id,
			ret.name,
			ret.children == null ? null : ret.children.collect { l -> attrs(l) }
		]
	}
	
	def parseSingle(jsonData) {
		parseSingle(JsonOutput.toJson(jsonData))
	}
	
	def parseSingle(String json) {
		def ret = parse("[" + json + "]")
		assert ret.size() == 1
		ret[0]
	}
	
	def parse(jsonData) {
		def json = JsonOutput.toJson(jsonData)
		parse(json) 
	}
	
	def parse(String json) {
		def inputStream = IOUtils.toInputStream(json)
		new LocationParser().parse(inputStream)
	}

}
