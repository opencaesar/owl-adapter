package io.opencaesar.oml2owl

import io.opencaesar.oml.AnnotatedElement
import io.opencaesar.oml.Annotation
import io.opencaesar.oml.AnnotationProperty
import io.opencaesar.oml.Aspect
import io.opencaesar.oml.BinaryScalar
import io.opencaesar.oml.CharArrayScalar
import io.opencaesar.oml.Concept
import io.opencaesar.oml.ConceptInstance
import io.opencaesar.oml.ConceptInstanceTypeAssertion
import io.opencaesar.oml.Description
import io.opencaesar.oml.DescriptionRefinement
import io.opencaesar.oml.DescriptionUsage
import io.opencaesar.oml.DirectionalRelationshipPredicate
import io.opencaesar.oml.EntityPredicate
import io.opencaesar.oml.EnumerationScalar
import io.opencaesar.oml.ExistentialRelationshipRestrictionAxiom
import io.opencaesar.oml.ExistentialScalarPropertyRestrictionAxiom
import io.opencaesar.oml.ForwardDirection
import io.opencaesar.oml.Graph
import io.opencaesar.oml.GraphMember
import io.opencaesar.oml.GraphMemberReference
import io.opencaesar.oml.IRIScalar
import io.opencaesar.oml.InverseDirection
import io.opencaesar.oml.LiteralBoolean
import io.opencaesar.oml.LiteralDateTime
import io.opencaesar.oml.LiteralDecimal
import io.opencaesar.oml.LiteralFloat
import io.opencaesar.oml.LiteralRational
import io.opencaesar.oml.LiteralReal
import io.opencaesar.oml.LiteralString
import io.opencaesar.oml.LiteralURI
import io.opencaesar.oml.LiteralUUID
import io.opencaesar.oml.NumericScalar
import io.opencaesar.oml.ParticularScalarPropertyRestrictionAxiom
import io.opencaesar.oml.PatternScalar
import io.opencaesar.oml.PlainLiteralScalar
import io.opencaesar.oml.ReifiedRelationship
import io.opencaesar.oml.ReifiedRelationshipInstance
import io.opencaesar.oml.ReifiedRelationshipInstanceTypeAssertion
import io.opencaesar.oml.ReifiedRelationshipPredicate
import io.opencaesar.oml.ReifiedRelationshipPredicateKind
import io.opencaesar.oml.Rule
import io.opencaesar.oml.Scalar
import io.opencaesar.oml.ScalarProperty
import io.opencaesar.oml.ScalarPropertyValueAssertion
import io.opencaesar.oml.StringScalar
import io.opencaesar.oml.Structure
import io.opencaesar.oml.StructuredProperty
import io.opencaesar.oml.StructuredPropertyRestrictionAxiom
import io.opencaesar.oml.StructuredPropertyValueAssertion
import io.opencaesar.oml.Term
import io.opencaesar.oml.TermSpecializationAxiom
import io.opencaesar.oml.Terminology
import io.opencaesar.oml.TerminologyExtension
import io.opencaesar.oml.TimeScalar
import io.opencaesar.oml.UniversalRelationshipRestrictionAxiom
import io.opencaesar.oml.UniversalScalarPropertyRestrictionAxiom
import io.opencaesar.oml.UnreifiedRelationship
import io.opencaesar.oml.util.OmlVisitor
import java.util.ArrayList
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.semanticweb.owlapi.model.OWLAnnotation
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.vocab.OWLFacet

import static extension io.opencaesar.oml.Oml.*

class Oml2Owl extends OmlVisitor {
		
	val Resource inputResource 
	val OwlApi owl	
	var OWLOntology ontology
	
	new(Resource inputResource, OwlApi owl2) {
		this.inputResource = inputResource
		this.owl = owl2
	}
	
	def OWLOntology run() {
		run(inputResource)
		return ontology
	}

	protected def dispatch void visit(Annotation annotation) {
		annotation.annotatedElement.addsAnnotation(annotation)
	}

