package io.opencaesar.oml2owl

import io.opencaesar.oml.AnnotatedElement
import io.opencaesar.oml.Annotation
import io.opencaesar.oml.AnnotationProperty
import io.opencaesar.oml.Aspect
import io.opencaesar.oml.BooleanLiteral
import io.opencaesar.oml.Bundle
import io.opencaesar.oml.CardinalityRestrictionKind
import io.opencaesar.oml.Concept
import io.opencaesar.oml.ConceptInstance
import io.opencaesar.oml.ConceptTypeAssertion
import io.opencaesar.oml.DecimalLiteral
import io.opencaesar.oml.Description
import io.opencaesar.oml.DifferentFromPredicate
import io.opencaesar.oml.DoubleLiteral
import io.opencaesar.oml.EntityPredicate
import io.opencaesar.oml.EnumeratedScalar
import io.opencaesar.oml.FacetedScalar
import io.opencaesar.oml.ForwardRelation
import io.opencaesar.oml.Import
import io.opencaesar.oml.IntegerLiteral
import io.opencaesar.oml.KeyAxiom
import io.opencaesar.oml.LinkAssertion
import io.opencaesar.oml.Member
import io.opencaesar.oml.Ontology
import io.opencaesar.oml.QuotedLiteral
import io.opencaesar.oml.RangeRestrictionKind
import io.opencaesar.oml.Reference
import io.opencaesar.oml.RelationCardinalityRestrictionAxiom
import io.opencaesar.oml.RelationEntity
import io.opencaesar.oml.RelationEntityPredicate
import io.opencaesar.oml.RelationInstance
import io.opencaesar.oml.RelationPredicate
import io.opencaesar.oml.RelationRangeRestrictionAxiom
import io.opencaesar.oml.RelationTargetRestrictionAxiom
import io.opencaesar.oml.RelationTypeAssertion
import io.opencaesar.oml.ReverseRelation
import io.opencaesar.oml.Rule
import io.opencaesar.oml.SameAsPredicate
import io.opencaesar.oml.ScalarProperty
import io.opencaesar.oml.ScalarPropertyCardinalityRestrictionAxiom
import io.opencaesar.oml.ScalarPropertyRangeRestrictionAxiom
import io.opencaesar.oml.ScalarPropertyValueAssertion
import io.opencaesar.oml.ScalarPropertyValueRestrictionAxiom
import io.opencaesar.oml.SpecializationAxiom
import io.opencaesar.oml.Structure
import io.opencaesar.oml.StructureInstance
import io.opencaesar.oml.StructuredProperty
import io.opencaesar.oml.StructuredPropertyCardinalityRestrictionAxiom
import io.opencaesar.oml.StructuredPropertyRangeRestrictionAxiom
import io.opencaesar.oml.StructuredPropertyValueAssertion
import io.opencaesar.oml.StructuredPropertyValueRestrictionAxiom
import io.opencaesar.oml.Term
import io.opencaesar.oml.Vocabulary
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

class Oml2Owl extends OmlVisitor<Void> {
	
