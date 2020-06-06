package io.opencaesar.oml2owl

interface OmlConstants {
	
	static val omlPath = "opencaesar.io/oml"

	static val omlIRI = "http://"+omlPath

	static val omlNS = omlIRI+ '#'
	
	// oml:ontologyType annotation
	
	static val ontologyType = omlNS+'ontologyType'

	// owl:Ontology oml:ontologyType ?x 
	
	static val Vocabulary = omlNS+'Vocabulary'
	
	static val VocabularyBundle = omlNS+'VocabularyBundle'

	static val Description = omlNS+'Description'
	
	static val DescriptionBundle = omlNS+'DescriptionBundle'

	// owl:Class rdfs:subClassOf x?
	
	static val Aspect = omlNS+'Aspect'
	
	static val Concept = omlNS+'Concept'

	static val RelationEntity = omlNS+'RelationEntity'

	static val Structure = omlNS+'Structure'

	// owl:ObjectProperty rdfs:subPropertyOf x?
	
	static val forwardRelation = omlNS+'forwardRelation'
	
	static val reverseRelation = omlNS+'reverseRelation'

	static val sourceRelation = omlNS+'sourceRelation'
	
	static val targetRelation = omlNS+'targetRelation'

	static val structuredProperty = omlNS+'structuredProperty'

	static val scalarProperty = omlNS+'scalarProperty'
}