	protected def dispatch void visit(Terminology terminology) {
		ontology = owl.createOntology(terminology.iri)
		owl.addImportsDeclaration(ontology, OmlConstants.OML)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.getLiteral(OmlConstants.Terminology))
	}

	protected def dispatch void visit(Description description) {
		ontology = owl.createOntology(description.iri)
		owl.addImportsDeclaration(ontology, OmlConstants.OML)
		owl.addOntologyAnnotation(ontology, OmlConstants.ontologyType, owl.getLiteral(OmlConstants.Description))
	}

	protected def dispatch void visit(Aspect aspect) {
		owl.addClass(ontology, aspect.iri)
		owl.addSubClassOfAxiom(ontology, aspect.iri, OmlConstants.Aspect)
	}

	protected def dispatch void visit(Concept concept) {
		owl.addClass(ontology, concept.iri)
		owl.addSubClassOfAxiom(ontology, concept.iri, OmlConstants.Concept)
	}

	protected def dispatch void visit(ReifiedRelationship relationship) {
		owl.addClass(ontology, relationship.iri)
		owl.addSubClassOfAxiom(ontology, relationship.iri, OmlConstants.ReifiedRelationship)
	}

	protected def dispatch void visit(Structure structure) {
		owl.addClass(ontology, structure.iri)
		owl.addSubClassOfAxiom(ontology, structure.iri, OmlConstants.Structure)
	}

	protected def dispatch void visit(Scalar scalar) {
		owl.addDatatype(ontology, scalar.iri)
	}

	protected def dispatch void visit(BinaryScalar scalar) {
		val restrictions = scalar.collectCharArrayFacetRestrictions()
		owl.addDatatypeRestriction(ontology, scalar.iri, scalar.specializedScalarRange.iri, restrictions)
	}

	protected def dispatch void visit(IRIScalar scalar) {
		val restrictions = scalar.collectPatternFacetRestrictions()
		owl.addDatatypeRestriction(ontology, scalar.iri, scalar.specializedScalarRange.iri, restrictions)
	}

	protected def dispatch void visit(PlainLiteralScalar scalar) {
		val restrictions = scalar.collectPatternFacetRestrictions()
		if (scalar.language !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.LANG_RANGE, owl.getLiteral(scalar.language))
		}
		owl.addDatatypeRestriction(ontology, scalar.iri, scalar.specializedScalarRange.iri, restrictions)
	}

	protected def dispatch void visit(StringScalar scalar) {
		val restrictions = scalar.collectPatternFacetRestrictions()
		owl.addDatatypeRestriction(ontology, scalar.iri, scalar.specializedScalarRange.iri, restrictions)
	}

	protected def dispatch void visit(NumericScalar scalar) {
		val restrictions = new ArrayList
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
		owl.addDatatypeRestriction(ontology, scalar.iri, scalar.specializedScalarRange.iri, restrictions)
	}

	protected def dispatch void visit(TimeScalar scalar) {
		val restrictions = new ArrayList
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
		owl.addDatatypeRestriction(ontology, scalar.iri, scalar.specializedScalarRange.iri, restrictions)
	}

	protected def dispatch void visit(EnumerationScalar scalar) {
		owl.addDataOneOf(ontology, scalar.iri, scalar.literals.map[literal])
	}

	protected def collectCharArrayFacetRestrictions(CharArrayScalar scalar) {
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
		return restrictions
	}

	protected def collectPatternFacetRestrictions(PatternScalar scalar) {
		val restrictions = collectCharArrayFacetRestrictions(scalar)
		if (scalar.pattern !== null) {
			restrictions += owl.getFacetRestriction(OWLFacet.PATTERN, owl.getLiteral(scalar.pattern))
		}
		return restrictions
	}

	protected def dispatch void visit(StructuredProperty property) {
		val propertyIri = property.iri
		owl.addObjectProperty(ontology, propertyIri)
		owl.addObjectPropertyDomainAxiom(ontology, propertyIri, property.domain.iri)
		owl.addObjectPropertyRangeAxiom(ontology, propertyIri, property.range.iri)
		if (property.functional) {
			owl.addFunctionalObjectProperty(ontology, propertyIri)
		}
	}

	protected def dispatch void visit(ScalarProperty property) {
		val propertyIri = property.iri
		owl.addDataProperty(ontology, propertyIri)
		owl.addDataPropertyDomainAxiom(ontology, propertyIri, property.domain.iri)
		owl.addDataPropertyRangeAxiom(ontology, propertyIri, property.range.iri)
		if (property.functional) {
			owl.addFunctionalDataProperty(ontology, propertyIri)
		}
	}

	protected def dispatch void visit(AnnotationProperty property) {
		owl.addAnnotationProperty(ontology, property.iri)
	}

	protected def dispatch void visit(ForwardDirection forward) {
		val relationship = forward.relationship
		val omlForwardIri = if (relationship instanceof ReifiedRelationship) {
			OmlConstants.reifiedRelationshipForward
		} else {
			OmlConstants.unreifiedRelationshipForward
		}
		// forward relationship
		val forwardIri = forward.iri
		owl.addObjectProperty(ontology, forwardIri)
		owl.addSubObjectPropertyOfAxiom(ontology, forwardIri, omlForwardIri)
		owl.addObjectPropertyDomainAxiom(ontology, forwardIri, forward.source.iri)
		owl.addObjectPropertyRangeAxiom(ontology, forwardIri, forward.target.iri)
		if (relationship.functional) {
			owl.addFunctionalObjectProperty(ontology, forwardIri)
		}
		if (relationship.inverseFunctional) {
			owl.addInverseFunctionalObjectProperty(ontology, forwardIri)
		}
		if (relationship.symmetric) {
			owl.addSymmetricObjectProperty(ontology, forwardIri)
		}
		if (relationship.asymmetric) {
			owl.addAsymmetricObjectProperty(ontology, forwardIri)
		}
		if (relationship.reflexive) {
			owl.addReflexiveObjectProperty(ontology, forwardIri)
		}
		if (relationship.irreflexive) {
			owl.addIrreflexiveObjectProperty(ontology, forwardIri)
		}
		if (relationship.transitive) {
			owl.addTransitiveObjectProperty(ontology, forwardIri)
		}
		if (relationship instanceof ReifiedRelationship) {
			// forward source relationship
			val forwardSourceIri = forward.sourceIri
			owl.addObjectProperty(ontology, forwardSourceIri)
			owl.addSubObjectPropertyOfAxiom(ontology, forwardSourceIri, omlForwardIri+'Source')
			owl.addObjectPropertyDomainAxiom(ontology, forwardSourceIri, relationship.iri)
			owl.addObjectPropertyRangeAxiom(ontology, forwardSourceIri, forward.source.iri)
			owl.addFunctionalObjectProperty(ontology, forwardSourceIri)
			if (relationship.functional) {
				owl.addInverseFunctionalObjectProperty(ontology, forwardSourceIri)
			}
			// forward target relationship
			val forwardTargetIri = forward.targetIri
			owl.addObjectProperty(ontology, forwardTargetIri)
			owl.addSubObjectPropertyOfAxiom(ontology, forwardTargetIri, omlForwardIri+'Target')
			owl.addObjectPropertyDomainAxiom(ontology, forwardTargetIri, relationship.iri)
			owl.addObjectPropertyRangeAxiom(ontology, forwardTargetIri, forward.target.iri)
			owl.addFunctionalObjectProperty(ontology, forwardTargetIri)
			if (relationship.inverseFunctional) {
				owl.addInverseFunctionalObjectProperty(ontology, forwardTargetIri)
			}
			// derivation rule for forward relationship
			val graphIri = forward.graph.iri
			val antedecents = new ArrayList
			antedecents += owl.getObjectPropertyAtom(forwardSourceIri, graphIri+'r', graphIri+'s')
			antedecents += owl.getObjectPropertyAtom(forwardTargetIri, graphIri+'r', graphIri+'t')
			val consequent = owl.getObjectPropertyAtom(forwardIri, graphIri+'s', graphIri+'t')
			val annotation = owl.getAnnotation(RDFS.LABEL.toString, owl.getLiteral(forward.name+' derivation'))
			owl.addNRule(ontology, consequent, antedecents, annotation)
		}
	}

	protected def dispatch void visit(InverseDirection inverse) {
		val relationship = inverse.relationship
		val omlInverseIri = if (relationship instanceof ReifiedRelationship) {
			OmlConstants.reifiedRelationshipInverse
		} else {
			OmlConstants.unreifiedRelationshipInverse
		}
		// inverse relationship
		val inverseIri = inverse.iri
		owl.addObjectProperty(ontology, inverseIri)
		owl.addSubObjectPropertyOfAxiom(ontology, inverseIri, omlInverseIri)
		owl.addInversePropertyAxiom(ontology, inverseIri, inverse.forwardRelationship.iri)
	}

	protected def dispatch void visit(ConceptInstance instance) {
		val instanceIri = instance.iri
		val individual = owl.addNamedIndividual(ontology, instanceIri)
		instance.propertyValues.forEach[assertion|assertion.appliesTo(individual)]
	}

	protected def dispatch void visit(ReifiedRelationshipInstance instance) {
		val instanceIri = instance.iri
		val individual = owl.addNamedIndividual(ontology, instanceIri)
		instance.propertyValues.forEach[assertion|assertion.appliesTo(individual)]
	}

	protected def dispatch void visit(TerminologyExtension _extension) {
		owl.addImportsDeclaration(ontology, _extension.importedGraph.iri)
	}

	protected def dispatch void visit(DescriptionUsage usage) {
		owl.addImportsDeclaration(ontology, usage.importedGraph.iri)
	}

	protected def dispatch void visit(DescriptionRefinement refinement) {
		owl.addImportsDeclaration(ontology, refinement.importedGraph.iri)
	}

	protected def dispatch void visit(ConceptInstanceTypeAssertion assertion) {
		val annotations = assertion.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addClassAssertionAxiom(ontology, assertion.conceptInstance.iri, assertion.concept.iri, annotations)
	}

	protected def dispatch void visit(ReifiedRelationshipInstanceTypeAssertion assertion) {
		val instance = assertion.reifiedRelationshipInstance
		val instanceIri = instance.iri
		val annotations = assertion.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addClassAssertionAxiom(ontology, instanceIri, assertion.relationship.iri, annotations)
		owl.addObjectPropertyAssertionAxiom(ontology, instanceIri, assertion.relationship.forward.sourceIri, instance.source.iri)
		owl.addObjectPropertyAssertionAxiom(ontology, instanceIri, assertion.relationship.forward.targetIri, instance.target.iri)
	}

	protected def dispatch void visit(TermSpecializationAxiom axiom) {
		val annotations = axiom.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		axiom.specializingTerm.specializes(axiom.specializedTerm, annotations)
	}

	protected def dispatch void visit(ExistentialRelationshipRestrictionAxiom axiom) {
		val annotations = axiom.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectSomeValuesFrom(ontology, axiom.restrictedEntity.iri, axiom.relationshipDirection.iri, axiom.restrictedTo.iri, annotations)
	}

	protected def dispatch void visit(UniversalRelationshipRestrictionAxiom axiom) {
		val annotations = axiom.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectAllValuesFrom(ontology, axiom.restrictedEntity.iri, axiom.relationshipDirection.iri, axiom.restrictedTo.iri, annotations)
	}

	protected def dispatch void visit(ExistentialScalarPropertyRestrictionAxiom axiom) {
		val annotations = axiom.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addDataSomeValuesFrom(ontology, axiom.restrictedEntity.iri, axiom.property.iri, axiom.restrictedTo.iri, annotations)
	}

	protected def dispatch void visit(UniversalScalarPropertyRestrictionAxiom axiom) {
		val annotations = axiom.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addDataAllValuesFrom(ontology, axiom.restrictedEntity.iri, axiom.property.iri, axiom.restrictedTo.iri, annotations)
	}

	protected def dispatch void visit(ParticularScalarPropertyRestrictionAxiom axiom) {
		val annotations = axiom.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addDataHasValue(ontology, axiom.restrictedEntity.iri, axiom.property.iri, axiom.value.literal, annotations)
	}

	protected def dispatch void visit(StructuredPropertyRestrictionAxiom axiom) {
		val individual = owl.getAnonymousIndividual(axiom.value.getId)
		axiom.value.propertyValues.forEach[nestedAssertion|nestedAssertion.appliesTo(individual)]
		val annotations = axiom.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectHasValue(ontology, axiom.restrictedEntity.iri, axiom.property.iri, individual, annotations)
	}

	protected def dispatch void visit(Rule rule) {
		var annotations = rule.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		if (annotations.filter[property.getIRI == RDFS.LABEL].isEmpty) {
			annotations = new ArrayList(annotations)
			annotations += owl.getAnnotation(RDFS.LABEL.toString, owl.getLiteral(rule.name))
		}
		owl.addNRule(ontology, rule.consequent.atom, rule.antecedent.map[atom], annotations)
	}