	public static val BUILT_IN_ONTOLOGIES = #[
		'http://www.w3.org/2001/XMLSchema',
		'http://www.w3.org/1999/02/22-rdf-syntax-ns',
		'http://www.w3.org/2000/01/rdf-schema',
		'http://www.w3.org/2002/07/owl'
	]
	
	val Resource inputResource 
	val OwlApi owl	
	var OWLOntology ontology
	
	new(Resource inputResource, OwlApi owl2) {
		this.inputResource = inputResource
		this.owl = owl2
	}
	
	def OWLOntology run() {
		inputResource.allContents.forEach[doSwitch]
		return ontology
	}

	override caseAnnotation(Annotation annotation) {
		annotation.annotatedElement.addsAnnotation(annotation)
		return null
	}

	override caseVocabulary(Vocabulary vocabulary) {
		ontology = owl.createOntology(vocabulary.iri)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.createIri(OmlConstants.Vocabulary))
		return null
	}

	override caseBundle(Bundle bundle) {
		ontology = owl.createOntology(bundle.iri)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.createIri(OmlConstants.Bundle))
		return null
	}

	override caseDescription(Description description) {
		ontology = owl.createOntology(description.iri)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.createIri(OmlConstants.Description))
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

	override caseRelationEntity(RelationEntity entity) {
		owl.addClass(ontology, entity.iri)
		owl.addSubClassOf(ontology, entity.iri, OmlConstants.RelationEntity)

		// source relation
		val sourceIri = entity.sourceIri
		owl.addObjectProperty(ontology, sourceIri)
		owl.addSubObjectPropertyOf(ontology, sourceIri, OmlConstants.sourceRelation)
		owl.addObjectPropertyDomain(ontology, sourceIri, entity.iri)
		owl.addObjectPropertyRange(ontology, sourceIri, entity.source.iri)
		owl.addFunctionalObjectProperty(ontology, sourceIri)
		if (entity.functional) {
			owl.addInverseFunctionalObjectProperty(ontology, sourceIri)
		}
		
		// target relation
		val targetIri = entity.targetIri
		owl.addObjectProperty(ontology, targetIri)
		owl.addSubObjectPropertyOf(ontology, targetIri, OmlConstants.targetRelation)
		owl.addObjectPropertyDomain(ontology, targetIri, entity.iri)
		owl.addObjectPropertyRange(ontology, targetIri, entity.target.iri)
		owl.addFunctionalObjectProperty(ontology, targetIri)
		if (entity.inverseFunctional) {
			owl.addInverseFunctionalObjectProperty(ontology, targetIri)
		}

		return null
	}

	override caseStructure(Structure structure) {
		owl.addClass(ontology, structure.iri)
		owl.addSubClassOf(ontology, structure.iri, OmlConstants.Structure)
		return null
	}

	override caseFacetedScalar(FacetedScalar scalar) {
		owl.addDatatype(ontology, scalar.iri)
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
		owl.addSubDataPropertyOf(ontology, propertyIri, OmlConstants.scalarProperty)
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
		owl.addSubObjectPropertyOf(ontology, propertyIri, OmlConstants.structuredProperty)
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
		
		// derivation rule for forward relation
		val antedecents = new ArrayList
		antedecents += owl.getObjectPropertyAtom(entity.sourceIri, 'r'.swrlIri, 's'.swrlIri)
		antedecents += owl.getObjectPropertyAtom(entity.targetIri, 'r'.swrlIri, 't'.swrlIri)
		val consequent = owl.getObjectPropertyAtom(forwardIri, 's'.swrlIri, 't'.swrlIri)
		val annotation = owl.getAnnotation(RDFS.LABEL.toString, owl.getLiteral(forward.name+' derivation'))
		owl.addNRule(ontology, #[consequent], antedecents, annotation)
		return null
	}

	override caseReverseRelation(ReverseRelation reverse) {
		// reverse relation
		val reverseIri = reverse.iri
		owl.addObjectProperty(ontology, reverseIri)
		owl.addSubObjectPropertyOf(ontology, reverseIri, OmlConstants.reverseRelation)
		owl.addInverseProperties(ontology, reverseIri, reverse.inverse.iri)
		return null
	}

	override caseRule(Rule rule) {
		var annotations = rule.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (annotations.filter[property.getIRI == RDFS.LABEL].isEmpty) {
			annotations = new ArrayList(annotations)
			annotations += owl.getAnnotation(RDFS.LABEL.toString, owl.getLiteral(rule.name))
		}
		owl.addNRule(ontology, rule.consequent.map[atom], rule.antecedent.map[atom], annotations)
		return null
	}

	protected def createIndividual(StructureInstance instance) {
		val individual = owl.getAnonymousIndividual(instance.getId)
		instance.ownedPropertyValues.forEach[it.appliesTo(individual)]
		individual
	}
	
	override caseConceptInstance(ConceptInstance instance) {
		val instanceIri = instance.iri
		val individual = owl.addNamedIndividual(ontology, instanceIri)
		instance.ownedPropertyValues.forEach[it.appliesTo(individual)]
		return null
	}

	override caseRelationInstance(RelationInstance instance) {
		val instanceIri = instance.iri
		val individual = owl.addNamedIndividual(ontology, instanceIri)
		instance.ownedPropertyValues.forEach[it.appliesTo(individual)]
		return null
	}

	override caseImport(Import ^import) {
		val iri = ^import.importedOntology.iri
		if (isBuiltInOntology(iri)) {
			val indirectImports = ^import.closure[it.importedOntology.importsWithSource]
			indirectImports.forEach[i2|
				val iri2 = i2.importedOntology.iri
				if (!isBuiltInOntology(iri2)) {
					owl.addImportsDeclaration(ontology, iri2)
				}
			]
		} else {
			owl.addImportsDeclaration(ontology, iri)
		}
		return null
	}

	override caseSpecializationAxiom(SpecializationAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		axiom.specializingTerm.specializes(axiom.specializedTerm, axiom.owningReference, annotations)
		return null
	}

	override caseScalarPropertyRangeRestrictionAxiom(ScalarPropertyRangeRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == RangeRestrictionKind.ALL) {
			owl.addDataAllValuesFrom(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.range.iri, annotations)
		} else {
			owl.addDataSomeValuesFrom(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.range.iri, annotations)
		}
		return null
	}

	override caseScalarPropertyValueRestrictionAxiom(ScalarPropertyValueRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addDataHasValue(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.value.literal, annotations)
		return null
	}

	override caseScalarPropertyCardinalityRestrictionAxiom(ScalarPropertyCardinalityRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == CardinalityRestrictionKind.MIN) {
			owl.addDataMinCardinality(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		} else if (axiom.kind == CardinalityRestrictionKind.MAX) { 
			owl.addDataMaxCardinality(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		} else {
			owl.addDataExactCardinality(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		}
		return null
	}

	override caseStructuredPropertyRangeRestrictionAxiom(StructuredPropertyRangeRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.range.iri, annotations)
		} else {
			owl.addObjectSomeValuesFrom(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.range.iri, annotations)
		}
		return null
	}

	override caseStructuredPropertyValueRestrictionAxiom(StructuredPropertyValueRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectHasValue(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.value.createIndividual, annotations)
		return null
	}

	override caseStructuredPropertyCardinalityRestrictionAxiom(StructuredPropertyCardinalityRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == CardinalityRestrictionKind.MIN) {
			owl.addObjectMinCardinality(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		} else if (axiom.kind == CardinalityRestrictionKind.MAX) { 
			owl.addObjectMaxCardinality(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		} else {
			owl.addObjectExactCardinality(ontology, axiom.restrictingType.iri, axiom.property.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		}
		return null
	}

	override caseRelationRangeRestrictionAxiom(RelationRangeRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, axiom.restrictingType.iri, axiom.relation.iri, axiom.range.iri, annotations)
		} else {
			owl.addObjectSomeValuesFrom(ontology, axiom.restrictingType.iri, axiom.relation.iri, axiom.range.iri, annotations)
		}
		return null
	}
	
	override caseRelationTargetRestrictionAxiom(RelationTargetRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectHasValue(ontology, axiom.restrictingType.iri, axiom.relation.iri, axiom.target.iri, annotations)
		return null
	}

	override caseRelationCardinalityRestrictionAxiom(RelationCardinalityRestrictionAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (axiom.kind == CardinalityRestrictionKind.MIN) {
			owl.addObjectMinCardinality(ontology, axiom.restrictingType.iri, axiom.relation.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		} else if (axiom.kind == CardinalityRestrictionKind.MAX) { 
			owl.addObjectMaxCardinality(ontology, axiom.restrictingType.iri, axiom.relation.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		} else {
			owl.addObjectExactCardinality(ontology, axiom.restrictingType.iri, axiom.relation.iri, axiom.cardinality as int, axiom.range?.iri, annotations)
		}
		return null
	}
	
	override caseKeyAxiom(KeyAxiom axiom) {
		val annotations = axiom.ownedAnnotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addHasKey(ontology, axiom.restrictingType.iri, axiom.properties.map[iri], annotations)
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
		owl.addObjectPropertyAssertion(ontology, instanceIri, assertion.type.sourceIri, instance.source.iri)
		owl.addObjectPropertyAssertion(ontology, instanceIri, assertion.type.targetIri, instance.target.iri)
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

	protected dispatch def void specializes(Term specific, Term general, Reference owningReference, OWLAnnotation...annotations) {
		// all other cases are not mapped or mapped differently
	}

	protected dispatch def void specializes(Concept specific, Concept general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}
	
	protected dispatch def void specializes(Concept specific, Aspect general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(Aspect specific, Aspect general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(RelationEntity specific, RelationEntity general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
		owl.addSubObjectPropertyOf(ontology, specific.sourceIri, general.sourceIri, annotations)
		owl.addSubObjectPropertyOf(ontology, specific.targetIri, general.targetIri, annotations)
		owl.addSubObjectPropertyOf(ontology, specific.forward.iri, general.forward.iri, annotations)
		if (specific.reverse !== null && general.reverse !== null) {
			owl.addSubObjectPropertyOf(ontology, specific.reverse.iri, general.reverse.iri, annotations)
		} else {
			// it's not obvious yet how to handle the other reverse relation cases
		}
	}

	protected dispatch def void specializes(RelationEntity specific, Aspect general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(Structure specific, Structure general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addSubClassOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(EnumeratedScalar specific, EnumeratedScalar general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addDatatypeDefinition(ontology, specific.iri, general.iri, annotations)
	}
	
	protected dispatch def void specializes(FacetedScalar specific, FacetedScalar general, Reference owningReference, OWLAnnotation...annotations) {
		if (owningReference !== null) {
			owl.addDatatypeDefinition(ontology, specific.iri, general.iri, annotations)
		} else {
			val restrictions = new ArrayList
			if (specific.length !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.LENGTH, owl.getLiteral(specific.length))
			}
			if (specific.maxLength !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.MAX_LENGTH, owl.getLiteral(specific.length))
			}
			if (specific.minLength !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.MIN_LENGTH, owl.getLiteral(specific.length))
			}
			if (specific.pattern !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.PATTERN, owl.getLiteral(specific.pattern))
			}
			if (specific.language !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.LANG_RANGE, owl.getLiteral(specific.language))
			}
			if (specific.minInclusive !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.MIN_INCLUSIVE, getLiteral(specific.minInclusive))
			}
			if (specific.maxInclusive !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.MAX_INCLUSIVE, getLiteral(specific.maxInclusive))
			}
			if (specific.minExclusive !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.MIN_EXCLUSIVE, getLiteral(specific.minExclusive))
			}
			if (specific.maxExclusive !== null) {
				restrictions += owl.getFacetRestriction(OWLFacet.MAX_EXCLUSIVE, getLiteral(specific.maxExclusive))
			}
			if (!restrictions.isEmpty) {
				owl.addDatatypeDefinition(ontology, specific.iri, general.iri, restrictions, annotations)
			} else {
				owl.addDatatypeDefinition(ontology, specific.iri, general.iri, annotations)
			}
		}
	}

	protected dispatch def void specializes(ScalarProperty specific, ScalarProperty general, Reference owningReference, OWLAnnotation...annotations) {
		owl.addSubDataPropertyOf(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(StructuredProperty specific, StructuredProperty general, Reference owningReference, OWLAnnotation...annotations) {
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
		owl.getClassAtom(predicate.entity.iri, predicate.variable.swrlIri)
	}

	protected dispatch def getAtom(RelationEntityPredicate predicate) {
		owl.getObjectPropertyAtom(predicate.entity.sourceIri, predicate.entityVariable.swrlIri, predicate.variable1.swrlIri)
		owl.getObjectPropertyAtom(predicate.entity.targetIri, predicate.entityVariable.swrlIri, predicate.variable2.swrlIri)
	}

	protected dispatch def getAtom(RelationPredicate predicate) {
		owl.getObjectPropertyAtom(predicate.relation.iri, predicate.variable1.swrlIri, predicate.variable2.swrlIri)
	}

	protected dispatch def getAtom(SameAsPredicate predicate) {
		owl.getSameIndividualAtom(predicate.variable1.swrlIri, predicate.variable2.swrlIri)
	}

	protected dispatch def getAtom(DifferentFromPredicate predicate) {
		owl.getDifferentIndividualsAtom(predicate.variable1.swrlIri, predicate.variable2.swrlIri)
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
	
	protected def String getSourceIri(RelationEntity entity) {
		entity.ontology?.namespace+'has'+entity.name.toFirstUpper+'Source'
	}

	protected def String getTargetIri(RelationEntity entity) {
		entity.ontology?.namespace+'has'+entity.name.toFirstUpper+'Target'
	}
	
	protected def getSwrlIri(String variableName) {
		"urn:swrl#"+variableName
	}

	static def isBuiltInOntology(String iri) {
		BUILT_IN_ONTOLOGIES.contains(iri)
	}
}