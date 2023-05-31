/**
 * 
 * Copyright 2019-2021 California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package io.opencaesar.oml2owl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.vocab.OWLFacet;

class OwlApi extends io.opencaesar.closeworld.OwlApi {

	protected final boolean annotationsOnAxioms;

	public OwlApi(final OWLOntologyManager manager, final boolean annotationsOnAxioms) {
		super(manager);
		this.annotationsOnAxioms = annotationsOnAxioms;
	}

	public IRI createIri(final String iri) {
		return IRI.create(iri);
	}

	public OWLOntology createOntology(String prefix, String namespace) {
		try {
			var iri = namespace.substring(0, namespace.length()-1);
			OWLOntology ontology = manager.createOntology(IRI.create(iri));
			ontology.getFormat().asPrefixOWLDocumentFormat().setDefaultPrefix(namespace);
			ontology.getFormat().asPrefixOWLDocumentFormat().setPrefix(prefix, namespace);
			return ontology;
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public OWLImportsDeclaration addImportsDeclaration(final OWLOntology ontology, final String iri) {
		final OWLImportsDeclaration import_ = factory.getOWLImportsDeclaration(IRI.create(iri));
		manager.applyChanges(new AddImport(ontology, import_));
		return import_;
	}

	public OWLClass addClass(final OWLOntology ontology, final String iri) {
		final OWLClass class_ = factory.getOWLClass(iri);
		final OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(class_);
		manager.addAxiom(ontology, axiom);
		return class_;
	}

	public OWLDatatype addDatatype(final OWLOntology ontology, final String iri) {
		final OWLDatatype datatype = factory.getOWLDatatype(iri);
		final OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(datatype);
		manager.addAxiom(ontology, axiom);
		return datatype;
	}

	public OWLDatatypeDefinitionAxiom addDatatypeDefinition(final OWLOntology ontology, final String datatypeIri, final String restrictedIri, final OWLFacetRestriction[] restrictions, final OWLAnnotation... annotations) {
		final OWLDatatype datatype = factory.getOWLDatatype(datatypeIri);
		final OWLDatatype restrictedDatatype = factory.getOWLDatatype(restrictedIri);
		final OWLDatatypeRestriction restriction = factory.getOWLDatatypeRestriction(restrictedDatatype, restrictions);
		final OWLDatatypeDefinitionAxiom axiom = factory.getOWLDatatypeDefinitionAxiom(datatype, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLDatatypeDefinitionAxiom addDatatypeDefinition(final OWLOntology ontology, final String datatypeIri, final String synonymIri, final OWLAnnotation... annotations) {
		final OWLDatatype datatype = factory.getOWLDatatype(datatypeIri);
		final OWLDatatype synonymDatatype = factory.getOWLDatatype(synonymIri);
		final OWLDatatypeDefinitionAxiom axiom = factory.getOWLDatatypeDefinitionAxiom(datatype, synonymDatatype, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLDataOneOf addDataOneOf(final OWLOntology ontology, final String datatypeIri, final OWLLiteral... literals) {
		final OWLDatatype datatype = factory.getOWLDatatype(datatypeIri);
		final OWLDataOneOf dataOneOf = factory.getOWLDataOneOf(literals);
		final OWLDatatypeDefinitionAxiom axiom = factory.getOWLDatatypeDefinitionAxiom(datatype, dataOneOf);
		manager.addAxiom(ontology, axiom);
		return dataOneOf;
	}

	public OWLObjectOneOf addObjectOneOf(final OWLOntology ontology, final String classIri, final List<OWLNamedIndividual> individuals) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectOneOf objectOneOf = factory.getOWLObjectOneOf(individuals);
		final OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(class_, objectOneOf);
		manager.addAxiom(ontology, axiom);
		return objectOneOf;
	}

	public OWLObjectProperty addObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLObjectProperty addFunctionalObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLFunctionalObjectPropertyAxiom axiom = factory.getOWLFunctionalObjectPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLObjectProperty addInverseFunctionalObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLInverseFunctionalObjectPropertyAxiom axiom = factory.getOWLInverseFunctionalObjectPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLObjectProperty addSymmetricObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLSymmetricObjectPropertyAxiom axiom = factory.getOWLSymmetricObjectPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLObjectProperty addAsymmetricObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLAsymmetricObjectPropertyAxiom axiom = factory.getOWLAsymmetricObjectPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLObjectProperty addReflexiveObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLReflexiveObjectPropertyAxiom axiom = factory.getOWLReflexiveObjectPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLObjectProperty addIrreflexiveObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLIrreflexiveObjectPropertyAxiom axiom = factory.getOWLIrreflexiveObjectPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLObjectProperty addTransitiveObjectProperty(final OWLOntology ontology, final String iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(iri);
		final OWLTransitiveObjectPropertyAxiom axiom = factory.getOWLTransitiveObjectPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLDataProperty addDataProperty(final OWLOntology ontology, final String iri) {
		final OWLDataProperty property = factory.getOWLDataProperty(iri);
		final OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLDataProperty addFunctionalDataProperty(final OWLOntology ontology, final String iri) {
		final OWLDataProperty property = factory.getOWLDataProperty(iri);
		final OWLFunctionalDataPropertyAxiom axiom = factory.getOWLFunctionalDataPropertyAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLAnnotationProperty addAnnotationProperty(final OWLOntology ontology, final String iri) {
		final OWLAnnotationProperty property = factory.getOWLAnnotationProperty(iri);
		final OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(property);
		manager.addAxiom(ontology, axiom);
		return property;
	}

	public OWLNamedIndividual addNamedIndividual(final OWLOntology ontology, final String iri) {
		final OWLNamedIndividual individual = factory.getOWLNamedIndividual(iri);
		final OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(individual);
		manager.addAxiom(ontology, axiom);
		return individual;
	}

	public OWLNamedIndividual getNamedIndividual(final String nodeId) {
		return factory.getOWLNamedIndividual(nodeId);
	}

	public OWLAnonymousIndividual getAnonymousIndividual() {
		return factory.getOWLAnonymousIndividual();
	}

	public SWRLRule addRule(final OWLOntology ontology, final List<SWRLAtom> head, final List<SWRLAtom> body, final OWLAnnotation... annotations) {
		final SWRLRule axiom = factory.getSWRLRule(body, head, Arrays.asList(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public SWRLVariable getSWRLVariable(String variable) {
		return factory.getSWRLVariable("urn:swrl:var#" + variable);
	}
	
	public SWRLIndividualArgument getSWRLIndividualArgument(final OWLIndividual individual) {
		return factory.getSWRLIndividualArgument(individual);
	}

	public SWRLLiteralArgument getSWRLLiteralArgument(final OWLLiteral literal) {
		return factory.getSWRLLiteralArgument(literal);
	}

	public SWRLClassAtom getClassAtom(final String classIri, final SWRLIArgument argument) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		return factory.getSWRLClassAtom(class_, argument);
	}

	public SWRLDataRangeAtom getDataRangeAtom(final String datatypeIri, SWRLDArgument argument) {
		final OWLDatatype datatype = factory.getOWLDatatype(datatypeIri);
		return factory.getSWRLDataRangeAtom(datatype, argument);
	}

	public SWRLObjectPropertyAtom getObjectPropertyAtom(final String propertyIri, final SWRLIArgument argument1, final SWRLIArgument argument2) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		return factory.getSWRLObjectPropertyAtom(property, argument1, argument2);
	}
	
	public SWRLDataPropertyAtom getDataPropertyAtom(final String propertyIri, final SWRLIArgument argument1, final SWRLDArgument argument2) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		return factory.getSWRLDataPropertyAtom(property, argument1, argument2);
	}

	public SWRLSameIndividualAtom getSameIndividualAtom(final SWRLIArgument argument1, final SWRLIArgument argument2) {
		return factory.getSWRLSameIndividualAtom(argument1, argument2);
	}

	public SWRLDifferentIndividualsAtom getDifferentIndividualsAtom(final SWRLIArgument argument1, final SWRLIArgument argument2) {
		return factory.getSWRLDifferentIndividualsAtom(argument1, argument2);
	}

	public SWRLBuiltInAtom getBuiltInAtom(final String builtInIri, final List<SWRLDArgument> arguments) {
		return factory.getSWRLBuiltInAtom(createIri(builtInIri), arguments);
	}

	public OWLSubClassOfAxiom addSubClassOf(final OWLOntology ontology, final String subIri, final String superIri, final OWLAnnotation... annotations) {
		final OWLClass subClass = factory.getOWLClass(subIri);
		final OWLClass supClass = factory.getOWLClass(superIri);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(subClass, supClass, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addSubClassOf(final OWLOntology ontology, final String subIri, final OWLClassExpression superExpression, final OWLAnnotation... annotations) {
		final OWLClass subClass = factory.getOWLClass(subIri);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(subClass, superExpression, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLEquivalentClassesAxiom addEquivalentClasses(final OWLOntology ontology, final String classIri, OWLClassExpression equivalentClass, final OWLAnnotation... annotations) {
		final List<OWLClassExpression> classes = new ArrayList<>();
		classes.add(factory.getOWLClass(classIri));
		classes.add(equivalentClass);
		final OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(classes, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLEquivalentClassesAxiom addEquivalentClasses(final OWLOntology ontology, final String classIri, String equivalentClassIri, final OWLAnnotation... annotations) {
		final List<OWLClassExpression> classes = new ArrayList<>();
		classes.add(factory.getOWLClass(classIri));
		classes.add(factory.getOWLClass(equivalentClassIri));
		final OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(classes, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLEquivalentDataPropertiesAxiom addEquivalentDataProperties(final OWLOntology ontology, final String propertyIri, String equivalentPropertyIri, final OWLAnnotation... annotations) {
		final List<OWLDataPropertyExpression> properties = new ArrayList<>();
		properties.add(factory.getOWLDataProperty(propertyIri));
		properties.add(factory.getOWLDataProperty(equivalentPropertyIri));
		final OWLEquivalentDataPropertiesAxiom axiom = factory.getOWLEquivalentDataPropertiesAxiom(properties, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLEquivalentObjectPropertiesAxiom addEquivalentObjectProperties(final OWLOntology ontology, final String propertyIri, String equivalentPropertyIri, final OWLAnnotation... annotations) {
		final List<OWLObjectPropertyExpression> properties = new ArrayList<>();
		properties.add(factory.getOWLObjectProperty(propertyIri));
		properties.add(factory.getOWLObjectProperty(equivalentPropertyIri));
		final OWLEquivalentObjectPropertiesAxiom axiom = factory.getOWLEquivalentObjectPropertiesAxiom(properties, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLObjectIntersectionOf getObjectIntersectionOf(List<String> classIris, List<OWLClassExpression> expressions) {
		final List<OWLClassExpression> classes = new ArrayList<>();
		classIris.stream().forEach(i -> classes.add(factory.getOWLClass(i)));
		classes.addAll(expressions);
		return factory.getOWLObjectIntersectionOf(classes);
	}

	public OWLHasKeyAxiom addHasKey(final OWLOntology ontology, final String classIri, final List<OWLProperty> keyProperties, final OWLAnnotation... annotations) {
		final OWLClass aClass = factory.getOWLClass(classIri);
		final OWLHasKeyAxiom axiom = factory.getOWLHasKeyAxiom(aClass, keyProperties, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLObjectSomeValuesFrom getObjectSomeValuesFrom(final String propertyIri, final String typeIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLClass type = factory.getOWLClass(typeIri);
		return factory.getOWLObjectSomeValuesFrom(property, type);
	}

	public OWLObjectAllValuesFrom getObjectAllValuesFrom(final String propertyIri, final String typeIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLClass type = factory.getOWLClass(typeIri);
		return factory.getOWLObjectAllValuesFrom(property, type);
	}

	public OWLObjectHasValue getObjectHasValue(final String propertyIri, final OWLIndividual individual) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		return factory.getOWLObjectHasValue(property, individual);
	}

	public OWLObjectHasSelf getObjectHasSelf(final String propertyIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		return factory.getOWLObjectHasSelf(property);
	}

	public OWLObjectHasValue getObjectHasValue(final String propertyIri, final String individualIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLNamedIndividual individual = factory.getOWLNamedIndividual(individualIri);
		return factory.getOWLObjectHasValue(property, individual);
	}

	public OWLObjectExactCardinality getObjectExactCardinality(final String propertyIri, final int cardinality, final String rangeIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		if (rangeIri != null) {
			return factory.getOWLObjectExactCardinality(cardinality, property, factory.getOWLClass(rangeIri));
		} else {
			return factory.getOWLObjectExactCardinality(cardinality, property);
		}
	}

	public OWLObjectMinCardinality getObjectMinCardinality(final String propertyIri, final int cardinality, final String rangeIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		if (rangeIri != null) {
			return factory.getOWLObjectMinCardinality(cardinality, property, factory.getOWLClass(rangeIri));
		} else {
			return factory.getOWLObjectMinCardinality(cardinality, property);
		}
	}

	public OWLObjectMaxCardinality getObjectMaxCardinality(final String propertyIri, final int cardinality, final String rangeIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		if (rangeIri != null) {
			return factory.getOWLObjectMaxCardinality(cardinality, property, factory.getOWLClass(rangeIri));
		} else {
			return factory.getOWLObjectMaxCardinality(cardinality, property);
		}
	}

	public OWLDataExactCardinality getDataExactCardinality(final String propertyIri, final int cardinality, final String rangeIri) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		if (rangeIri != null) {
			return factory.getOWLDataExactCardinality(cardinality, property, factory.getOWLDatatype(rangeIri));
		} else {
			return factory.getOWLDataExactCardinality(cardinality, property);
		}
	}

	public OWLDataMinCardinality getDataMinCardinality(final String propertyIri, final int cardinality, final String rangeIri) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		if (rangeIri != null) {
			return factory.getOWLDataMinCardinality(cardinality, property, factory.getOWLDatatype(rangeIri));
		} else {
			return factory.getOWLDataMinCardinality(cardinality, property);
		}
	}

	public OWLDataMaxCardinality getDataMaxCardinality(final String propertyIri, final int cardinality, final String rangeIri) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		if (rangeIri != null) {
			return factory.getOWLDataMaxCardinality(cardinality, property, factory.getOWLDatatype(rangeIri));
		} else {
			return factory.getOWLDataMaxCardinality(cardinality, property);
		}
	}

	public OWLDataSomeValuesFrom getDataSomeValuesFrom(final String propertyIri, final String typeIri) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDatatype type = factory.getOWLDatatype(typeIri);
		return factory.getOWLDataSomeValuesFrom(property, type);
	}

	public OWLDataAllValuesFrom getDataAllValuesFrom(final String propertyIri, final String typeIri) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDatatype type = factory.getOWLDatatype(typeIri);
		return factory.getOWLDataAllValuesFrom(property, type);
	}

	public OWLDataHasValue getDataHasValue(final String propertyIri, final OWLLiteral literal) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		return factory.getOWLDataHasValue(property, literal);
	}

	public OWLSubObjectPropertyOfAxiom addSubObjectPropertyOf(final OWLOntology ontology, final String subPropertyIri, final String superPropertyIri, final OWLAnnotation... annotations) {
		final OWLObjectProperty subProperty = factory.getOWLObjectProperty(subPropertyIri);
		final OWLObjectProperty supProperty = factory.getOWLObjectProperty(superPropertyIri);
		final OWLSubObjectPropertyOfAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subProperty, supProperty, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLObjectPropertyDomainAxiom addObjectPropertyDomain(final OWLOntology ontology, final String propertyIri, final String domainIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLClass domain = factory.getOWLClass(domainIri);
		final OWLObjectPropertyDomainAxiom axiom = factory.getOWLObjectPropertyDomainAxiom(property, domain);
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLObjectPropertyRangeAxiom addObjectPropertyRange(final OWLOntology ontology, final String propertyIri, final String rangeIri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLClass range = factory.getOWLClass(rangeIri);
		final OWLObjectPropertyRangeAxiom axiom = factory.getOWLObjectPropertyRangeAxiom(property, range);
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubDataPropertyOfAxiom addSubDataPropertyOf(final OWLOntology ontology, final String subPropertyIri, final String superPropertyIri, final OWLAnnotation... annotations) {
		final OWLDataProperty subProperty = factory.getOWLDataProperty(subPropertyIri);
		final OWLDataProperty supProperty = factory.getOWLDataProperty(superPropertyIri);
		final OWLSubDataPropertyOfAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subProperty, supProperty, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLDataPropertyDomainAxiom addDataPropertyDomain(final OWLOntology ontology, final String propertyIri, final String domainIri) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLClass domain = factory.getOWLClass(domainIri);
		final OWLDataPropertyDomainAxiom axiom = factory.getOWLDataPropertyDomainAxiom(property, domain);
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLDataPropertyRangeAxiom addDataPropertyRange(final OWLOntology ontology, final String propertyIri, final String rangeIri) {
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDatatype range = factory.getOWLDatatype(rangeIri);
		final OWLDataPropertyRangeAxiom axiom = factory.getOWLDataPropertyRangeAxiom(property, range);
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLInverseObjectPropertiesAxiom addInverseProperties(final OWLOntology ontology, final String forwardPropertyIri, final String inversePropertyIri) {
		final OWLObjectProperty forwardproperty = factory.getOWLObjectProperty(forwardPropertyIri);
		final OWLObjectProperty inverseProperty = factory.getOWLObjectProperty(inversePropertyIri);
		final OWLInverseObjectPropertiesAxiom axiom = factory.getOWLInverseObjectPropertiesAxiom(forwardproperty, inverseProperty);
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLClassAssertionAxiom addClassAssertion(final OWLOntology ontology, final String individualIri, final String classIri, final OWLAnnotation... annotations) {
		final OWLNamedIndividual individual = factory.getOWLNamedIndividual(individualIri);
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLClassAssertionAxiom axiom = factory.getOWLClassAssertionAxiom(class_, individual, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLObjectPropertyAssertionAxiom addObjectPropertyAssertion(final OWLOntology ontology, final String individualIri, final String propertyIri, final String objectIri, final OWLAnnotation... annotations) {
		final OWLNamedIndividual object = factory.getOWLNamedIndividual(objectIri);
		final OWLNamedIndividual individual = factory.getOWLNamedIndividual(individualIri);
		return addObjectPropertyAssertion(ontology, individual, propertyIri, object, annotations);
	}

	public OWLObjectPropertyAssertionAxiom addObjectPropertyAssertion(final OWLOntology ontology, final OWLIndividual individual, final String propertyIri, final OWLIndividual object, final OWLAnnotation... annotations) {
		addObjectProperty(ontology, propertyIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(property, individual, object, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLDataPropertyAssertionAxiom addDataPropertyAssertion(final OWLOntology ontology, final String individualIri, final String propertyIri, final OWLLiteral literal, final OWLAnnotation... annotations) {
		final OWLNamedIndividual individual = factory.getOWLNamedIndividual(individualIri);
		return addDataPropertyAssertion(ontology, individual, propertyIri, literal, annotations);
	}

	public OWLDataPropertyAssertionAxiom addDataPropertyAssertion(final OWLOntology ontology, final OWLIndividual individual, final String propertyIri, final OWLLiteral literal, final OWLAnnotation... annotations) {
		addDataProperty(ontology, propertyIri);
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDataPropertyAssertionAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(property, individual, literal, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLAnnotation getAnnotation(final String propertyIri, final OWLAnnotationValue value) {
		final OWLAnnotationProperty property = factory.getOWLAnnotationProperty(propertyIri);
		return factory.getOWLAnnotation(property, value);
	}

	public OWLAnnotation addOntologyAnnotation(final OWLOntology ontology, final OWLAnnotation annotation) {
		AddOntologyAnnotation _addOntologyAnnotation = new AddOntologyAnnotation(ontology, annotation);
		manager.applyChange(_addOntologyAnnotation);
		return annotation;
	}

	public OWLAnnotationAssertionAxiom addAnnotationAssertion(final OWLOntology ontology, final String subjectIri, final OWLAnnotation annotation) {
		final OWLAnnotationAssertionAxiom axiom = factory.getOWLAnnotationAssertionAxiom(IRI.create(subjectIri), annotation);
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLFacetRestriction getFacetRestriction(final OWLFacet facet, final OWLLiteral value) {
		return factory.getOWLFacetRestriction(facet, value);
	}

	public OWLLiteral getLiteral(final boolean value) {
		return factory.getOWLLiteral(value);
	}

	public OWLLiteral getLiteral(final int value) {
		return factory.getOWLLiteral(value);
	}

	public OWLLiteral getLiteral(final double value) {
		return factory.getOWLLiteral(value);
	}

	public OWLLiteral getLiteral(final String value) {
		return factory.getOWLLiteral(value);
	}

	public OWLLiteral getLiteral(final BigDecimal value) {
		return getLiteralWithDatatype(value.toString(), (OmlConstants.XSD_NS + "decimal"));
	}

	public OWLLiteral getLiteralWithDatatype(final String value, final String datatypeIri) {
		final OWLDatatype datatype = factory.getOWLDatatype(datatypeIri);
		return factory.getOWLLiteral(value, datatype);
	}

	public OWLLiteral getLiteralWithLangTag(final String value, final String langTag) {
		return factory.getOWLLiteral(value, langTag);
	}

	public OWLDataProperty getDataProperty(String propertyIri) {
		return factory.getOWLDataProperty(propertyIri);
	}

	public OWLObjectProperty getObjectProperty(String propertyIri) {
		return factory.getOWLObjectProperty(propertyIri);
	}

	public List<OWLAnnotation> checkIfNeeded(final OWLAnnotation[] annotations) {
		if (annotationsOnAxioms) {
			return Arrays.asList(annotations);
		} else {
			return Collections.emptyList();
		}
	}
}