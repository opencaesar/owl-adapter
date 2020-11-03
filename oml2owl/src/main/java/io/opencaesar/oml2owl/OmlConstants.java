package io.opencaesar.oml2owl;

public interface OmlConstants {
	
	static final String omlPath = "opencaesar.io/oml";

	static final String omlIRI = "http://" + OmlConstants.omlPath;

	static final String omlNS = OmlConstants.omlIRI + "#";

	static final String ontologyType = OmlConstants.omlNS + "ontologyType";

	static final String Vocabulary = OmlConstants.omlNS + "Vocabulary";

	static final String VocabularyBundle = OmlConstants.omlNS + "VocabularyBundle";

	static final String Description = OmlConstants.omlNS + "Description";

	static final String DescriptionBundle = OmlConstants.omlNS + "DescriptionBundle";

	static final String Aspect = OmlConstants.omlNS + "Aspect";

	static final String Concept = OmlConstants.omlNS + "Concept";

	static final String RelationEntity = OmlConstants.omlNS + "RelationEntity";

	static final String Structure = OmlConstants.omlNS + "Structure";

	static final String forwardRelation = OmlConstants.omlNS + "forwardRelation";

	static final String reverseRelation = OmlConstants.omlNS + "reverseRelation";

	static final String sourceRelation = OmlConstants.omlNS + "sourceRelation";

	static final String targetRelation = OmlConstants.omlNS + "targetRelation";

	static final String inverseSourceRelation = OmlConstants.omlNS + "inverseSourceRelation";

	static final String inverseTargetRelation = OmlConstants.omlNS + "inverseTargetRelation";

	static final String structuredProperty = OmlConstants.omlNS + "structuredProperty";

	static final String scalarProperty = OmlConstants.omlNS + "scalarProperty";
}
