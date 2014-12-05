package org.instedd.act.models

class LocationIndexTest extends GroovyTestCase {

	void "test matches first level locatios" () {
		def index = LocationIndex.build([
			[ geonameId: 1, name: "L1" ],
			[ geonameId: 2, name: "L2" ]
		])
		
		assert matches(index, "L1") == [["L1", 1]]
		assert matches(index, "L2") == [["L2", 2]]
	}
	
	void "test allows matching by prefix" () {
		def index = LocationIndex.build([
			[ geonameId: 1, name: "L1" ],
			[ geonameId: 2, name: "L2" ]
		])
		
		assert matches(index, "L") == [["L1", 1], ["L2", 2]]
	}
	
	void "test allows matching by substring" () {
		def index = LocationIndex.build([
			[ geonameId: 1, name: "ABCD" ]
		])
		
		assert matches(index, "B") == [["ABCD", 1]]
	}
	
	void "test allows matching by multiple path components" () {
		def index = LocationIndex.build([
			[ geonameId: 2,
			  name: "1",
			  children: [
				  [
					  geonameId: 2,
					  name: "2",
					  children: [
						  [
							  geonameId: 3,
							  name: "3"
						  ]
					  ]
				  ]
			  ]
			]
		])
		
		assert matchedNames(index, "1 2 3") == ["3"]
		assert matchedNames(index, "1 3") == ["3"]
		assert matchedNames(index, "2 3") == ["3"]
		
		assert matchedNames(index, "1 2") == ["2", "3"]
	}
	
	void "test allows splitting query with different separators"() {
		def index = LocationIndex.build([
			[ geonameId: 2,
			  name: "1",
			  children: [
				  [
					  geonameId: 2,
					  name: "2"
				  ]
			  ]
			]
		])
		
		assert matchedNames(index, "1 2") == ["2"]
		assert matchedNames(index, "1, 2") == ["2"]
		assert matchedNames(index, "1 - 2") == ["2"]
	}
	
	void "test search is case insensitive" () {
		def index = LocationIndex.build([
			[ geonameId: 1, name: "L1" ],
			[ geonameId: 2, name: "L2" ]
		])
		
		assert matches(index, "l") == [["L1", 1], ["L2", 2]]
	}
	
	void "test search ignores accents and diacritics" () {
		def index = LocationIndex.build([
			[ geonameId: 1, name: "à" ],
			[ geonameId: 2, name: "á" ],
			[ geonameId: 3, name: "ã" ],
			[ geonameId: 4, name: "ä" ]
		])
		
		assert matchedNames(index, "a") == ["à", "á", "ã", "ä"]
	}
	
	void "test includes child locations" () {
		def index = LocationIndex.build([
			[ geonameId: 1,
			  name: "L1",
			  children: [
				  [ geonameId: 11, name: "SL1" ],
				  [ geonameId: 12, name: "SL2" ]
			  ]
			]
		])
		
		assert matches(index, "sl") == [["SL1", 11], ["SL2", 12]]
	}

	void "test sublocations are indexed also by their parent name" () {
		def index = LocationIndex.build([
			[ geonameId: 1,
			  name: "A",
			  children: [
				  [ geonameId: 11, name: "B" ],
				  [ geonameId: 12, name: "C" ]
			  ]
			]
		])
		
		assert matchedNames(index, "a") == ["A", "B", "C"]
	}
	
	void "test results are sorted in path lexicographical order" () {
		def index = LocationIndex.build([
			[ geonameId: 1,
			  name: "LB",
			  children: [
				  [ geonameId: 11, name: "LD" ],
				  [ geonameId: 12, name: "LC" ]
			  ]
			],
			[ geonameId: 2,
			  name: "LA"
			],
		])
		
		assert matchedNames(index, "l") == ["LA", "LB", "LC", "LD"]
	}
	
	def matches(index, query) {
		index.matches(query).collect { l -> [l.name, l.id]  }
	}
		
	def matchedNames(index, query) {
		matches(index, query).collect { e -> e[0] }
	}
}
