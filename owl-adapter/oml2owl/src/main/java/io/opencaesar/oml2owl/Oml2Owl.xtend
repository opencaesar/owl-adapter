package io.opencaesar.oml2owl

import io.opencaesar.oml.AnnotatedElement
import io.opencaesar.oml.Annotation
import io.opencaesar.oml.AnnotationProperty
import io.opencaesar.oml.Aspect
import io.opencaesar.oml.BooleanLiteral
import io.opencaesar.oml.Bundle
import io.opencaesar.oml.BundleExtension
import io.opencaesar.oml.BundleInclusion
import io.opencaesar.oml.CardinalityRestrictionKind
import io.opencaesar.oml.Concept
import io.opencaesar.oml.ConceptInstance
import io.opencaesar.oml.ConceptTypeAssertion
import io.opencaesar.oml.DecimalLiteral
import io.opencaesar.oml.Description
import io.opencaesar.oml.DescriptionExtension
import io.opencaesar.oml.DescriptionUsage
import io.opencaesar.oml.DoubleLiteral
import io.opencaesar.oml.EntityPredicate
import io.opencaesar.oml.EnumeratedScalar
import io.opencaesar.oml.FacetedScalar
import io.opencaesar.oml.ForwardRelation
import io.opencaesar.oml.IntegerLiteral
import io.opencaesar.oml.InverseRelation
import io.opencaesar.oml.KeyAxiom
import io.opencaesar.oml.LinkAssertion
import io.opencaesar.oml.Member
import io.opencaesar.oml.Ontology
import io.opencaesar.oml.RangeRestrictionKind
import io.opencaesar.oml.Reference
import io.opencaesar.oml.Relation
import io.opencaesar.oml.RelationCardinalityRestrictionAxiom
import io.opencaesar.oml.RelationEntity
import io.opencaesar.oml.RelationEntityPredicate
import io.opencaesar.oml.RelationEntityPredicateKind
import io.opencaesar.oml.RelationInstance
import io.opencaesar.oml.RelationPredicate
import io.opencaesar.oml.RelationRangeRestrictionAxiom
import io.opencaesar.oml.RelationTypeAssertion
import io.opencaesar.oml.Rule
import io.opencaesar.oml.ScalarProperty
import io.opencaesar.oml.ScalarPropertyRangeRestrictionAxiom
import io.opencaesar.oml.ScalarPropertyValueAssertion
import io.opencaesar.oml.ScalarPropertyValueRestrictionAxiom
import io.opencaesar.oml.SpecializationAxiom
import io.opencaesar.oml.Structure
import io.opencaesar.oml.StructureInstance
import io.opencaesar.oml.StructuredProperty
import io.opencaesar.oml.StructuredPropertyRangeRestrictionAxiom
import io.opencaesar.oml.StructuredPropertyValueAssertion
import io.opencaesar.oml.StructuredPropertyValueRestrictionAxiom
import io.opencaesar.oml.Term
import io.opencaesar.oml.Vocabulary
import io.opencaesar.oml.VocabularyExtension
import io.opencaesar.oml.util.OmlVisitor
import java.util.ArrayList
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.semanticweb.owlapi.model.OWLAnnotation
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.vocab.OWLFacet

import static extension io.opencaesar.oml.util.OmlRead.*
import io.opencaesar.oml.QuotedLiteral

class Oml2Owl extends OmlVisitor {

	val Resource inputResource 
	val OwlApi owl	
	var OWLOntology ontology
	
	new(Resource inputResource, OwlApi owl2) {
		this.inputResource = inputResource
		this.owl = owl2
	}
	
	def OWLOntology run() {
		visit(inputResource)
		return ontology
	}

	override caseAnnotation(Annotation annotation) {
		annotation.annotatedElement.addsAnnotation(annotation)
		return null
	}

