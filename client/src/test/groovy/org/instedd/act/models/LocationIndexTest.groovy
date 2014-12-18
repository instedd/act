package org.instedd.act.models

import org.instedd.act.models.LocationParser.JsonLocation

class LocationIndexTest extends GroovyTestCase {

	void "test matches first level locatios" () {
		def index = build([
			[ 1, "L1" ],
			[ 2, "L2" ]
		])
		
		assert matches(index, "L1") == [["L1", 1]]
		assert matches(index, "L2") == [["L2", 2]]
	}
	
	void "test allows matching by prefix" () {
		def index = build([
			[ 1, "L1" ],
			[ 2, "L2" ]
		])
		
		assert matches(index, "L") == [["L1", 1], ["L2", 2]]
	}
	
	void "test allows matching by substring" () {
		def index = build([
			[ 1, "ABCD" ]
		])
		
		assert matches(index, "B") == [["ABCD", 1]]
	}
	
	void "test allows matching by multiple path components" () {
		def index = build([
			[ 1, "1",
				[ [ 2, "2",
					[  [ 3, "3" ] ] ] ] ]
		])
		
		assert matchedNames(index, "1 2 3") == ["3"]
		assert matchedNames(index, "2 3") == ["3"]
		
		assert matchedNames(index, "1 2") == ["2", "3"]
	}
	
	void "test allows splitting query with different separators"() {
		def index = build([
			[ 2, "1",
			  [ [ 2, "2" ] ] ]
		])
		
		assert matchedNames(index, "1 2") == ["2"]
		assert matchedNames(index, "1, 2") == ["2"]
		assert matchedNames(index, "1 - 2") == ["2"]
	}
	
	void "test search is case insensitive" () {
		def index = build([
			[ 1, "L1" ],
			[ 2, "L2" ]
		])
		
		assert matches(index, "l") == [["L1", 1], ["L2", 2]]
	}
	
	void "test search ignores accents and diacritics" () {
		def index = build([
			[ 1, "à" ],
			[ 2, "á" ],
			[ 3, "ã" ],
			[ 4, "ä" ]
		])
		
		assert matchedNames(index, "a") == ["à", "á", "ã", "ä"]
	}
	
	void "test includes child locations" () {
		def index = build([
			[ 1, "L1",
			  [
				  [ 11, "SL1" ],
				  [ 12, "SL2" ]
			  ]
			]
		])
		
		assert matches(index, "sl") == [["SL1", 11], ["SL2", 12]]
	}

	void "test sublocations are indexed also by their parent name" () {
		def index = build([
			[ 1,
			  "A",
			  [
				  [ 11, "B" ],
				  [ 12, "C" ]
			  ]
			]
		])
		
		assert matchedNames(index, "a") == ["A", "B", "C"]
	}
	
	void "test results are sorted in path lexicographical order" () {
		def index = build([
			[ 1,
			  "LB",
			  [
				  [ 11, "LD" ],
				  [ 12, "LC" ]
			  ]
			],
			[ 2,
			  "LA"
			],
		])
		
		assert matchedNames(index, "l") == ["LA", "LB", "LC", "LD"]
	}
	
	void "test result contains multiple entries for locations with conflicting names" () {
		def index = build([
			[ 1, "A" ],
			[ 2, "A" ],
		])
		
		assert matches(index, "A") == [["A", 1], ["A", 2]]
	}
	
	def build(jsonData) {
		def locations = jsonLocations(jsonData)
		LocationIndex.build(locations)
	}
	
	def jsonLocations(jsonData) {
		jsonData.collect { entry -> new JsonLocation([
			id: entry[0],
			name: entry[1],
			children: entry.size() > 2 ? jsonLocations(entry[2]) : null
		]) }
	}
	
	def matches(index, query) {
		index.matches(query).collect { l -> [l.name, l.id]  }
	}
		
	def matchedNames(index, query) {
		matches(index, query).collect { e -> e[0] }
	}
}
