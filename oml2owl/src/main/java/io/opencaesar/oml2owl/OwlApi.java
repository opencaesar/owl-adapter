package io.opencaesar.oml2owl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
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
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.vocab.OWLFacet;

public class OwlApi extends io.opencaesar.closeworld.OwlApi {

	protected final boolean annotationsOnAxioms;

	public OwlApi(final OWLOntologyManager manager, final boolean annotationsOnAxioms) {
		super(manager);
		this.annotationsOnAxioms = annotationsOnAxioms;
	}

	public IRI createIri(final String iri) {
		return IRI.create(iri);
	}

	public OWLOntology createOntology(final String iri) {
		try {
			return manager.createOntology(IRI.create(iri));
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

	public OWLDataOneOf addDataOneOf(final OWLOntology ontology, final String subIri, final OWLLiteral... literals) {
		final OWLDatatype datatype = factory.getOWLDatatype(subIri);
		final OWLDataOneOf dataOneOf = factory.getOWLDataOneOf(literals);
		final OWLDatatypeDefinitionAxiom axiom = factory.getOWLDatatypeDefinitionAxiom(datatype, dataOneOf);
		manager.addAxiom(ontology, axiom);
		return dataOneOf;
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

	public OWLAnonymousIndividual getAnonymousIndividual(final String nodeId) {
		return factory.getOWLAnonymousIndividual(nodeId);
	}

	public SWRLRule addRule(final OWLOntology ontology, final List<SWRLAtom> head, final List<SWRLAtom> body, final OWLAnnotation... annotations) {
		final SWRLRule axiom = factory.getSWRLRule(body, head, Arrays.asList(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public SWRLClassAtom getClassAtom(final String classIri, final String variableIri) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final SWRLVariable variable = factory.getSWRLVariable(variableIri);
		return factory.getSWRLClassAtom(class_, variable);
	}

	public SWRLObjectPropertyAtom getObjectPropertyAtom(final String propertyIri, final String variable1Iri, final String variable2Iri) {
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final SWRLVariable variable1 = factory.getSWRLVariable(variable1Iri);
		final SWRLVariable variable2 = factory.getSWRLVariable(variable2Iri);
		return factory.getSWRLObjectPropertyAtom(property, variable1, variable2);
	}

	public SWRLSameIndividualAtom getSameIndividualAtom(final String variable1Iri, final String variable2Iri) {
		final SWRLVariable variable1 = factory.getSWRLVariable(variable1Iri);
		final SWRLVariable variable2 = factory.getSWRLVariable(variable2Iri);
		return factory.getSWRLSameIndividualAtom(variable1, variable2);
	}

	public SWRLDifferentIndividualsAtom getDifferentIndividualsAtom(final String variable1Iri, final String variable2Iri) {
		final SWRLVariable variable1 = factory.getSWRLVariable(variable1Iri);
		final SWRLVariable variable2 = factory.getSWRLVariable(variable2Iri);
		return factory.getSWRLDifferentIndividualsAtom(variable1, variable2);
	}

	public OWLSubClassOfAxiom addSubClassOf(final OWLOntology ontology, final String subIri, final String superIri, final OWLAnnotation... annotations) {
		final OWLClass subClass = factory.getOWLClass(subIri);
		final OWLClass supClass = factory.getOWLClass(superIri);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(subClass, supClass, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLHasKeyAxiom addHasKey(final OWLOntology ontology, final String classIri, final List<String> keyPropertyIris, final OWLAnnotation... annotations) {
		final OWLClass aClass = factory.getOWLClass(classIri);
		final List<OWLDataProperty> keyProperties = keyPropertyIris.stream().map(iri -> factory.getOWLDataProperty(iri)).collect(Collectors.toList());
		final OWLHasKeyAxiom axiom = factory.getOWLHasKeyAxiom(aClass, keyProperties, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addObjectSomeValuesFrom(final OWLOntology ontology, final String classIri, final String propertyIri, final String typeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLClass type = factory.getOWLClass(typeIri);
		final OWLObjectSomeValuesFrom restriction = factory.getOWLObjectSomeValuesFrom(property, type);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addObjectAllValuesFrom(final OWLOntology ontology, final String classIri, final String propertyIri, final String typeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLClass type = factory.getOWLClass(typeIri);
		final OWLObjectAllValuesFrom restriction = factory.getOWLObjectAllValuesFrom(property, type);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addObjectHasValue(final OWLOntology ontology, final String classIri, final String propertyIri, final OWLIndividual individual, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLObjectHasValue restriction = factory.getOWLObjectHasValue(property, individual);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addObjectHasValue(final OWLOntology ontology, final String classIri, final String propertyIri, final String individualIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLNamedIndividual individual = factory.getOWLNamedIndividual(individualIri);
		final OWLObjectHasValue restriction = factory.getOWLObjectHasValue(property, individual);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addObjectExactCardinality(final OWLOntology ontology, final String classIri, final String propertyIri, final int cardinality, final String rangeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLObjectExactCardinality restriction;
		if (rangeIri != null) {
			restriction = factory.getOWLObjectExactCardinality(cardinality, property, factory.getOWLClass(rangeIri));
		} else {
			restriction = factory.getOWLObjectExactCardinality(cardinality, property);
		}
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addObjectMinCardinality(final OWLOntology ontology, final String classIri, final String propertyIri, final int cardinality, final String rangeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLObjectMinCardinality restriction;
		if (rangeIri != null) {
			restriction = factory.getOWLObjectMinCardinality(cardinality, property, factory.getOWLClass(rangeIri));
		} else {
			restriction = factory.getOWLObjectMinCardinality(cardinality, property);
		}
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addObjectMaxCardinality(final OWLOntology ontology, final String classIri, final String propertyIri, final int cardinality, final String rangeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLObjectProperty property = factory.getOWLObjectProperty(propertyIri);
		final OWLObjectMaxCardinality restriction;
		if (rangeIri != null) {
			restriction = factory.getOWLObjectMaxCardinality(cardinality, property, factory.getOWLClass(rangeIri));
		} else {
			restriction = factory.getOWLObjectMaxCardinality(cardinality, property);
		}
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addDataExactCardinality(final OWLOntology ontology, final String classIri, final String propertyIri, final int cardinality, final String rangeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDataExactCardinality restriction;
		if (rangeIri != null) {
			restriction = factory.getOWLDataExactCardinality(cardinality, property, factory.getOWLDatatype(rangeIri));
		} else {
			restriction = factory.getOWLDataExactCardinality(cardinality, property);
		}
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addDataMinCardinality(final OWLOntology ontology, final String classIri, final String propertyIri, final int cardinality, final String rangeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDataMinCardinality restriction;
		if (rangeIri != null) {
			restriction = factory.getOWLDataMinCardinality(cardinality, property, factory.getOWLDatatype(rangeIri));
		} else {
			restriction = factory.getOWLDataMinCardinality(cardinality, property);
		}
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addDataMaxCardinality(final OWLOntology ontology, final String classIri, final String propertyIri, final int cardinality, final String rangeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDataMaxCardinality restriction;
		if (rangeIri != null) {
			restriction = factory.getOWLDataMaxCardinality(cardinality, property, factory.getOWLDatatype(rangeIri));
		} else {
			restriction = factory.getOWLDataMaxCardinality(cardinality, property);
		}
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addDataSomeValuesFrom(final OWLOntology ontology, final String classIri, final String propertyIri, final String typeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDatatype type = factory.getOWLDatatype(typeIri);
		final OWLDataSomeValuesFrom restriction = factory.getOWLDataSomeValuesFrom(property, type);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addDataAllValuesFrom(final OWLOntology ontology, final String classIri, final String propertyIri, final String typeIri, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDatatype type = factory.getOWLDatatype(typeIri);
		final OWLDataAllValuesFrom restriction = factory.getOWLDataAllValuesFrom(property, type);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLSubClassOfAxiom addDataHasValue(final OWLOntology ontology, final String classIri, final String propertyIri, final OWLLiteral literal, final OWLAnnotation... annotations) {
		final OWLClass class_ = factory.getOWLClass(classIri);
		final OWLDataProperty property = factory.getOWLDataProperty(propertyIri);
		final OWLDataHasValue restriction = factory.getOWLDataHasValue(property, literal);
		final OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(class_, restriction, checkIfNeeded(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
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

	public OWLInverseObjectPropertiesAxiom addInverseProperties(final OWLOntology ontology, final String forwardPropertyIri, final String reversePropertyIri) {
		final OWLObjectProperty forwardproperty = factory.getOWLObjectProperty(forwardPropertyIri);
		final OWLObjectProperty reverseProperty = factory.getOWLObjectProperty(reversePropertyIri);
		final OWLInverseObjectPropertiesAxiom axiom = factory.getOWLInverseObjectPropertiesAxiom(forwardproperty, reverseProperty);
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

	public List<OWLAnnotation> checkIfNeeded(final OWLAnnotation... annotations) {
		if (annotationsOnAxioms) {
			return Arrays.asList(annotations);
		} else {
			return Collections.emptyList();
		}
	}
}