	override caseVocabulary(Vocabulary vocabulary) {
		ontology = owl.createOntology(vocabulary.iri)
		owl.addImportsDeclaration(ontology, OmlConstants.OML)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.getLiteral(OmlConstants.Vocabulary))
		return null
	}

	override caseBundle(Bundle bundle) {
		ontology = owl.createOntology(bundle.iri)
		owl.addImportsDeclaration(ontology, OmlConstants.OML)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.getLiteral(OmlConstants.Bundle))
		return null
	}

	override caseDescription(Description description) {
		ontology = owl.createOntology(description.iri)
		owl.addImportsDeclaration(ontology, OmlConstants.OML)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.getLiteral(OmlConstants.Description))
		return null
	}

	override caseAspect(Aspect aspect) {
		owl.addClass(ontology, aspect.iri)
		owl.addSubClassOf(ontology, aspect.iri, OmlConstants.Aspect)
		return null
	}

	override caseConcept(Concept concept) {
		owl.addClass(ontology, concept.iri)
		owl.addSubClassOf(ontology, concept.iri, OmlConstants.Concept)
		return null
	}

	override caseRelationEntity(RelationEntity relation) {
		owl.addClass(ontology, relation.iri)
		owl.addSubClassOf(ontology, relation.iri, OmlConstants.RelationEntity)
		return null
	}

	override caseStructure(Structure structure) {
		owl.addClass(ontology, structure.iri)
		owl.addSubClassOf(ontology, structure.iri, OmlConstants.Structure)
		return null
	}

	override caseFacetedScalar(FacetedScalar scalar) {
		owl.addDatatype(ontology, scalar.iri)
		val restrictions = new ArrayList
		if (scalar.length !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.LENGTH, owl.getLiteral(scalar.length))
		}
		if (scalar.maxLength !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.MAX_LENGTH, owl.getLiteral(scalar.length))
		}
		if (scalar.minLength !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.MIN_LENGTH, owl.getLiteral(scalar.length))
		}
		if (scalar.pattern !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.PATTERN, owl.getLiteral(scalar.pattern))
		}
		if (scalar.language !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.LANG_RANGE, owl.getLiteral(scalar.language))
		}
		if (scalar.minInclusive !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.MIN_INCLUSIVE, getLiteral(scalar.minInclusive))
		}
		if (scalar.maxInclusive !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.MAX_INCLUSIVE, getLiteral(scalar.maxInclusive))
		}
		if (scalar.minExclusive !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.MIN_EXCLUSIVE, getLiteral(scalar.minExclusive))
		}
		if (scalar.maxExclusive !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.MAX_EXCLUSIVE, getLiteral(scalar.maxExclusive))
		}
		if (!restrictions.isEmpty) {
			owl.addDatatypeRestriction(ontology, scalar.iri, scalar.specializedScalar.iri, restrictions)
		}
		return null
	}

	override caseEnumeratedScalar(EnumeratedScalar scalar) {
		owl.addDataOneOf(ontology, scalar.iri, scalar.literals.map[literal])
		return null
	}

	override caseAnnotationProperty(AnnotationProperty property) {
		owl.addAnnotationProperty(ontology, property.iri)
		return null
	}

	override caseScalarProperty(ScalarProperty property) {
		val propertyIri = property.iri
		owl.addDataProperty(ontology, propertyIri)
		owl.addDataPropertyDomain(ontology, propertyIri, property.domain.iri)
		owl.addDataPropertyRange(ontology, propertyIri, property.range.iri)
		if (property.functional) {
			owl.addFunctionalDataProperty(ontology, propertyIri)
		}
		return null
	}

	override caseStructuredProperty(StructuredProperty property) {
		val propertyIri = property.iri
		owl.addObjectProperty(ontology, propertyIri)
		owl.addObjectPropertyDomain(ontology, propertyIri, property.domain.iri)
		owl.addObjectPropertyRange(ontology, propertyIri, property.range.iri)
		if (property.functional) {
			owl.addFunctionalObjectProperty(ontology, propertyIri)
		}
		return null
	}

	override caseForwardRelation(ForwardRelation forward) {
		val entity = forward.entity

		// forward relation
		val forwardIri = forward.iri
		owl.addObjectProperty(ontology, forwardIri)
		owl.addSubObjectPropertyOf(ontology, forwardIri, OmlConstants.forwardRelation)
		owl.addObjectPropertyDomain(ontology, forwardIri, forward.domain.iri)
		owl.addObjectPropertyRange(ontology, forwardIri, forward.range.iri)
		if (entity.functional) {
			owl.addFunctionalObjectProperty(ontology, forwardIri)
		}
		if (entity.inverseFunctional) {
			owl.addInverseFunctionalObjectProperty(ontology, forwardIri)
		}
		if (entity.symmetric) {
			owl.addSymmetricObjectProperty(ontology, forwardIri)
		}
		if (entity.asymmetric) {
			owl.addAsymmetricObjectProperty(ontology, forwardIri)
		}
		if (entity.reflexive) {
			owl.addReflexiveObjectProperty(ontology, forwardIri)
		}
		if (entity.irreflexive) {
			owl.addIrreflexiveObjectProperty(ontology, forwardIri)
		}
		if (entity.transitive) {
			owl.addTransitiveObjectProperty(ontology, forwardIri)
		}
		// forward source relation
		val forwardSourceIri = forward.sourceIri
		owl.addObjectProperty(ontology, forwardSourceIri)
		owl.addSubObjectPropertyOf(ontology, forwardSourceIri, OmlConstants.sourceRelation)
		owl.addObjectPropertyDomain(ontology, forwardSourceIri, entity.iri)
		owl.addObjectPropertyRange(ontology, forwardSourceIri, forward.domain.iri)
		owl.addFunctionalObjectProperty(ontology, forwardSourceIri)
		if (entity.functional) {
			owl.addInverseFunctionalObjectProperty(ontology, forwardSourceIri)
		}
		// forward target relation
		val forwardTargetIri = forward.targetIri
		owl.addObjectProperty(ontology, forwardTargetIri)
		owl.addSubObjectPropertyOf(ontology, forwardTargetIri, OmlConstants.targetRelation)
		owl.addObjectPropertyDomain(ontology, forwardTargetIri, entity.iri)
		owl.addObjectPropertyRange(ontology, forwardTargetIri, forward.range.iri)
		owl.addFunctionalObjectProperty(ontology, forwardTargetIri)
		if (entity.inverseFunctional) {
			owl.addInverseFunctionalObjectProperty(ontology, forwardTargetIri)
		}
		// derivation rule for forward relation
		val graphIri = forward.ontology.iri
		val antedecents = new ArrayList
		antedecents += owl.getObjectPropertyAtom(forwardSourceIri, graphIri+'r', graphIri+'s')
		antedecents += owl.getObjectPropertyAtom(forwardTargetIri, graphIri+'r', graphIri+'t')
		val consequent = owl.getObjectPropertyAtom(forwardIri, graphIri+'s', graphIri+'t')
		val annotation = owl.getAnnotation(RDFS.LABEL.toString, owl.getLiteral(forward.name+' derivation'))
		owl.addNRule(ontology, consequent, antedecents, annotation)
		return null
	}

	override caseInverseRelation(InverseRelation inverse) {
		// inverse relation
		val inverseIri = inverse.iri
		owl.addObjectProperty(ontology, inverseIri)
		owl.addSubObjectPropertyOf(ontology, inverseIri, OmlConstants.inverseRelation)
		owl.addInverseProperty(ontology, inverseIri, inverse.inverse.iri)
		return null
	}

	override caseRule(Rule rule) {
		var annotations = rule.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (annotations.filter[property.getIRI == RDFS.LABEL].isEmpty) {
			annotations = new ArrayList(annotations)
			annotations += owl.getAnnotation(RDFS.LABEL.toString, owl.getLiteral(rule.name))
		}
		owl.addNRule(ontology, rule.consequent.atom, rule.antecedent.map[atom], annotations)
		return null
	}

	protected def createIndividual(StructureInstance instance) {
		val individual = owl.getAnonymousIndividual(instance.getId)
		instance.ownedPropertyValues.forEach[appliesTo(individual)]
		individual
	}
	
	override caseConceptInstance(ConceptInstance instance) {
		val instanceIri = instance.iri
		val individual = owl.addNamedIndividual(ontology, instanceIri)
		instance.ownedPropertyValues.forEach[assertion|assertion.appliesTo(individual)]
		return null
	}

	override caseRelationInstance(RelationInstance instance) {
		val instanceIri = instance.iri
		val individual = owl.addNamedIndividual(ontology, instanceIri)
		instance.ownedPropertyValues.forEach[assertion|assertion.appliesTo(individual)]
		return null
	}

	override caseVocabularyExtension(VocabularyExtension _extension) {
		owl.addImportsDeclaration(ontology, _extension.extendedVocabulary.iri)
		return null
	}

	override caseBundleInclusion(BundleInclusion inclusion) {
		owl.addImportsDeclaration(ontology, inclusion.includedVocabulary.iri)
		return null
	}

	override caseBundleExtension(BundleExtension _extension) {
		owl.addImportsDeclaration(ontology, _extension.extendedBundle.iri)
		return null
	}

	override caseDescriptionUsage(DescriptionUsage usage) {
		owl.addImportsDeclaration(ontology, usage.usedTerminology.iri)
		return null
	}

	override caseDescriptionExtension(DescriptionExtension _extension) {
		owl.addImportsDeclaration(ontology, _extension.extendedDescription.iri)
		return null
	}

	override caseSpecializationAxiom(SpecializationAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		axiom.specializingTerm.specializes(axiom.specializedTerm, annotations)
		return null
	}

	override caseScalarPropertyRangeRestrictionAxiom(ScalarPropertyRangeRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == RangeRestrictionKind.ALL) {
			owl.addDataAllValuesFrom(ontology, axiom.restrictingTypeُ.iri, axiom.property.iri, axiom.range.iri, annotations)
		} else {
			owl.addDataSomeValuesFrom(ontology, axiom.restrictingTypeُ.iri, axiom.property.iri, axiom.range.iri, annotations)
		}
		return null
	}

	override caseScalarPropertyValueRestrictionAxiom(ScalarPropertyValueRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addDataHasValue(ontology, axiom.restrictingTypeُ.iri, axiom.property.iri, axiom.value.literal, annotations)
		return null
	}

	override caseStructuredPropertyRangeRestrictionAxiom(StructuredPropertyRangeRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, axiom.restrictingTypeُ.iri, axiom.property.iri, axiom.range.iri, annotations)
		} else {
			owl.addObjectSomeValuesFrom(ontology, axiom.restrictingTypeُ.iri, axiom.property.iri, axiom.range.iri, annotations)
		}
		return null
	}

	override caseStructuredPropertyValueRestrictionAxiom(StructuredPropertyValueRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectHasValue(ontology, axiom.restrictingTypeُ.iri, axiom.property.iri, axiom.value.createIndividual, annotations)
		return null
	}

	override caseRelationRangeRestrictionAxiom(RelationRangeRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, axiom.restrictingTypeُ.iri, axiom.relation.iri, axiom.range.iri, annotations)
		} else {
			owl.addObjectSomeValuesFrom(ontology, axiom.restrictingTypeُ.iri, axiom.relation.iri, axiom.range.iri, annotations)
		}
		return null
	}
	
	override caseRelationCardinalityRestrictionAxiom(RelationCardinalityRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == CardinalityRestrictionKind.MIN) {
			owl.addObjectMinCardinality(ontology, axiom.restrictingTypeُ.iri, axiom.relation.iri, axiom.cardinality as int, annotations)
		} else if (axiom.kind == CardinalityRestrictionKind.MAX) { 
			owl.addObjectMaxCardinality(ontology, axiom.restrictingTypeُ.iri, axiom.relation.iri, axiom.cardinality as int, annotations)
		} else {
			owl.addObjectExactCardinality(ontology, axiom.restrictingTypeُ.iri, axiom.relation.iri, axiom.cardinality as int, annotations)
		}
		return null
	}
	
	override caseKeyAxiom(KeyAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addHasKey(ontology, axiom.restrictingTypeُ.iri, axiom.properties.map[iri], annotations)
		return null
	}

	override caseConceptTypeAssertion(ConceptTypeAssertion assertion) {
		val annotations = assertion.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addClassAssertion(ontology, assertion.conceptInstance.iri, assertion.type.iri, annotations)
		return null
	}

	override caseRelationTypeAssertion(RelationTypeAssertion assertion) {
		val instance = assertion.relationInstance
		val instanceIri = instance.iri
		val annotations = assertion.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addClassAssertion(ontology, instanceIri, assertion.type.iri, annotations)
		owl.addObjectPropertyAssertion(ontology, instanceIri, assertion.type.forward.sourceIri, instance.source.iri)
		owl.addObjectPropertyAssertion(ontology, instanceIri, assertion.type.forward.targetIri, instance.target.iri)
		return null
	}

