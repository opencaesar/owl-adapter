package io.opencaesar.oml2owl

import java.math.BigDecimal
import java.util.Collections
import org.semanticweb.owlapi.model.AddImport
import org.semanticweb.owlapi.model.AddOntologyAnnotation
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAnnotation
import org.semanticweb.owlapi.model.OWLAnnotationValue
import org.semanticweb.owlapi.model.OWLDataFactory
import org.semanticweb.owlapi.model.OWLFacetRestriction
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLLiteral
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyManager
import org.semanticweb.owlapi.model.SWRLAtom
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.vocab.OWLFacet
import org.semanticweb.owlapi.model.OWLClass
import java.util.Set

class OwlApi {
	
	val XSD = "http://www.w3.org/2001/XMLSchema#"
	val OWLOntologyManager manager
	val OWLDataFactory factory
	
	new(OWLOntologyManager manager) {
		this.manager = manager
		this.factory = manager.getOWLDataFactory()
	}
	
	def createOntology(String iri) {
		return manager.createOntology(IRI.create(iri))
	}

	def addImportsDeclaration(OWLOntology ontology, String iri) {
		val import = factory.getOWLImportsDeclaration(IRI.create(iri))
		manager.applyChanges(new AddImport(ontology, import))
		return import
	}

	def addClass(OWLOntology ontology, String iri) {
		val class = factory.getOWLClass(iri)
		val axiom = factory.getOWLDeclarationAxiom(class)
		manager.addAxiom(ontology, axiom)
		return class
	}

	def addDatatype(OWLOntology ontology, String iri) {
		val datatype = factory.getOWLDatatype(iri)
		val axiom = factory.getOWLDeclarationAxiom(datatype)
		manager.addAxiom(ontology, axiom)
		return datatype
	}

	def addDatatypeRestriction(OWLOntology ontology, String subIri, String superIri, OWLFacetRestriction...restrictions) {
		val datatype = factory.getOWLDatatype(subIri)
		val restrictedDatatype = factory.getOWLDatatype(superIri)
		val restriction = factory.getOWLDatatypeRestriction(restrictedDatatype, restrictions)
		val axiom = factory.getOWLDatatypeDefinitionAxiom(datatype, restriction)
		manager.addAxiom(ontology, axiom)
		return restriction
	}

	def addDataOneOf(OWLOntology ontology, String subIri, OWLLiteral...literals) {
		val datatype = factory.getOWLDatatype(subIri)
		val dataOneOf = factory.getOWLDataOneOf(literals)
		val axiom = factory.getOWLDatatypeDefinitionAxiom(datatype, dataOneOf)
		manager.addAxiom(ontology, axiom)
		return dataOneOf
	}

	def addObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLDeclarationAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addFunctionalObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLFunctionalObjectPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addInverseFunctionalObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLInverseFunctionalObjectPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addSymmetricObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLSymmetricObjectPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addAsymmetricObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLAsymmetricObjectPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addReflexiveObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLReflexiveObjectPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addIrreflexiveObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLIrreflexiveObjectPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addTransitiveObjectProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLObjectProperty(iri)
		val axiom = factory.getOWLTransitiveObjectPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addDataProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLDataProperty(iri)
		val axiom = factory.getOWLDeclarationAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addFunctionalDataProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLDataProperty(iri)
		val axiom = factory.getOWLFunctionalDataPropertyAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addAnnotationProperty(OWLOntology ontology, String iri) {
		val property = factory.getOWLAnnotationProperty(iri)
		val axiom = factory.getOWLDeclarationAxiom(property)
		manager.addAxiom(ontology, axiom)
		return property
	}

	def addNamedIndividual(OWLOntology ontology, String iri) {
		val individual = factory.getOWLNamedIndividual(iri)
		val axiom = factory.getOWLDeclarationAxiom(individual)
		manager.addAxiom(ontology, axiom)
		return individual
	}

