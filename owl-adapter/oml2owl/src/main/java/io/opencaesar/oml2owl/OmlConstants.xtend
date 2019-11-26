package io.opencaesar.oml2owl

interface OmlConstants {
	
	static val OML = "http://opencaesar.io/oml#"
	
	static val ontologyType = OML+'ontologyType'

	// owl:Ontology oml:ontologyType ?x 
	
	static val Vocabulary = OML+'Vocabulary'
	
	static val Bundle = OML+'Bundle'

	static val Description = OML+'Description'
	
	// owl:Class rdfs:subClassOf x?
	
	static val Aspect = OML+'Aspect'
	
	static val Concept = OML+'Concept'

	static val RelationEntity = OML+'RelationEntity'

	static val Structure = OML+'Structure'
	

	// owl:ObjectProperty rdfs:subPropertyOf x?
	
	static val forwardRelation = OML+'forwardRelation'
	
	static val inverseRelation = OML+'inverseRelation'

	static val sourceRelation = OML+'sourceRelation'
	
	static val targetRelation = OML+'targetRelation'
}