/* ------------------------- */

	protected dispatch def void addsAnnotation(AnnotatedElement element, Annotation annotation) {
		// all other cases are not mapped or mapped differently
	}

	protected dispatch def void addsAnnotation(Ontology omlOntology, Annotation annotation) {
		owl.addOntologyAnnotation(ontology, annotation.property.iri, annotation.value.literal)
	}

	protected dispatch def void addsAnnotation(Member member, Annotation annotation) {
		owl.addAnnotationAssertion(ontology, member.iri, annotation.property.iri, annotation.value.literal)
	}
	
	protected dispatch def void addsAnnotation(Reference reference, Annotation annotation) {
		reference.resolve.addsAnnotation(annotation)
	}

	protected dispatch def void specializes(Term specific, Term general, OWLAnnotation...annotations) {
		// all other cases are not mapped or mapped differently
	}

	protected dispatch def void specializes(Concept specific, Concept general, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}
	
	protected dispatch def void specializes(Concept specific, Aspect general, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(Aspect specific, Aspect general, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(RelationEntity specific, RelationEntity general, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(RelationEntity specific, Aspect general, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(ScalarProperty specific, ScalarProperty general, OWLAnnotation...annotations) {
		owl.addSubDataPropertyOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(StructuredProperty specific, StructuredProperty general, OWLAnnotation...annotations) {
		owl.addSubObjectPropertyOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void appliesTo(ScalarPropertyValueAssertion assertion, OWLIndividual individual) {
		val annotations = assertion.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addDataPropertyAssertion(ontology, individual, assertion.property.iri, assertion.value.literal, annotations)
	}

	protected dispatch def void appliesTo(StructuredPropertyValueAssertion assertion, OWLIndividual individual) {
		val annotations = assertion.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectPropertyAssertion(ontology, individual, assertion.property.iri, assertion.value.createIndividual, annotations)
	}

	protected dispatch def void appliesTo(LinkAssertion assertion, OWLNamedIndividual individual) {
		val annotations = assertion.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectPropertyAssertion(ontology, individual.getIRI.IRIString, assertion.relation.iri, assertion.target.iri, annotations)
	}

	protected dispatch def getAtom(EntityPredicate predicate) {
		owl.getClassAtom(predicate.entity.iri, predicate.variableIri)
	}

	protected dispatch def getAtom(RelationEntityPredicate predicate) {
		switch (predicate.kind) {
			case RelationEntityPredicateKind.SOURCE_TO_RELATION:
				owl.getObjectPropertyAtom(predicate.entity.forward.sourceIri, predicate.variable1Iri, predicate.variable2Iri)
			case RelationEntityPredicateKind.RELATION_TO_SOURCE:
				owl.getObjectPropertyInverseAtom(predicate.entity.forward.sourceIri, predicate.variable1Iri, predicate.variable2Iri)
			case RelationEntityPredicateKind.TARGET_TO_RELATION:
				owl.getObjectPropertyAtom(predicate.entity.forward.targetIri, predicate.variable1Iri, predicate.variable2Iri)
			case RelationEntityPredicateKind.RELATION_TO_TARGET:
				owl.getObjectPropertyInverseAtom(predicate.entity.forward.targetIri, predicate.variable1Iri, predicate.variable2Iri)
		}
	}

	protected dispatch def getAtom(RelationPredicate predicate) {
		if (predicate.inverse) {
			owl.getObjectPropertyInverseAtom(predicate.relation.iri, predicate.variable1Iri, predicate.variable2Iri)
		} else {
			owl.getObjectPropertyAtom(predicate.relation.iri, predicate.variable1Iri, predicate.variable2Iri)
		}
	}

	protected dispatch def getLiteral(QuotedLiteral literal) {
		if (literal.type !== null) {
			owl.getLiteralWithDatatype(literal.value.toString, literal.type.iri)
		} else if (literal.langTag !== null) {
			owl.getLiteralWithLangTag(literal.value.toString, literal.langTag)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(BooleanLiteral literal) {
		if (literal.type !== null) {
			owl.getLiteralWithDatatype(literal.value.toString, literal.type.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(IntegerLiteral literal) {
		if (literal.type !== null) {
			owl.getLiteralWithDatatype(literal.value.toString, literal.type.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(DecimalLiteral literal) {
		if (literal.type !== null) {
			owl.getLiteralWithDatatype(literal.value.toString, literal.type.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(DoubleLiteral literal) {
		if (literal.type !== null) {
			owl.getLiteralWithDatatype(literal.value.toString, literal.type.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}
	
	protected def String getSourceIri(Relation relation) {
		relation.iri + 'Source'
	}

	protected def String getTargetIri(Relation relation) {
		relation.iri + 'Target'
	}
}