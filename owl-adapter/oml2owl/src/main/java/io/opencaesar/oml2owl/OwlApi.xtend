package io.opencaesar.oml2owl

import io.opencaesar.oml2owl.utils.ClassExpression
import java.math.BigDecimal
import java.util.Collections
import java.util.Set
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
import org.semanticweb.owlapi.vocab.OWLFacet

import static extension io.opencaesar.oml2owl.utils.OwlClassExpression.*
import org.semanticweb.owlapi.model.OWLClassExpression

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

	def addSubClassOfAxiom(OWLOntology ontology, String subIri, String superIri, OWLAnnotation...annotations) {
		val subClass = factory.getOWLClass(subIri)
		val supClass = factory.getOWLClass(superIri)
		val axiom = factory.getOWLSubClassOfAxiom(subClass, supClass, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDisjointClassesAxiom(OWLOntology ontology, Set<ClassExpression> classes, OWLAnnotation...annotations) {
		val axiom = factory.getOWLDisjointClassesAxiom(classes.map[toOwlClassExpression(this)])
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

	def addSubObjectPropertyOfAxiom(OWLOntology ontology, String subPropertyIri, String superPropertyIri, OWLAnnotation...annotations) {
		val subProperty = factory.getOWLObjectProperty(subPropertyIri)
		val supProperty = factory.getOWLObjectProperty(superPropertyIri)
		val axiom = factory.getOWLSubObjectPropertyOfAxiom(subProperty, supProperty, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectPropertyDomainAxiom(OWLOntology ontology, String propertyIri, String domainIri) {
		val property = factory.getOWLObjectProperty(propertyIri)
		val domain = factory.getOWLClass(domainIri)
		val axiom = factory.getOWLObjectPropertyDomainAxiom(property, domain)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectPropertyRangeAxiom(OWLOntology ontology, String propertyIri, String rangeIri) {
		val property = factory.getOWLObjectProperty(propertyIri)
		val range = factory.getOWLClass(rangeIri)
		val axiom = factory.getOWLObjectPropertyRangeAxiom(property, range)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addSubDataPropertyOfAxiom(OWLOntology ontology, String subPropertyIri, String superPropertyIri, OWLAnnotation...annotations) {
		val subProperty = factory.getOWLDataProperty(subPropertyIri)
		val supProperty = factory.getOWLDataProperty(superPropertyIri)
		val axiom = factory.getOWLSubDataPropertyOfAxiom(subProperty, supProperty, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataPropertyDomainAxiom(OWLOntology ontology, String propertyIri, String domainIri) {
		val property = factory.getOWLDataProperty(propertyIri)
		val domain = factory.getOWLClass(domainIri)
		val axiom = factory.getOWLDataPropertyDomainAxiom(property, domain)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataPropertyRangeAxiom(OWLOntology ontology, String propertyIri, String rangeIri) {
		val property = factory.getOWLDataProperty(propertyIri)
		val range = factory.getOWLDatatype(rangeIri)
		val axiom = factory.getOWLDataPropertyRangeAxiom(property, range)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addInversePropertyAxiom(OWLOntology ontology, String forwardPropertyIri, String inversePropertyIri) {
		val forwardproperty = factory.getOWLObjectProperty(forwardPropertyIri)
		val inverseProperty = factory.getOWLObjectProperty(inversePropertyIri)
		val axiom = factory.getOWLInverseObjectPropertiesAxiom(forwardproperty, inverseProperty)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addClassAssertionAxiom(OWLOntology ontology, String individualIri, String classIri, OWLAnnotation...annotations) {
		val individual = factory.getOWLNamedIndividual(individualIri)
		val class = factory.getOWLClass(classIri)
		val axiom = factory.getOWLClassAssertionAxiom(class, individual, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addObjectPropertyAssertionAxiom(OWLOntology ontology, String individualIri, String propertyIri, String objectIri, OWLAnnotation...annotations) {
		val object = factory.getOWLNamedIndividual(objectIri)
		val individual = factory.getOWLNamedIndividual(individualIri)
		return addObjectPropertyAssertionAxiom(ontology, individual, propertyIri, object, annotations)
	}

	def addObjectPropertyAssertionAxiom(OWLOntology ontology, OWLIndividual individual, String propertyIri, OWLIndividual object, OWLAnnotation...annotations) {
		val property = factory.getOWLObjectProperty(propertyIri)
		val axiom = factory.getOWLObjectPropertyAssertionAxiom(property, individual, object, annotations)
		manager.addAxiom(ontology, axiom)
		return axiom
	}

	def addDataPropertyAssertionAxiom(OWLOntology ontology, String individualIri, String propertyIri, OWLLiteral literal, OWLAnnotation...annotations) {
		val individual = factory.getOWLNamedIndividual(individualIri)
		return addDataPropertyAssertionAxiom(ontology, individual, propertyIri, literal, annotations)
	}

	def addDataPropertyAssertionAxiom(OWLOntology ontology, OWLIndividual individual, String propertyIri, OWLLiteral literal, OWLAnnotation...annotations) {
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

	def addAnnotationAssertionAxiom(OWLOntology ontology, String subjectIri, String propertyIri, OWLAnnotationValue value) {
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
		return getLiteral(value.toString, XSD+"decimal")
	}

	def getLiteral(String value, String datatypeIri) {
		val datatype = factory.getOWLDatatype(datatypeIri)
		return factory.getOWLLiteral(value, datatype)
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
	
	def getOWLObjectIntersectionOf(Set<? extends OWLClassExpression> operands) {
		return factory.getOWLObjectIntersectionOf(operands)
	}
	
	def getOWLObjectUnionOf(Set<? extends OWLClassExpression> operands) {
		return factory.getOWLObjectUnionOf(operands)
	}
}