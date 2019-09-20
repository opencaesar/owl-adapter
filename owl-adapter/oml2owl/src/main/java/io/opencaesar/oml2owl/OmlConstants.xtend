package io.opencaesar.oml2owl

interface OmlConstants {
	
	static val OML = "http://opencaesar.io/oml#"
	
	static val ontologyType = OML+'ontologyType'

	// owl:Ontology oml:ontologyType XXX 
	
	static val Terminology = OML+'Terminology'
	
	static val Description = OML+'Description'
	
	// owl:Class rdfs:subClassOf XXX 
	
	static val Aspect = OML+'Aspect'
	
	static val Concept = OML+'Concept'

	static val ReifiedRelationship = OML+'ReifiedRelationship'

	static val Structure = OML+'Structure'
	

	// owl:ObjectProperty rdfs:subPropertyOf XXX 
	
	static val reifiedRelationshipForward = OML+'reifiedRelationshipForward'
	
	static val reifiedRelationshipInverse = OML+'reifiedRelationshipInverse'

	static val unreifiedRelationshipForward = OML+'unreifiedRelationshipForward'
	
	static val unreifiedRelationshipInverse = OML+'unreifiedRelationshipInverse'
}