/* ------------------------- */

	protected dispatch def void addsAnnotation(AnnotatedElement element, Annotation annotation) {
		// all other cases are not mapped or mapped differently
	}

	protected dispatch def void addsAnnotation(Graph graph, Annotation annotation) {
		owl.addOntologyAnnotation(ontology, annotation.property.iri, annotation.value.literal)
	}

	protected dispatch def void addsAnnotation(GraphMember member, Annotation annotation) {
		owl.addAnnotationAssertionAxiom(ontology, member.iri, annotation.property.iri, annotation.value.literal)
	}
	
	protected dispatch def void addsAnnotation(GraphMemberReference element, Annotation annotation) {
		element.resolve.addsAnnotation(annotation)
	}

	protected dispatch def void specializes(Term specific, Term general, OWLAnnotation...annotations) {
		// all other cases are not mapped or mapped differently
	}

	protected dispatch def void specializes(Concept specific, Concept general, OWLAnnotation...annotations) {
		owl.addSubClassOfAxiom(ontology, specific.iri, general.iri, annotations)
	}
	
	protected dispatch def void specializes(Concept specific, Aspect general, OWLAnnotation...annotations) {
		owl.addSubClassOfAxiom(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(Aspect specific, Aspect general, OWLAnnotation...annotations) {
		owl.addSubClassOfAxiom(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(ReifiedRelationship specific, ReifiedRelationship general, OWLAnnotation...annotations) {
		owl.addSubClassOfAxiom(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(ReifiedRelationship specific, Aspect general, OWLAnnotation...annotations) {
		owl.addSubClassOfAxiom(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(UnreifiedRelationship specific, UnreifiedRelationship general, OWLAnnotation...annotations) {
		owl.addSubObjectPropertyOfAxiom(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(ScalarProperty specific, ScalarProperty general, OWLAnnotation...annotations) {
		owl.addSubDataPropertyOfAxiom(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void specializes(StructuredProperty specific, StructuredProperty general, OWLAnnotation...annotations) {
		owl.addSubObjectPropertyOfAxiom(ontology, specific.iri, general.iri, annotations)
	}

	protected dispatch def void appliesTo(ScalarPropertyValueAssertion assertion, OWLIndividual individual) {
		val annotations = assertion.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addDataPropertyAssertionAxiom(ontology, individual, assertion.property.iri, assertion.value.literal, annotations)
	}

	protected dispatch def void appliesTo(StructuredPropertyValueAssertion assertion, OWLIndividual individual) {
		val nestedIndividual = owl.getAnonymousIndividual(assertion.value.getId)
		assertion.value.propertyValues.forEach[nestedAssertion|nestedAssertion.appliesTo(nestedIndividual)]
		val annotations = assertion.annotations.map[owl.getAnnotation(property.iri, value.literal)]
		owl.addObjectPropertyAssertionAxiom(ontology, individual, assertion.property.iri, nestedIndividual, annotations)
	}

	protected dispatch def getAtom(EntityPredicate predicate) {
		owl.getClassAtom(predicate.entity.iri, predicate.variableIri)
	}

	protected dispatch def getAtom(DirectionalRelationshipPredicate predicate) {
		owl.getObjectPropertyAtom(predicate.relationshipDirection.iri, predicate.variable1Iri, predicate.variable2Iri)
	}

	protected dispatch def getAtom(ReifiedRelationshipPredicate predicate) {
		switch (predicate.kind) {
			case ReifiedRelationshipPredicateKind.SOURCE:
				owl.getObjectPropertyAtom(predicate.relationship.forward.sourceIri, predicate.variable1Iri, predicate.variable2Iri)
			case ReifiedRelationshipPredicateKind.INVERSE_SOURCE:
				owl.getObjectPropertyInverseAtom(predicate.relationship.forward.sourceIri, predicate.variable1Iri, predicate.variable2Iri)
			case ReifiedRelationshipPredicateKind.TARGET:
				owl.getObjectPropertyAtom(predicate.relationship.forward.targetIri, predicate.variable1Iri, predicate.variable2Iri)
			case ReifiedRelationshipPredicateKind.INVERSE_TARGET:
				owl.getObjectPropertyInverseAtom(predicate.relationship.forward.targetIri, predicate.variable1Iri, predicate.variable2Iri)
		}
	}

	protected dispatch def getLiteral(LiteralBoolean literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(LiteralDateTime literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(LiteralString literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(LiteralUUID literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(LiteralURI literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
			}
	}

	protected dispatch def getLiteral(LiteralReal literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(LiteralRational literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(LiteralFloat literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

	protected dispatch def getLiteral(LiteralDecimal literal) {
		if (literal.valueType !== null) {
			owl.getLiteral(literal.value.toString, literal.valueType.iri)
		} else {		
			owl.getLiteral(literal.value)
		}
	}

}