	def getAnonymousIndividual(String nodeId) {
		return factory.getOWLAnonymousIndividual(nodeId)
	}

	def addNRule(OWLOntology ontology, SWRLAtom head, SWRLAtom[] body, OWLAnnotation...annotations) {
		val axiom = factory.getSWRLRule(body, Collections.singleton(head), annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addSWRLSameAs(OWLOntology ontology, String variable1, String variable2) {
		val arg1 = factory.getSWRLVariable(variable1)
		val arg2 = factory.getSWRLVariable(variable2)
		return factory.getSWRLSameIndividualAtom(arg1, arg2)
	}
	
	def addSWRLDifferentFrom(OWLOntology ontology, String variable1, String variable2) {
		val arg1 = factory.getSWRLVariable(variable1)
		val arg2 = factory.getSWRLVariable(variable2)
		return factory.getSWRLDifferentIndividualsAtom(arg1, arg2)
	}
	
	def getClassAtom(String classIri, String variableIri) {
		val class = factory.getOWLClass(classIri)
		val variable = factory.getSWRLVariable(variableIri)
		return factory.getSWRLClassAtom(class, variable)
	}

	def getObjectPropertyAtom(String propertyIri, String variable1Iri, String variable2Iri) {
		val property = factory.getOWLObjectProperty(propertyIri)
		val variable1 = factory.getSWRLVariable(variable1Iri)
		val variable2 = factory.getSWRLVariable(variable2Iri)
		return factory.getSWRLObjectPropertyAtom(property, variable1, variable2)
	}

	def getObjectPropertyInverseAtom(String propertyIri, String variable1Iri, String variable2Iri) {
		val property = factory.getOWLObjectInverseOf(factory.getOWLObjectProperty(propertyIri))
		val variable1 = factory.getSWRLVariable(variable1Iri)
		val variable2 = factory.getSWRLVariable(variable2Iri)
		return factory.getSWRLObjectPropertyAtom(property, variable1, variable2)
	}

	def addSubClassOf(OWLOntology ontology, String subIri, String superIri, OWLAnnotation...annotations) {
		val subClass = factory.getOWLClass(subIri)
		val supClass = factory.getOWLClass(superIri)
		val axiom = factory.getOWLSubClassOfAxiom(subClass, supClass, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDisjointClasses(OWLOntology ontology, Iterable<OWLClassExpression> classes, OWLAnnotation...annotations) {
		val axiom = factory.getOWLDisjointClassesAxiom(classes.toSet, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}
	
	def addDisjointUnion(OWLOntology ontology, OWLClass owlClass, Set<OWLClassExpression> subClasses, OWLAnnotation...annotations) {
		val axiom = factory.getOWLDisjointUnionAxiom(owlClass, subClasses, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}
	
	def addHasKey(OWLOntology ontology, String classIri, Iterable<String> keyPropertyIris, OWLAnnotation...annotations) {
		val aClass = factory.getOWLClass(classIri)
		val keyProperties = keyPropertyIris.map[iri|factory.getOWLDataProperty(iri)].toList
		val axiom = factory.getOWLHasKeyAxiom(aClass, keyProperties, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectSomeValuesFrom(OWLOntology ontology, String classIri, String propertyIri, String typeIri, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLObjectProperty(propertyIri)
		val type = factory.getOWLClass(typeIri)
		val restriction = factory.getOWLObjectSomeValuesFrom(property, type)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectAllValuesFrom(OWLOntology ontology, String classIri, String propertyIri, String typeIri, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLObjectProperty(propertyIri)
		val type = factory.getOWLClass(typeIri)
		val restriction = factory.getOWLObjectAllValuesFrom(property, type)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectHasValue(OWLOntology ontology, String classIri, String propertyIri, OWLIndividual individual, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLObjectProperty(propertyIri)
		val restriction = factory.getOWLObjectHasValue(property, individual)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectExactCardinality(OWLOntology ontology, String classIri, String propertyIri, int cardinality, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLObjectProperty(propertyIri)
		val restriction = factory.getOWLObjectExactCardinality(cardinality, property)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectMinCardinality(OWLOntology ontology, String classIri, String propertyIri, int cardinality, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLObjectProperty(propertyIri)
		val restriction = factory.getOWLObjectMinCardinality(cardinality, property)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectMaxCardinality(OWLOntology ontology, String classIri, String propertyIri, int cardinality, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLObjectProperty(propertyIri)
		val restriction = factory.getOWLObjectMaxCardinality(cardinality, property)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataSomeValuesFrom(OWLOntology ontology, String classIri, String propertyIri, String typeIri, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLDataProperty(propertyIri)
		val type = factory.getOWLDatatype(typeIri)
		val restriction = factory.getOWLDataSomeValuesFrom(property, type)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataAllValuesFrom(OWLOntology ontology, String classIri, String propertyIri, String typeIri, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLDataProperty(propertyIri)
		val type = factory.getOWLDatatype(typeIri)
		val restriction = factory.getOWLDataAllValuesFrom(property, type)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataHasValue(OWLOntology ontology, String classIri, String propertyIri, OWLLiteral literal, OWLAnnotation...annotations) {
		val class = factory.getOWLClass(classIri)
		val property = factory.getOWLDataProperty(propertyIri)
		val restriction = factory.getOWLDataHasValue(property, literal)
		val axiom = factory.getOWLSubClassOfAxiom(class, restriction, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addSubObjectPropertyOf(OWLOntology ontology, String subPropertyIri, String superPropertyIri, OWLAnnotation...annotations) {
		val subProperty = factory.getOWLObjectProperty(subPropertyIri)
		val supProperty = factory.getOWLObjectProperty(superPropertyIri)
		val axiom = factory.getOWLSubObjectPropertyOfAxiom(subProperty, supProperty, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectPropertyDomain(OWLOntology ontology, String propertyIri, String domainIri) {
		val property = factory.getOWLObjectProperty(propertyIri)
		val domain = factory.getOWLClass(domainIri)
		val axiom = factory.getOWLObjectPropertyDomainAxiom(property, domain)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectPropertyRange(OWLOntology ontology, String propertyIri, String rangeIri) {
		val property = factory.getOWLObjectProperty(propertyIri)
		val range = factory.getOWLClass(rangeIri)
		val axiom = factory.getOWLObjectPropertyRangeAxiom(property, range)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addSubDataPropertyOf(OWLOntology ontology, String subPropertyIri, String superPropertyIri, OWLAnnotation...annotations) {
		val subProperty = factory.getOWLDataProperty(subPropertyIri)
		val supProperty = factory.getOWLDataProperty(superPropertyIri)
		val axiom = factory.getOWLSubDataPropertyOfAxiom(subProperty, supProperty, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataPropertyDomain(OWLOntology ontology, String propertyIri, String domainIri) {
		val property = factory.getOWLDataProperty(propertyIri)
		val domain = factory.getOWLClass(domainIri)
		val axiom = factory.getOWLDataPropertyDomainAxiom(property, domain)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataPropertyRange(OWLOntology ontology, String propertyIri, String rangeIri) {
		val property = factory.getOWLDataProperty(propertyIri)
		val range = factory.getOWLDatatype(rangeIri)
		val axiom = factory.getOWLDataPropertyRangeAxiom(property, range)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addInverseProperty(OWLOntology ontology, String forwardPropertyIri, String inversePropertyIri) {
		val forwardproperty = factory.getOWLObjectProperty(forwardPropertyIri)
		val inverseProperty = factory.getOWLObjectProperty(inversePropertyIri)
		val axiom = factory.getOWLInverseObjectPropertiesAxiom(forwardproperty, inverseProperty)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addClassAssertion(OWLOntology ontology, String individualIri, String classIri, OWLAnnotation...annotations) {
		val individual = factory.getOWLNamedIndividual(individualIri)
		val class = factory.getOWLClass(classIri)
		val axiom = factory.getOWLClassAssertionAxiom(class, individual, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectPropertyAssertion(OWLOntology ontology, String individualIri, String propertyIri, String objectIri, OWLAnnotation...annotations) {
		val object = factory.getOWLNamedIndividual(objectIri)
		val individual = factory.getOWLNamedIndividual(individualIri)
		return addObjectPropertyAssertion(ontology, individual, propertyIri, object, annotations)
	}

	def addObjectPropertyAssertion(OWLOntology ontology, OWLIndividual individual, String propertyIri, OWLIndividual object, OWLAnnotation...annotations) {
		val property = factory.getOWLObjectProperty(propertyIri)
		val axiom = factory.getOWLObjectPropertyAssertionAxiom(property, individual, object, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataPropertyAssertion(OWLOntology ontology, String individualIri, String propertyIri, OWLLiteral literal, OWLAnnotation...annotations) {
		val individual = factory.getOWLNamedIndividual(individualIri)
		return addDataPropertyAssertion(ontology, individual, propertyIri, literal, annotations)
	}

	def addDataPropertyAssertion(OWLOntology ontology, OWLIndividual individual, String propertyIri, OWLLiteral literal, OWLAnnotation...annotations) {
		val property = factory.getOWLDataProperty(propertyIri)
		val axiom = factory.getOWLDataPropertyAssertionAxiom(property, individual, literal, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def getAnnotation(String propertyIri, OWLAnnotationValue value) {
		val property = factory.getOWLAnnotationProperty(propertyIri)
		return factory.getOWLAnnotation(property, value)
	}

	def addOntologyAnnotation(OWLOntology ontology, String propertyIri, OWLAnnotationValue value) {
		val property = factory.getOWLAnnotationProperty(propertyIri)
		val annotation = factory.getOWLAnnotation(property, value)
		manager.applyChange(new AddOntologyAnnotation(ontology, annotation))
		return annotation
	}

	def addAnnotationAssertion(OWLOntology ontology, String subjectIri, String propertyIri, OWLAnnotationValue value) {
		val property = factory.getOWLAnnotationProperty(propertyIri)
		val axiom = factory.getOWLAnnotationAssertionAxiom(property, IRI.create(subjectIri), value)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def getFacetRestriction(OWLFacet facet, OWLLiteral value) {
		return factory.getOWLFacetRestriction(facet, value)
	}

	def getLiteral(boolean value) {
		return factory.getOWLLiteral(value)
	}

	def getLiteral(int value) {
		return factory.getOWLLiteral(value)
	}

	def getLiteral(double value) {
		return factory.getOWLLiteral(value)
	}

	def getLiteral(String value) {
		return factory.getOWLLiteral(value)
	}

	def getLiteral(BigDecimal value) {
		return getLiteralWithDatatype(value.toString, XSD+"decimal")
	}

	def getLiteralWithDatatype(String value, String datatypeIri) {
		val datatype = factory.getOWLDatatype(datatypeIri)
		return factory.getOWLLiteral(value, datatype)
	}

	def getLiteralWithLangTag(String value, String langTag) {
		return factory.getOWLLiteral(value, langTag)
	}

	def getOWLThing() {
		return factory.getOWLThing
	}

	def getOWLNothing() {
		return factory.getOWLNothing
	}
	
	def getOWLClass(IRI iri) {
		return factory.getOWLClass(iri)
	}
	
	def getOWLObjectComplementOf(OWLClassExpression e) {
		return factory.getOWLObjectComplementOf(e)
	}
	
	def getOWLObjectIntersectionOf(Iterable<? extends OWLClassExpression> operands) {
		return factory.getOWLObjectIntersectionOf(operands)
	}
	
	def getOWLObjectUnionOf(Iterable<? extends OWLClassExpression> operands) {
		return factory.getOWLObjectUnionOf(operands)
	}
}