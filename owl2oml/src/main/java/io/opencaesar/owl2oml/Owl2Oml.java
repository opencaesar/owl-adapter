/**
 * 
 * Copyright 2024 California Institute of Technology ("Caltech").
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
package io.opencaesar.owl2oml;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasAnnotations;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataCardinalityRestriction;
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
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
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
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLQuantifiedDataRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import io.opencaesar.oml.AnonymousInstance;
import io.opencaesar.oml.Argument;
import io.opencaesar.oml.CardinalityRestrictionKind;
import io.opencaesar.oml.Description;
import io.opencaesar.oml.DescriptionBundle;
import io.opencaesar.oml.Import;
import io.opencaesar.oml.ImportKind;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Predicate;
import io.opencaesar.oml.RangeRestrictionKind;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.util.OmlBuilder;
import io.opencaesar.oml.util.OmlCatalog;
import io.opencaesar.oml.util.OmlConstants;

/**
 * Converts an OWL ontology that was generated from OML back to OML ontology  
 */
class Owl2Oml {
	
	protected final OWLOntologyManager manager;
	protected final OmlBuilder oml;
	protected final OmlCatalog catalog;
	protected final String outputFileExtension;
	
	/**
	 * Constructs a new instance
	 * 
	 * @param manager
	 * @param oml
	 * @param catalog
	 * @param outputFileExtension
	 */
	public Owl2Oml(OWLOntologyManager manager, OmlBuilder oml, OmlCatalog catalog, String outputFileExtension) {
		this.manager = manager;
		this.oml = oml;
		this.catalog = catalog;
		this.outputFileExtension = outputFileExtension;
	}

	/**
	 * Runs the converter
	 * 
	 * @param owlOntology the given OWL ontology
	 * @return A list of OML ontologies
	 * @throws IOException
	 */
	public List<Ontology> run(OWLOntology owlOntology) throws IOException, OWLException {
		return visitOntology(owlOntology);
	}

	protected List<Ontology> visitOntology(OWLOntology owlOntology) throws IOException, OWLException {
		var namespace = getNamespace(owlOntology);
		var prefix = getPrefix(owlOntology);
		var iri = getIri(owlOntology);
		var uri = getUri(iri);

		Ontology ontology = null;
		
		// create ontology based on type
		var type = getOmlType(owlOntology);
		if (OmlConstants.Vocabulary.equals(type)) {
			ontology = visitVocabulary(owlOntology, uri, namespace, prefix); 
		} else if (OmlConstants.Description.equals(type)) {
			ontology = visitDescription(owlOntology, uri, namespace, prefix); 
		} else if (OmlConstants.VocabularyBundle.equals(type)) {
			ontology = visitVocabularyBundle(owlOntology, uri, namespace, prefix); 
		} else if (OmlConstants.DescriptionBundle.equals(type)) {
			ontology = visitDescriptionBundle(owlOntology, uri, namespace, prefix); 
		}
		
		return (ontology != null) ? Collections.singletonList(ontology) : Collections.emptyList();
	}

	protected Vocabulary visitVocabulary(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var vocabulary = oml.createVocabulary(uri, namespace, prefix);
		visitImports(owlOntology, vocabulary);
		visitAnnotations(owlOntology, vocabulary);
		visitAxioms(owlOntology, vocabulary);
		return vocabulary;
	}
	
	protected Description visitDescription(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var description = oml.createDescription(uri, namespace, prefix);
		visitImports(owlOntology, description);
		visitAnnotations(owlOntology, description);
		visitAxioms(owlOntology, description);
		return description;
	}

	protected VocabularyBundle visitVocabularyBundle(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var bundle = oml.createVocabularyBundle(uri, namespace, prefix);
		visitImports(owlOntology, bundle);
		visitAnnotations(owlOntology, bundle);
		return bundle;
	}

	protected DescriptionBundle visitDescriptionBundle(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var bundle = oml.createDescriptionBundle(uri, namespace, prefix);
		visitImports(owlOntology, bundle);
		visitAnnotations(owlOntology, bundle);
		return bundle;
	}

	protected void visitImports(OWLOntology owlOntology, Ontology ontology) {
		for (var i : owlOntology.getDirectImports()) {
			var type = getOmlType(i);
			var namespace = getNamespace(i);
			createImport(type, namespace, ontology);	
		};
	}

	protected void visitAnnotations(OWLOntology owlOntology, Ontology ontology) {
		for (var annotation : owlOntology.annotationsAsList()) {
			boolean handled = visitAnnotation(annotation, owlOntology, ontology);
			if (!handled) {
				System.out.println(annotation);
			}
		}
	}
		
	protected boolean visitAnnotation(OWLAnnotation annotation, OWLOntology owlOntology, Ontology ontology) {
		var property = annotation.getProperty();
		if (isOmlAnnotationProperty(property.getIRI())) { // ignore oml annotations
			return true;
		}
		var propertyIri = getImportedIri(property.getIRI(), ontology);
		if (propertyIri != null) {
			var value = annotation.getValue();
			if (value.isLiteral()) {
				var literal = visitLiteral(value.asLiteral().get(), ontology);
				if (literal != null) {
					oml.addAnnotation(ontology, propertyIri, literal);
					return true;
				}
			} else {
				var valueIri = getImportedIri(value.asIRI().get(), ontology);
				if (valueIri != null) {
					oml.addAnnotation(ontology, propertyIri, valueIri);
					return true;
				}
			}
		}
		return false;
	}

	protected void visitAxioms(OWLOntology owlOntology, Ontology ontology) {
		for (var axiom : owlOntology.getAxioms()) {
			boolean handled = visitAxiom(axiom, owlOntology, ontology);
			if (!handled) {
				System.out.println(axiom);
			}
		};
	}
	
	protected boolean visitAxiom(OWLAxiom axiom, OWLOntology owlOntology, Ontology ontology) {
		var type = axiom.getAxiomType();
		
		if (AxiomType.DECLARATION.equals(type)) {
			return visitDeclarationAxiom((OWLDeclarationAxiom) axiom, owlOntology, ontology);
		}
		if (AxiomType.ANNOTATION_ASSERTION.equals(type)) {
			return visitAnnotationAssertionAxiom((OWLAnnotationAssertionAxiom) axiom, owlOntology, ontology);
		}
		if (ontology instanceof Vocabulary) {
			if (AxiomType.SWRL_RULE.equals(type)) {
				return visitSwrlRule((SWRLRule) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.INVERSE_OBJECT_PROPERTIES.equals(type)) {
				return visitInverseObjectPropertiesAxiom((OWLInverseObjectPropertiesAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.FUNCTIONAL_DATA_PROPERTY.equals(type)) {
				return visitFunctionalDataPropertyAxiom((OWLFunctionalDataPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.FUNCTIONAL_OBJECT_PROPERTY.equals(type)) {
				return visitFunctionalObjectPropertyAxiom((OWLFunctionalObjectPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY.equals(type)) {
				return visitInverseFunctionalObjectPropertyAxiom((OWLInverseFunctionalObjectPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.SYMMETRIC_OBJECT_PROPERTY.equals(type)) {
				return visitSymmetricObjectPropertyAxiom((OWLSymmetricObjectPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.ASYMMETRIC_OBJECT_PROPERTY.equals(type)) {
				return visitAsymmetricObjectPropertyAxiom((OWLAsymmetricObjectPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.REFLEXIVE_OBJECT_PROPERTY.equals(type)) {
				return visitReflexiveObjectPropertyAxiom((OWLReflexiveObjectPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.IRREFLEXIVE_OBJECT_PROPERTY.equals(type)) {
				return visitIrreflexiveObjectPropertyAxiom((OWLIrreflexiveObjectPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.TRANSITIVE_OBJECT_PROPERTY.equals(type)) {
				return visitTransitiveObjectPropertyAxiom((OWLTransitiveObjectPropertyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.DATA_PROPERTY_DOMAIN.equals(type)) {
				return visitDataPropertyDomainAxiom((OWLDataPropertyDomainAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.DATA_PROPERTY_RANGE.equals(type)) {
				return visitDataPropertyRangeAxiom((OWLDataPropertyRangeAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.OBJECT_PROPERTY_DOMAIN.equals(type)) {
				return visitObjectPropertyDomainAxiom((OWLObjectPropertyDomainAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.OBJECT_PROPERTY_RANGE.equals(type)) {
				return visitObjectPropertyRangeAxiom((OWLObjectPropertyRangeAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.ANNOTATION_PROPERTY_DOMAIN.equals(type)) {
				return visitAnnotationPropertyDomainAxiom((OWLAnnotationPropertyDomainAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.ANNOTATION_PROPERTY_RANGE.equals(type)) {
				return visitAnnotationPropertyRangeAxiom((OWLAnnotationPropertyRangeAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.SUBCLASS_OF.equals(type)) {
				return visitSubClassOfAxiom((OWLSubClassOfAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.SUB_DATA_PROPERTY.equals(type)) {
				return visitSubDataPropertyOfAxiom((OWLSubDataPropertyOfAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.SUB_OBJECT_PROPERTY.equals(type)) {
				return visitSubObjectPropertyOfAxiom((OWLSubObjectPropertyOfAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.SUB_ANNOTATION_PROPERTY_OF.equals(type)) {
				return visitSubAnnotationPropertyOfAxiom((OWLSubAnnotationPropertyOfAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.EQUIVALENT_CLASSES.equals(type)) {
				return visitEquivalentClassesAxiom((OWLEquivalentClassesAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.EQUIVALENT_DATA_PROPERTIES.equals(type)) {
				return visitEquivalentDataPropertiesAxiom((OWLEquivalentDataPropertiesAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.EQUIVALENT_OBJECT_PROPERTIES.equals(type)) {
				return visitEquivalentObjectPropertiesAxiom((OWLEquivalentObjectPropertiesAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.DATATYPE_DEFINITION.equals(type)) {
				return visitDatatypeDefinitionAxiom((OWLDatatypeDefinitionAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.HAS_KEY.equals(type)) {
				return visitHasKeyAxiom((OWLHasKeyAxiom) axiom, owlOntology, (Vocabulary)ontology);
			}
			if (AxiomType.CLASS_ASSERTION.equals(type)) {
				return ((OWLClassAssertionAxiom) axiom).getIndividual() instanceof OWLAnonymousIndividual;
			}
			if (AxiomType.DATA_PROPERTY_ASSERTION.equals(type)) {
				return ((OWLDataPropertyAssertionAxiom) axiom).getSubject() instanceof OWLAnonymousIndividual;
			}
			if (AxiomType.OBJECT_PROPERTY_ASSERTION.equals(type)) {
				return ((OWLObjectPropertyAssertionAxiom) axiom).getSubject() instanceof OWLAnonymousIndividual;
			}
		} else if (ontology instanceof Description) {
			if (AxiomType.CLASS_ASSERTION.equals(type)) {
				return visitClassAssertionAxiom((OWLClassAssertionAxiom) axiom, owlOntology, (Description)ontology);
			}
			if (AxiomType.DATA_PROPERTY_ASSERTION.equals(type)) {
				return visitDataPropertyAssertionAxiom((OWLDataPropertyAssertionAxiom) axiom, owlOntology, ontology);
			}
			if (AxiomType.OBJECT_PROPERTY_ASSERTION.equals(type)) {
				return visitObjectPropertyAssertionAxiom((OWLObjectPropertyAssertionAxiom) axiom, owlOntology, ontology);
			}
			//TBD: REMOVE after adding InverseObjectProperties(isSourceOf, hasSource) to the 'oml' core vocabulary
			if (AxiomType.INVERSE_OBJECT_PROPERTIES.equals(type)) {
				return true;
			}
		}
		return false;
	}

	protected boolean visitDeclarationAxiom(OWLDeclarationAxiom axiom, OWLOntology owlOntology, Ontology ontology) {
		// ignore non-local declarations
		var iri = axiom.getEntity().getIRI();
		if (!isLocalIri(iri, owlOntology)) {
			return true;
		}
		
		// handle declarationis with oml:type annotations
		String type = getOmlType(iri, owlOntology);
		if (ontology instanceof Vocabulary) {
			if (axiom.getEntity() instanceof OWLDatatype) {
				oml.addScalar((Vocabulary)ontology, getFragment(iri));
				return true;
			} else if (axiom.getEntity() instanceof OWLClass) {
				if (OmlConstants.Concept.equals(type)) {
					oml.addConcept((Vocabulary)ontology, getFragment(iri));
					return true;
				} else if (OmlConstants.Structure.equals(type)) {
					oml.addStructure((Vocabulary)ontology, getFragment(iri));
					return true;
				} else if (OmlConstants.RelationEntity.equals(type)) {
					var relationEntity = oml.addRelationEntity((Vocabulary)ontology, getFragment(iri), Collections.emptyList(), Collections.emptyList(), false, false, false, false, false, false, false);
					var forwardIri = getForwardRelationIri(owlOntology, iri);
					if (forwardIri != null) {
						oml.addForwardRelation(relationEntity, getFragment(forwardIri));
					}
					var reverseIri = getReverseRelationIri(owlOntology, iri);
					if (reverseIri != null) {
						oml.addReverseRelation(relationEntity, getFragment(reverseIri));
					}
					return true;
				} else 	{//if (OmlConstants.Aspect.equals(type)) {
					oml.addAspect((Vocabulary)ontology, getFragment(iri));
					return true;
				}
			} else if (axiom.getEntity() instanceof OWLProperty) {
				if (axiom.getEntity() instanceof OWLDataProperty) {
					oml.addScalarProperty((Vocabulary)ontology, getFragment(iri), Collections.emptyList(), Collections.emptyList(), false);
					return true;
				} else if (axiom.getEntity() instanceof OWLAnnotationProperty) {
					if (!(iri.getIRIString().startsWith(OmlConstants.OML_NS))) {
						oml.addAnnotationProperty((Vocabulary)ontology, getFragment(iri));
					}
					return true;
				} else if (OmlConstants.StructuredProperty.equals(type)) {
					oml.addStructuredProperty((Vocabulary)ontology, getFragment(iri), Collections.emptyList(), Collections.emptyList(), false);
					return true;
				} else if (OmlConstants.ForwardRelation.equals(type)) {
					return true; // handled with Relation Entity
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled with Relation Base
				} else {//if (OmlConstants.UnreifiedRelation.equals(type)) {
					var relation = oml.addUnreifiedRelation((Vocabulary)ontology, getFragment(iri), Collections.emptyList(), Collections.emptyList(), false, false, false, false, false, false, false);
					var reverseIri = getReverseRelationIri(owlOntology, iri);
					if (reverseIri != null) {
						oml.addReverseRelation(relation, getFragment(reverseIri));
					}
					return true;
				}
			}
		} else if (ontology instanceof Description) {
			if (OmlConstants.RelationInstance.equals(type)) {
				oml.addRelationInstance((Description)ontology, getFragment(iri), Collections.emptyList(), Collections.emptyList());
				return true;
			} else {// if (OmlConstants.ConceptInstance.equals(type)) {
				oml.addConceptInstance((Description)ontology, getFragment(iri));
				return true;
			}
		}
		
		return false;
	}

	protected boolean visitAnnotationAssertionAxiom(OWLAnnotationAssertionAxiom axiom, OWLOntology owlOntology, Ontology ontology) {
		var property = axiom.getProperty();
		if (isOmlAnnotationProperty(property.getIRI())) { // ignore oml annotations
			return true;
		}
		var subject = axiom.getSubject();
		if (!subject.isIRI()) {
			return false;
		}
		var propertyIri = getImportedIri(property.getIRI(), ontology);
		var subjectIri = getImportedIri(subject.asIRI().get(), ontology);
		if (propertyIri != null && subjectIri != null) {
			var value = axiom.getValue();
			if (value.isLiteral()) {
				var literal = visitLiteral(value.asLiteral().get(), ontology);
				if (literal != null) {
					oml.addAnnotation(ontology, subjectIri, propertyIri, literal);
					return true;
				}
			} else {
				var importedValueIri = getImportedIri(value.asIRI().get(), ontology);
				if (importedValueIri != null) {
					oml.addAnnotation(ontology, subjectIri, propertyIri, importedValueIri);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitSwrlRule(SWRLRule swrlRule, OWLOntology owlOntology, Vocabulary vocabulary) {
		var label = getAnnotationValue(swrlRule, Namespaces.RDFS+"label");
		var type = getOmlType(swrlRule);
		if (OmlConstants.Rule.equals(type)) {
			var name = getFragment(IRI.create(getName(swrlRule)));
			
			var body = new ArrayList<>(swrlRule.getBody());
			var bodyRelEntAtoms = removeRelationEntityAtoms(body);
			var antecedent = new ArrayList<Predicate>();
			for(var atom : body) {
				antecedent.add(visitSwrlAtom(atom, vocabulary));
			}
			for(var atom : bodyRelEntAtoms) {
				antecedent.add(visitRelationEntitySwrlAtom(atom, vocabulary));
			}
			
			var head = new ArrayList<>(swrlRule.getHead());
			var headRelEntAtoms = removeRelationEntityAtoms(head);
			var consequent = new ArrayList<Predicate>();
			for(var atom : head) {
				consequent.add(visitSwrlAtom(atom, vocabulary));
			}
			for(var atom : headRelEntAtoms) {
				consequent.add(visitRelationEntitySwrlAtom(atom, vocabulary));
			}
			
			oml.addRule(vocabulary, name, antecedent.toArray(new Predicate[0]), consequent.toArray(new Predicate[0]));
			
			visitAnnotations(swrlRule, vocabulary);
			
			return true;
		} else if (label != null && label.endsWith(" derivation")) {
			return true; // ignore as it is derived from Relation Entity 
		}
		return false;
	}

	protected Predicate visitSwrlAtom(SWRLAtom atom, Vocabulary vocabulary) {
		if (atom instanceof SWRLClassAtom) {
			var a = (SWRLClassAtom)atom;
			var predicate = a.getPredicate();
			if (predicate instanceof OWLClass) {
				var iri = ((OWLClass)predicate).getIRI().getIRIString();
				var arg = visitArgument(a.getArgument(), vocabulary);
				return oml.createTypePredicate(vocabulary, iri, arg);
			}
		} else if (atom instanceof SWRLDataRangeAtom) {
			var a = (SWRLDataRangeAtom)atom;
			var predicate = a.getPredicate();
			if (predicate instanceof OWLDatatype) {
				var iri = ((OWLDatatype)predicate).getIRI().getIRIString();
				var arg = visitArgument(a.getArgument(), vocabulary);
				return oml.createTypePredicate(vocabulary, iri, arg);
			}
		} else if (atom instanceof SWRLObjectPropertyAtom) {
			var a = (SWRLObjectPropertyAtom)atom;
			var predicate = a.getPredicate();
			if (predicate instanceof OWLObjectProperty) {
				var iri = ((OWLObjectProperty)predicate).getIRI().getIRIString();
				var arg1 = visitArgument(a.getFirstArgument(), vocabulary);
				var arg2 = visitArgument(a.getSecondArgument(), vocabulary);
				return oml.createPropertyPredicate(vocabulary, iri, arg1, arg2);
			}
		} else if (atom instanceof SWRLDataPropertyAtom) {
			var a = (SWRLDataPropertyAtom)atom;
			var predicate = a.getPredicate();
			if (predicate instanceof OWLDataProperty) {
				var iri = ((OWLDataProperty)predicate).getIRI().getIRIString();
				var arg1 = visitArgument(a.getFirstArgument(), vocabulary);
				var arg2 = visitArgument(a.getSecondArgument(), vocabulary);
				return oml.createPropertyPredicate(vocabulary, iri, arg1, arg2);
			}
		} else if (atom instanceof SWRLSameIndividualAtom) {
			var a = (SWRLSameIndividualAtom)atom;
			var arg1 = visitArgument(a.getFirstArgument(), vocabulary);
			var arg2 = visitArgument(a.getSecondArgument(), vocabulary);
			return oml.createSameAsPredicate(vocabulary, arg1, arg2);
		} else if (atom instanceof SWRLDifferentIndividualsAtom) {
			var a = (SWRLDifferentIndividualsAtom)atom;
			var arg1 = visitArgument(a.getFirstArgument(), vocabulary);
			var arg2 = visitArgument(a.getSecondArgument(), vocabulary);
			return oml.createDifferentFromPredicate(vocabulary, arg1, arg2);
		} else if (atom instanceof SWRLBuiltInAtom) {
			var a = (SWRLBuiltInAtom)atom;
			var predicateIri = a.getPredicate().getIRIString();
			var args = new ArrayList<Argument>();
			for (var arg : a.getArguments()) {
				args.add(visitArgument(arg, vocabulary));
			}
			return oml.createBuiltInPredicate(vocabulary, predicateIri, args.toArray(new Argument[0]));
		}
		return null;
	}

	protected Predicate visitRelationEntitySwrlAtom(List<SWRLAtom> atoms, Vocabulary vocabulary) {
		var a1 = (SWRLClassAtom)atoms.get(0);
		var iri = ((OWLClass)a1.getPredicate()).getIRI().getIRIString();
		var rel = visitArgument(a1.getArgument(), vocabulary);
		var a2 = (SWRLObjectPropertyAtom)atoms.get(1);
		var src = visitArgument(a2.getSecondArgument(), vocabulary);
		var a3 = (SWRLObjectPropertyAtom)atoms.get(2);
		var tgt = visitArgument(a3.getSecondArgument(), vocabulary);
		return oml.createRelationEntityPredicate(vocabulary, iri, src, rel, tgt);
	}
	
	protected Argument visitArgument(SWRLArgument arg, Vocabulary vocabulary) {
		if (arg instanceof SWRLVariable) {
			var iri = ((SWRLVariable)arg).getIRI().getIRIString();
			if (iri.startsWith("urn:swrl:var#")) {
				String var = iri.substring(13);
				return oml.createArgument(vocabulary, var, null, null);
			}
		} else if (arg instanceof SWRLLiteralArgument) {
			var literal = visitLiteral(((SWRLLiteralArgument)arg).getLiteral(), vocabulary);
			if (literal != null) {
				return oml.createArgument(vocabulary, null, literal, null);
			}
		} else if (arg instanceof SWRLIndividualArgument) {
			var ind = ((SWRLIndividualArgument)arg).getIndividual();
			if (ind instanceof OWLNamedIndividual) {
				var iri = ((OWLNamedIndividual)ind).getIRI().getIRIString();
				return oml.createArgument(vocabulary, null, null, iri);
			}
		}
		return null;
	}

	protected void visitAnnotations(SWRLRule rule, Ontology ontology) {
		for (var annotation : rule.annotationsAsList()) {
			boolean handled = visitAnnotation(annotation, rule, ontology);
			if (!handled) {
				System.out.println(annotation);
			}
		}
	}
	protected boolean visitAnnotation(OWLAnnotation annotation, SWRLRule rule, Ontology ontology) {
		var property = annotation.getProperty();
		if (isOmlAnnotationProperty(property.getIRI())) {// ignore OML annotations
			return true;
		}
		var propertyIri = getImportedIri(property.getIRI(), ontology);
		if (propertyIri != null) {
			var value = annotation.getValue();
			if (value.isLiteral()) {
				var literal = visitLiteral(value.asLiteral().get(), ontology);
				if (literal != null) {
					oml.addAnnotation(ontology, getName(rule), propertyIri, literal);
					return true;
				}
			} else {
				var valueIri = getImportedIri(value.asIRI().get(), ontology);
				if (valueIri != null) {
					oml.addAnnotation(ontology, getName(rule), propertyIri, valueIri);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitInverseObjectPropertiesAxiom(OWLInverseObjectPropertiesAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		return true; // already visitd when creating relation entities and unreified relations
	}

	protected boolean visitFunctionalDataPropertyAxiom(OWLFunctionalDataPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLDataProperty) {
			var propertyIri = getImportedIri(((OWLDataProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				oml.setScalarProperty(vocabulary, propertyIri, null, null, Boolean.TRUE);
				return true;
			}
		}
		return false;
	}
	
	protected boolean visitFunctionalObjectPropertyAxiom(OWLFunctionalObjectPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
				if (OmlConstants.StructuredProperty.equals(type)) {
					oml.setStructuredProperty(vocabulary, propertyIri, null, null, Boolean.TRUE);
					return true;
				} else if (OmlConstants.ForwardRelation.equals(type)) {
					propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
					oml.setRelationBase(vocabulary, propertyIri, null, null, Boolean.TRUE, null, null, null, null, null, null);
					return true;
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled instead for the relation base
				} else {
					oml.setRelationBase(vocabulary, propertyIri, null, null, Boolean.TRUE, null, null, null, null, null, null);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitInverseFunctionalObjectPropertyAxiom(OWLInverseFunctionalObjectPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
				if (OmlConstants.ForwardRelation.equals(type)) {
					propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, Boolean.TRUE, null, null, null, null, null);
					return true;
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled instead for the relation base
				} else {
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, Boolean.TRUE, null, null, null, null, null);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitSymmetricObjectPropertyAxiom(OWLSymmetricObjectPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
				if (OmlConstants.ForwardRelation.equals(type)) {
					propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, Boolean.TRUE, null, null, null, null);
					return true;
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled instead for the relation base
				} else {
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, Boolean.TRUE, null, null, null, null);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitAsymmetricObjectPropertyAxiom(OWLAsymmetricObjectPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
				if (OmlConstants.ForwardRelation.equals(type)) {
					propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, Boolean.TRUE, null, null, null);
					return true;
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled instead for the relation base
				} else {
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, Boolean.TRUE, null, null, null);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitReflexiveObjectPropertyAxiom(OWLReflexiveObjectPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
				if (OmlConstants.ForwardRelation.equals(type)) {
					propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, null, Boolean.TRUE, null, null);
					return true;
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled instead for the relation base
				} else {
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, null, Boolean.TRUE, null, null);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitIrreflexiveObjectPropertyAxiom(OWLIrreflexiveObjectPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
				if (OmlConstants.ForwardRelation.equals(type)) {
					propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, null, null, Boolean.TRUE, null);
					return true;
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled instead for the relation base
				} else {
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, null, null, Boolean.TRUE, null);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitTransitiveObjectPropertyAxiom(OWLTransitiveObjectPropertyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
			if (propertyIri != null) {
				var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
				if (OmlConstants.ForwardRelation.equals(type)) {
					propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, null, null, null, Boolean.TRUE);
					return true;
				} else if (OmlConstants.ReverseRelation.equals(type)) {
					return true; // handled instead for the relation base
				} else {
					oml.setRelationBase(vocabulary, propertyIri, null, null, null, null, null, null, null, null, Boolean.TRUE);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitDataPropertyDomainAxiom(OWLDataPropertyDomainAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLDataProperty) {
			var domain = axiom.getDomain();
			if (domain instanceof OWLClass) {
				var propertyIri = getImportedIri(((OWLDataProperty)property).getIRI(), vocabulary);
				var domainIri = getImportedIri(((OWLClass)domain).getIRI(), vocabulary);
				if (propertyIri != null && domainIri != null) {
					oml.setScalarProperty(vocabulary, propertyIri, domainIri, null, null);
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean visitDataPropertyRangeAxiom(OWLDataPropertyRangeAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLDataProperty) {
			var range = axiom.getRange();
			if (range instanceof OWLDatatype) {
				var propertyIri = getImportedIri(((OWLDataProperty)property).getIRI(), vocabulary);
				var rangeIri = getImportedIri(((OWLDatatype)range).getIRI(), vocabulary);
				if (propertyIri != null && rangeIri != null) {
					oml.setScalarProperty(vocabulary, propertyIri, null, rangeIri, null);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitObjectPropertyDomainAxiom(OWLObjectPropertyDomainAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var source = axiom.getDomain();
			if (source instanceof OWLClass) {
				var sourceIri = getImportedIri(((OWLClass)source).getIRI(), vocabulary);
				var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
				if (sourceIri != null && propertyIri != null) {
					var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
					if (OmlConstants.StructuredProperty.equals(type)) {
						oml.setStructuredProperty(vocabulary, propertyIri, sourceIri, null, null);
						return true;
					} else if (OmlConstants.ForwardRelation.equals(type)) {
						propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
						oml.setRelationBase(vocabulary, propertyIri, sourceIri, null, null, null, null, null, null, null, null);
						return true;
					} else if (OmlConstants.ReverseRelation.equals(type)) {
						return true; // handled instead for the relation base
					} else {
						oml.setRelationBase(vocabulary, propertyIri, sourceIri, null, null, null, null, null, null, null, null);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean visitObjectPropertyRangeAxiom(OWLObjectPropertyRangeAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var property = axiom.getProperty();
		if (property instanceof OWLObjectProperty) {
			var target = axiom.getRange();
			if (target instanceof OWLClass) {
				var targetIri = getImportedIri(((OWLClass)target).getIRI(), vocabulary);
				var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
				if (targetIri != null && propertyIri != null) {
					var type = getOmlType(((OWLObjectProperty)property).getIRI(), owlOntology);
					if (OmlConstants.StructuredProperty.equals(type)) {
						oml.setStructuredProperty(vocabulary, propertyIri, null, targetIri, null);
						return true;
					} else if (OmlConstants.ForwardRelation.equals(type)) {
						propertyIri = getAnnotationValue(((OWLObjectProperty)property).getIRI(), owlOntology, OmlConstants.relationEntity);
						oml.setRelationBase(vocabulary, propertyIri, null, targetIri, null, null, null, null, null, null, null);
						return true;
					} else if (OmlConstants.ReverseRelation.equals(type)) {
						return true; // handled instead for the relation base
					} else {
						oml.setRelationBase(vocabulary, propertyIri, null, targetIri, null, null, null, null, null, null, null);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean visitAnnotationPropertyDomainAxiom(OWLAnnotationPropertyDomainAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		return false;
	}
	
	protected boolean visitAnnotationPropertyRangeAxiom(OWLAnnotationPropertyRangeAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		return false;
	}

	
	protected boolean visitClassAssertionAxiom(OWLClassAssertionAxiom axiom, OWLOntology owlOntology, Description description) {
		var individual = axiom.getIndividual();
		if (individual instanceof OWLNamedIndividual) {
			var classExpression = axiom.getClassExpression();
			if (classExpression instanceof OWLClass) {
				var instanceIri = getImportedIri(((OWLNamedIndividual)individual).getIRI(), description);
				var typeIri = getImportedIri(((OWLClass)classExpression).getIRI(), description);
				if (instanceIri != null && typeIri != null) {
					oml.addTypeAssertion(description, instanceIri, typeIri);
					return true;
				}
			}
		} else {
			return true; // handled with anonymous instance
		}
		return false;
	}
	
	protected boolean visitDataPropertyAssertionAxiom(OWLDataPropertyAssertionAxiom axiom, OWLOntology owlOntology, Ontology ontology) {		
		var subject = axiom.getSubject();
		if (subject instanceof OWLNamedIndividual) {
			var subjectIri = getImportedIri(((OWLNamedIndividual)subject).getIRI(), ontology);
			if (subjectIri != null) {
				return visitDataPropertyAssertionAxiom(subjectIri, axiom, owlOntology, ontology);
			}
		} else  {
			return true; // handled with anonymous instance
		}
		return false;
	}
	
	protected boolean visitDataPropertyAssertionAxiom(Object subject, OWLDataPropertyAssertionAxiom axiom, OWLOntology owlOntology, Ontology ontology) {		
		var property = axiom.getProperty();
		if (property instanceof OWLDataProperty) {
			var propertyIri = getImportedIri(((OWLDataProperty)property).getIRI(), ontology);
			if (propertyIri != null) {
				var literal = visitLiteral(axiom.getObject(), ontology);
				if (literal != null) {
					oml.addPropertyValueAssertion(ontology, subject, propertyIri, literal);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitObjectPropertyAssertionAxiom(OWLObjectPropertyAssertionAxiom axiom, OWLOntology owlOntology, Ontology ontology) {
		var subject = axiom.getSubject();
		if (subject instanceof OWLNamedIndividual) {
			var subjectIri = getImportedIri(((OWLNamedIndividual)subject).getIRI(), ontology);
			if (subjectIri != null) {
				return visitObjectPropertyAssertionAxiom(subjectIri, axiom, owlOntology, ontology);
			}
		} else {
			return true; // handled with anonymous instance
		}
		return false;
	}

	protected boolean visitObjectPropertyAssertionAxiom(Object subject, OWLObjectPropertyAssertionAxiom axiom, OWLOntology owlOntology, Ontology ontology) {
		if (axiom.getProperty() instanceof OWLObjectProperty) {
			var property = (OWLObjectProperty) axiom.getProperty();
			var object = axiom.getObject();
			if (object instanceof OWLNamedIndividual) {
				var objectIri = getImportedIri(((OWLNamedIndividual)object).getIRI(), ontology);
				if (objectIri != null) {
					if (OmlConstants.hasSource.equals(property.getIRI().getIRIString())) {
						oml.setRelationInstance((Description)ontology, (String)subject, objectIri, null);
						return true;
					} else if (OmlConstants.hasTarget.equals(property.getIRI().getIRIString())) {
						oml.setRelationInstance((Description)ontology, (String)subject, null, objectIri);
						return true;
					} else {
						var propertyIri = getImportedIri(property.getIRI(), ontology);
						if (propertyIri != null) {
							oml.addPropertyValueAssertion(ontology, subject, propertyIri, objectIri);
							return true;
						}
					}
				}
			} else { // object is anonymous individual
				var instance = visitAnonymousIndividual((OWLAnonymousIndividual)object, owlOntology, ontology);
				if (instance != null) {
					if (OmlConstants.isSourceOf.equals(property.getIRI().getIRIString())) {
						var relationEntityIri = owlOntology.classAssertionAxioms((OWLAnonymousIndividual)object)
								.map(a -> a.getClassExpression())
								.filter(c -> c instanceof OWLClass)
								.map(c -> ((OWLClass)c).getIRI())
								.map(iri ->  getImportedIri(iri, ontology))
								.findFirst().orElse(null);
						if (relationEntityIri != null) {
							oml.addForwardRelationValueAssertion(ontology, subject, relationEntityIri, instance);
							return true;
						}
					} else if (OmlConstants.isTargetOf.equals(property.getIRI().getIRIString())) {
						var relationEntityIri = owlOntology.classAssertionAxioms((OWLAnonymousIndividual)object)
								.map(a -> a.getClassExpression())
								.filter(c -> c instanceof OWLClass)
								.map(c -> ((OWLClass)c).getIRI())
								.map(iri ->  getImportedIri(iri, ontology))
								.findFirst().orElse(null);
						if (relationEntityIri != null) {
							oml.addReverseRelationValueAssertion(ontology, subject, relationEntityIri, instance);
							return true;
						}
					} else {
						var propertyIri = getImportedIri(property.getIRI(), ontology);
						if (propertyIri != null) {
							oml.addPropertyValueAssertion(ontology, subject, propertyIri, instance);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	protected AnonymousInstance visitAnonymousIndividual(OWLAnonymousIndividual individual, OWLOntology owlOntology, Ontology ontology) {
		String type = getOmlType(individual, owlOntology);
		if (OmlConstants.StructureInstance.equals(type)) {
			var structureIri = owlOntology.classAssertionAxioms(individual)
					.map(a -> a.getClassExpression())
					.filter(c -> c instanceof OWLClass)
					.map(c -> ((OWLClass)c).getIRI())
					.map(iri ->  getImportedIri(iri, ontology))
					.findFirst().orElse(null);
			var instance = oml.createStructureInstance(ontology, structureIri);
			owlOntology.axioms(individual).forEach(axiom -> {
				if (axiom.getAxiomType().equals(AxiomType.DATA_PROPERTY_ASSERTION)) {
					visitDataPropertyAssertionAxiom(instance, (OWLDataPropertyAssertionAxiom) axiom, owlOntology, ontology);
				} else if (axiom.getAxiomType().equals(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
					visitObjectPropertyAssertionAxiom(instance, (OWLObjectPropertyAssertionAxiom) axiom, owlOntology, ontology);
				}
			});
			return instance;
		} else if (OmlConstants.AnonymousRelationInstance.equals(type)) {
			var targetAxiom = owlOntology.objectPropertyAssertionAxioms(individual)
					.filter(a ->  {
						String p = ((OWLObjectProperty) a.getProperty()).getIRI().getIRIString();
						return p.equals(OmlConstants.hasSource) || p.equals(OmlConstants.hasTarget);
					})
					.findFirst().orElse(null);
			if (targetAxiom != null) {
				String targetIri = getImportedIri(targetAxiom.getObject().asOWLNamedIndividual().getIRI(), ontology);
				var instance = oml.createAnonymousRelationInstance(ontology, targetIri);
				owlOntology.axioms(individual).filter(a -> a != targetAxiom).forEach(axiom -> {
					if (axiom.getAxiomType().equals(AxiomType.DATA_PROPERTY_ASSERTION)) {
						visitDataPropertyAssertionAxiom(instance, (OWLDataPropertyAssertionAxiom) axiom, owlOntology, ontology);
					} else if (axiom.getAxiomType().equals(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
						visitObjectPropertyAssertionAxiom(instance, (OWLObjectPropertyAssertionAxiom) axiom, owlOntology, ontology);
					}
				});
				return instance;
			}
		}
		return null;
	}
	

	protected boolean visitSubClassOfAxiom(OWLSubClassOfAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var subclass = axiom.getSubClass();
		if (subclass instanceof OWLClass) {
			var subclassIri = getImportedIri(((OWLClass)subclass).getIRI(), vocabulary);
			if (subclassIri != null) {
				var superclass = axiom.getSuperClass();
				if (superclass instanceof OWLClass) {
					var superclassIri = getImportedIri(((OWLClass)superclass).getIRI(), vocabulary);
					if (superclassIri != null) {
						oml.addSpecializationAxiom(vocabulary, subclassIri, superclassIri);
						return true;
					}
				} else if (superclass instanceof OWLRestriction) {
					return visitRestriction((OWLRestriction)superclass, subclassIri, owlOntology, vocabulary);
				}
			}
		}
		return false;
	}

	protected boolean visitSubDataPropertyOfAxiom(OWLSubDataPropertyOfAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var subproperty = axiom.getSubProperty();
		if (subproperty instanceof OWLDataProperty) {
			var superproperty = axiom.getSuperProperty();
			if (superproperty instanceof OWLDataProperty) {
				var subpropertyIri = getImportedIri(((OWLDataProperty)subproperty).getIRI(), vocabulary);
				var superpropertyIri = getImportedIri(((OWLDataProperty)superproperty).getIRI(), vocabulary);
				if (subpropertyIri != null && superpropertyIri != null) {
					oml.addSpecializationAxiom(vocabulary, subpropertyIri, superpropertyIri);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitSubObjectPropertyOfAxiom(OWLSubObjectPropertyOfAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var subproperty = axiom.getSubProperty();
		if (subproperty instanceof OWLObjectProperty) {
			var superproperty = axiom.getSuperProperty();
			if (superproperty instanceof OWLObjectProperty) {
				var subpropertyIri = getImportedIri(((OWLObjectProperty)subproperty).getIRI(), vocabulary);
				var superpropertyIri = getImportedIri(((OWLObjectProperty)superproperty).getIRI(), vocabulary);
				if (subpropertyIri != null && superpropertyIri != null) {
					oml.addSpecializationAxiom(vocabulary, subpropertyIri, superpropertyIri);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitSubAnnotationPropertyOfAxiom(OWLSubAnnotationPropertyOfAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		return false;
	}

	protected boolean visitEquivalentClassesAxiom(OWLEquivalentClassesAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var expressions = axiom.getOperandsAsList();
		if (expressions.size() == 2) {
			var classifier = expressions.get(0);
			if (classifier instanceof OWLClass) {
				var classifierIri = getImportedIri(((OWLClass)classifier).getIRI(), vocabulary);
				if (classifierIri != null) {
					var equivalent = expressions.get(1);
					if (equivalent instanceof OWLClass) {
						var equivalentIri = getImportedIri(((OWLClass)equivalent).getIRI(), vocabulary);
						if (equivalentIri != null) {
							oml.addClassifierEquivalenceAxiom(vocabulary, classifierIri, Collections.singletonList(equivalentIri));
							return true;
						}
					} else if (equivalent instanceof OWLRestriction) {
						var omlAxiom = oml.addClassifierEquivalenceAxiom(vocabulary, classifierIri, Collections.emptyList());
						return visitRestriction((OWLRestriction)equivalent, omlAxiom, owlOntology, vocabulary);
					} else if (equivalent instanceof OWLObjectIntersectionOf) {
						var operands = ((OWLObjectIntersectionOf)equivalent).getOperands();
						var equivalents = operands.stream().filter(i -> i instanceof OWLClass).map(i -> getImportedIri(((OWLClass)i).getIRI(), vocabulary)).collect(Collectors.toList());
						var omlAxiom = oml.addClassifierEquivalenceAxiom(vocabulary, classifierIri, equivalents);
						var restrictions = operands.stream().filter(i -> i instanceof OWLRestriction).map(i -> (OWLRestriction)i).collect(Collectors.toList());
						boolean result = true;
						for (var restriction : restrictions) {
							result &= visitRestriction(restriction, omlAxiom, owlOntology, vocabulary);
						}
						return result;
					} else if (equivalent instanceof OWLObjectOneOf) {
						var instances = ((OWLObjectOneOf)equivalent).getIndividuals();
						var instanceIris = new ArrayList<String>();
						var allInstancesOk = true;
						for (var instance : instances) {
							if (instance instanceof OWLNamedIndividual) {
								var instanceIri = getImportedIri(((OWLNamedIndividual)instance).getIRI(), vocabulary);
								if (instanceIri != null) {
									instanceIris.add(instanceIri);
								} else {
									allInstancesOk = false;
								}
							} else {
								allInstancesOk = false; // unexpected situation in oml
							}
						}
						if (allInstancesOk) {
							oml.addInstanceEnumerationAxiom(vocabulary, classifierIri, instanceIris);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	protected boolean visitRestriction(OWLRestriction restriction, Object owner, OWLOntology owlOntology, Vocabulary vocabulary) {
		if (restriction.getProperty() instanceof OWLProperty) {
			var property = (OWLProperty) restriction.getProperty();
			if (restriction instanceof OWLQuantifiedDataRestriction) {
				var range = ((OWLQuantifiedDataRestriction) restriction).getFiller();
				if (range instanceof OWLDatatype) {
					var rangeIri = getImportedIri(((OWLDatatype)range).getIRI(), vocabulary);
					if (rangeIri != null) {
						var propertyIri = getImportedIri(property.getIRI(), vocabulary);
						if (propertyIri != null) {
							if (restriction instanceof OWLDataAllValuesFrom) {
								oml.addPropertyRangeRestrictionAxiom(vocabulary, owner, propertyIri, rangeIri, RangeRestrictionKind.ALL);
								return true;
							} else if (restriction instanceof OWLDataSomeValuesFrom) {
								oml.addPropertyRangeRestrictionAxiom(vocabulary, owner, propertyIri, rangeIri, RangeRestrictionKind.SOME);
								return true;
							} else if (restriction instanceof OWLDataCardinalityRestriction) {
								var cardinality = ((OWLDataCardinalityRestriction)restriction).getCardinality();
								
								if (restriction instanceof OWLDataExactCardinality) {
									oml.addPropertyCardinalityRestrictionAxiom(vocabulary, owner, propertyIri, CardinalityRestrictionKind.EXACTLY, cardinality, rangeIri);
									return true;
								} else if (restriction instanceof OWLDataMinCardinality) {
									oml.addPropertyCardinalityRestrictionAxiom(vocabulary, owner, propertyIri, CardinalityRestrictionKind.MIN, cardinality, rangeIri);
									return true;
								} else if (restriction instanceof OWLDataMaxCardinality) {
									oml.addPropertyCardinalityRestrictionAxiom(vocabulary, owner, propertyIri, CardinalityRestrictionKind.MAX, cardinality, rangeIri);
									return true;
								}
							}
						}
					}
				}
			} else if (restriction instanceof OWLQuantifiedObjectRestriction) {
				var range = ((OWLQuantifiedObjectRestriction) restriction).getFiller();
				if (range instanceof OWLClass) {
					var rangeIri = getImportedIri(((OWLClass)range).getIRI(), vocabulary);
					if (rangeIri != null) {
						var propertyIri = getImportedIri(property.getIRI(), vocabulary);
						if (propertyIri != null) {
							if (restriction instanceof OWLObjectAllValuesFrom) {
								oml.addPropertyRangeRestrictionAxiom(vocabulary, owner, propertyIri, rangeIri, RangeRestrictionKind.ALL);
								return true;
							} else if (restriction instanceof OWLObjectSomeValuesFrom) {
								oml.addPropertyRangeRestrictionAxiom(vocabulary, owner, propertyIri, rangeIri, RangeRestrictionKind.SOME);
								return true;
							} else if (restriction instanceof OWLObjectCardinalityRestriction) {
								var cardinality = ((OWLObjectCardinalityRestriction)restriction).getCardinality();
		
								if (restriction instanceof OWLObjectExactCardinality) {
									oml.addPropertyCardinalityRestrictionAxiom(vocabulary, owner, propertyIri, CardinalityRestrictionKind.EXACTLY, cardinality, rangeIri);
									return true;
								} else if (restriction instanceof OWLObjectMinCardinality) {
									oml.addPropertyCardinalityRestrictionAxiom(vocabulary, owner, propertyIri, CardinalityRestrictionKind.MIN, cardinality, rangeIri);
									return true;
								} else if (restriction instanceof OWLObjectMaxCardinality) {
									oml.addPropertyCardinalityRestrictionAxiom(vocabulary, owner, propertyIri, CardinalityRestrictionKind.MAX, cardinality, rangeIri);
									return true;
								}
							}
						}
					}
				}
			} else if (restriction instanceof OWLDataHasValue) {
				var literal = visitLiteral(((OWLDataHasValue) restriction).getFiller(), vocabulary);
				if (literal != null) {
					var propertyIri = getImportedIri(property.getIRI(), vocabulary);
					if (propertyIri != null) {
						oml.addPropertyValueRestrictionAxiom(vocabulary, owner, propertyIri, literal);
						return true;
					}
				}
			} else if (restriction instanceof OWLObjectHasValue) {
				var value = ((OWLObjectHasValue) restriction).getFiller();
				if (value instanceof OWLNamedIndividual) {
					var valueIri = getImportedIri(((OWLNamedIndividual)value).getIRI(), vocabulary);
					if (valueIri != null) {
						var propertyIri = getImportedIri(property.getIRI(), vocabulary);
						if (propertyIri != null) {
							oml.addPropertyValueRestrictionAxiom(vocabulary, owner, propertyIri, valueIri);
							return true;
						}
					}
				} else {
					var instance = visitAnonymousIndividual((OWLAnonymousIndividual)value, owlOntology, vocabulary);
					if (instance != null) {
						if (OmlConstants.isSourceOf.equals(property.getIRI().getIRIString())) {
							var relationEntityIri = owlOntology.classAssertionAxioms((OWLAnonymousIndividual)value)
									.map(a -> a.getClassExpression())
									.filter(c -> c instanceof OWLClass)
									.map(c -> ((OWLClass)c).getIRI())
									.map(iri ->  getImportedIri(iri, vocabulary))
									.findFirst().orElse(null);
							if (relationEntityIri != null) {
								oml.addForwardRelationValueRestrictionAxiom(vocabulary, owner, relationEntityIri, instance);
								return true;
							}
						} else if (OmlConstants.isTargetOf.equals(property.getIRI().getIRIString())) {
							var relationEntityIri = owlOntology.classAssertionAxioms((OWLAnonymousIndividual)value)
									.map(a -> a.getClassExpression())
									.filter(c -> c instanceof OWLClass)
									.map(c -> ((OWLClass)c).getIRI())
									.map(iri ->  getImportedIri(iri, vocabulary))
									.findFirst().orElse(null);
							if (relationEntityIri != null) {
								oml.addReverseRelationValueRestrictionAxiom(vocabulary, owner, relationEntityIri, instance);
								return true;
							}
						} else {
							var propertyIri = getImportedIri(property.getIRI(), vocabulary);
							if (propertyIri != null) {
								oml.addPropertyValueRestrictionAxiom(vocabulary, owner, propertyIri, instance);
								return true;
							}
						}
					}
				}
			} else if (restriction instanceof OWLObjectHasSelf) {
				var propertyIri = getImportedIri(property.getIRI(), vocabulary);
				if (propertyIri != null) {
					oml.addPropertySelfRestrictionAxiom(vocabulary, owner, propertyIri);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitEquivalentDataPropertiesAxiom(OWLEquivalentDataPropertiesAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var expressions = axiom.getOperandsAsList();
		if (expressions.size() == 2) {
			var property = expressions.get(0);
			if (property instanceof OWLDataProperty) {
				var propertyIri = getImportedIri(((OWLDataProperty)property).getIRI(), vocabulary);
				if (propertyIri != null) {
					var equivalent = expressions.get(1);
					if (equivalent instanceof OWLDataProperty) {
						var equivalentIri = getImportedIri(((OWLDataProperty)equivalent).getIRI(), vocabulary);
						if (equivalentIri != null) {
							oml.addPropertyEquivalenceAxiom(vocabulary, propertyIri, equivalentIri);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	protected boolean visitEquivalentObjectPropertiesAxiom(OWLEquivalentObjectPropertiesAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var expressions = axiom.getOperandsAsList();
		if (expressions.size() == 2) {
			var property = expressions.get(0);
			if (property instanceof OWLObjectProperty) {
				var propertyIri = getImportedIri(((OWLObjectProperty)property).getIRI(), vocabulary);
				if (propertyIri != null) {
					var equivalent = expressions.get(1);
					if (equivalent instanceof OWLObjectProperty) {
						var equivalentIri = getImportedIri(((OWLObjectProperty)equivalent).getIRI(), vocabulary);
						if (equivalentIri != null) {
							oml.addPropertyEquivalenceAxiom(vocabulary, propertyIri, equivalentIri);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	protected boolean visitDatatypeDefinitionAxiom(OWLDatatypeDefinitionAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var datatype = axiom.getDatatype();
		var datatypeIri = getImportedIri(datatype.getIRI(), vocabulary);
		if (datatypeIri != null) {
			var datarange = axiom.getDataRange();
			if (datarange instanceof OWLDatatype) {
				var datarangeIri = getImportedIri(((OWLDatatype)datarange).getIRI(), vocabulary);
				if (datarangeIri != null) {
					oml.addScalarEquivalenceAxiom(vocabulary, datatypeIri, datarangeIri, 
						null, null, null, null, null, null, null, null, null);
					return true;
				}
			} else if (datarange instanceof OWLDatatypeRestriction) {
				var restriction = (OWLDatatypeRestriction) datarange;
				datarange = restriction.getDatatype();
				var datarangeIri = getImportedIri(((OWLDatatype)datarange).getIRI(), vocabulary);
				if (datarangeIri != null) {
					var facetRestrictions = restriction.getFacetRestrictions();
					var length = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.LENGTH)).map(i -> Integer.valueOf(i.getFacetValue().getLiteral())).findAny().orElse(null);
					var minLength = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.MIN_LENGTH)).map(i -> Integer.valueOf(i.getFacetValue().getLiteral())).findAny().orElse(null);
					var maxLength = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.MAX_LENGTH)).map(i -> Integer.valueOf(i.getFacetValue().getLiteral())).findAny().orElse(null);
					var pattern = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.PATTERN)).map(i -> i.getFacetValue().getLiteral()).findAny().orElse(null);
					var language = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.LANG_RANGE)).map(i -> i.getFacetValue().getLiteral()).findAny().orElse(null);
					var minInclusive = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.MIN_INCLUSIVE)).map(i -> visitLiteral(i.getFacetValue(), vocabulary)).filter(Objects::nonNull).findAny().orElse(null);
					var minExclusive = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.MIN_EXCLUSIVE)).map(i -> visitLiteral(i.getFacetValue(), vocabulary)).filter(Objects::nonNull).findAny().orElse(null);
					var maxInclusive = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.MAX_INCLUSIVE)).map(i -> visitLiteral(i.getFacetValue(), vocabulary)).filter(Objects::nonNull).findAny().orElse(null);
					var maxExclusive = facetRestrictions.stream().filter(i -> i.getFacet().equals(OWLFacet.MAX_EXCLUSIVE)).map(i -> visitLiteral(i.getFacetValue(), vocabulary)).filter(Objects::nonNull).findAny().orElse(null);
					oml.addScalarEquivalenceAxiom(vocabulary, datatypeIri, datarangeIri, 
						length, minLength, maxLength, pattern, language, minInclusive, minExclusive, maxInclusive, maxExclusive);
					return true;
				}
			} else if (datarange instanceof OWLDataOneOf) {
				var literals = ((OWLDataOneOf)datarange).getValues().stream().map(i -> visitLiteral(i, vocabulary)).filter(Objects::nonNull).toArray(Literal[]::new);
				if (literals.length > 0) {
					oml.addLiteralEnumerationAxiom(vocabulary, datatypeIri, literals);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean visitHasKeyAxiom(OWLHasKeyAxiom axiom, OWLOntology owlOntology, Vocabulary vocabulary) {
		var domain = axiom.getClassExpression();
		if (domain instanceof OWLClass) {
			var domainIri = getImportedIri(((OWLClass)domain).getIRI(), vocabulary);
			if (domainIri != null) {
				var keyPropertyIris = new ArrayList<String>();
				var allImportsOK = true;
				for (var property : axiom.getPropertyExpressions()) {
					if (property instanceof OWLProperty) {
						var keyPropertyIri = getImportedIri(((OWLProperty)property).getIRI(), vocabulary);
						if (keyPropertyIri != null) {
							keyPropertyIris.add(keyPropertyIri);
						} else {
							allImportsOK = false;
						}
					}
				}
				if (!keyPropertyIris.isEmpty() && allImportsOK) {
					oml.addKeyAxiom(vocabulary, domainIri, keyPropertyIris);
					return true;
				}
			}
		}
		return false;
	}

	protected Literal visitLiteral(OWLLiteral literal, Ontology ontology) {
		if (literal.isBoolean()) {
			return oml.createBooleanLiteral(literal.parseBoolean());
		} else if (literal.isInteger()) {
			return oml.createIntegerLiteral(literal.parseInteger());
		} else if (literal.isDouble()) {
			return oml.createDoubleLiteral(literal.parseDouble());
		} else if (XSDVocabulary.DECIMAL.getIRI().equals(literal.getDatatype().getIRI())) {
			return oml.createDecimalLiteral(new BigDecimal(literal.getLiteral()));
		} else {
			var lang = literal.getLang();
			if (isStringIri(literal.getDatatype().getIRI())) {
				return oml.createQuotedLiteral(
						ontology,
						literal.getLiteral(),
						null,
						lang.length()==0 ? null : lang);
			} else {
				var typeIri = getImportedIri(literal.getDatatype().getIRI(), ontology);
				if (typeIri != null) {
					return oml.createQuotedLiteral(
						ontology,
						literal.getLiteral(),
						isStringIri(literal.getDatatype().getIRI()) ? null : typeIri,
						lang.length()==0 ? null : lang);
				}
			}
		}
		return null;
	}

	//----------------------------------------------------------------------------------------
		
	protected String getOmlType(OWLAnnotationSubject subject, OWLOntology owlOntology) {
		return getAnnotationValue(subject, owlOntology, OmlConstants.type);
	}

	protected String getOmlType(HasAnnotations hasAnnotations) {
		return getAnnotationValue(hasAnnotations, OmlConstants.type);
	}

	protected boolean isOmlAnnotationProperty(IRI propertyIri) {
		return propertyIri.getIRIString().startsWith(OmlConstants.OML_NS);
	}

	protected String getName(SWRLRule rule) {
		return getAnnotationValue(rule, OmlConstants.name);
	}

	protected String getNamespace(OWLOntology owlOntology) {
		return getAnnotationValue(owlOntology, OmlConstants.namespace);
	}

	protected String getPrefix(OWLOntology owlOntology) {
		return getAnnotationValue(owlOntology, OmlConstants.prefix);
	}

	protected String getIri(OWLOntology owlOntology) {
		String iri = owlOntology.getOntologyID().getOntologyIRI().get().getIRIString();
		if (iri.endsWith("#") || iri.endsWith("/")) {
			iri = iri.substring(0, iri.length()-1);
		}
		return iri;
	}

	protected URI getUri(String ontologyIri) {
		try {
			return catalog.resolveUri(URI.createURI(ontologyIri+"."+outputFileExtension));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

    protected IRI getForwardRelationIri(OWLOntology owlOntology, IRI relationEntityIri) {
    	for (OWLAnnotationAssertionAxiom axiom : owlOntology.axioms(AxiomType.ANNOTATION_ASSERTION).collect(Collectors.toList())) {
			if (OmlConstants.relationEntity.equals(axiom.getProperty().getIRI().getIRIString())) {
				if (axiom.getValue().asIRI().get().equals(relationEntityIri)) {
					return axiom.getSubject().asIRI().get();
				}
			}
		}
		return null;
	}

    protected IRI getReverseRelationIri(OWLOntology owlOntology, IRI relationEntityIri) {
    	for (OWLAnnotationAssertionAxiom axiom : owlOntology.axioms(AxiomType.ANNOTATION_ASSERTION).collect(Collectors.toList())) {
			if (OmlConstants.relationBase.equals(axiom.getProperty().getIRI().getIRIString())) {
				if (axiom.getValue().asIRI().get().equals(relationEntityIri)) {
					return axiom.getSubject().asIRI().get();
				}
			}
		}
		return null;
    }

	// Namespaces for core vocabularies
	private static final Map<String, String> STANDARD_NS = new HashMap<>();
	static {
		STANDARD_NS.put(Namespaces.XSD.getPrefixIRI(), Namespaces.XSD.getPrefixName());
		STANDARD_NS.put(Namespaces.RDF.getPrefixIRI(), Namespaces.RDF.getPrefixName());
		STANDARD_NS.put(Namespaces.RDFS.getPrefixIRI(), Namespaces.RDFS.getPrefixName());
		STANDARD_NS.put(Namespaces.OWL.getPrefixIRI(), Namespaces.OWL.getPrefixName());
		STANDARD_NS.put(Namespaces.SWRLB.getPrefixIRI(), Namespaces.SWRLB.getPrefixName());
	}

	protected boolean isStandardNamespace(String namespace) {
		return STANDARD_NS.containsKey(namespace);
	}
	    
	protected String getStandardPrefix(String namespace) {
		return STANDARD_NS.get(namespace);
	}

	protected boolean isStringIri(IRI iri) {
		return iri.equals(OWL2Datatype.XSD_STRING.getIRI()) || iri.equals(OWL2Datatype.RDF_LANG_STRING.getIRI());
	}

	protected boolean isLocalIri(IRI iri, OWLOntology owlOntology) {
		return getNamespace(iri).equals(getNamespace(owlOntology));
	}

	protected String getNamespace(IRI iri) {
		return iri.getNamespace();
	}

	protected String getFragment(IRI iri) {
		return iri.getFragment();
	}

	protected String getAnnotationValue(HasAnnotations hasAnnotations, String propertyIri) {
		var property = manager.getOWLDataFactory().getOWLAnnotationProperty(propertyIri);
		var annotation = hasAnnotations.annotations(property).findFirst().orElse(null);
		var value = (annotation != null) ? annotation.getValue() : null;
		if (value instanceof OWLLiteral) {
			return value.asLiteral().get().getLiteral();
		} else if (value instanceof IRI) {
			return value.asIRI().get().getIRIString();
		}
		return null;
	}

	protected String getAnnotationValue(OWLAnnotationSubject subject, OWLOntology owlOntology, String propertyIri) {
		var property = manager.getOWLDataFactory().getOWLAnnotationProperty(propertyIri);
		var annotation = owlOntology.annotationAssertionAxioms(subject).filter(i -> i.getProperty().equals(property)).findFirst().orElse(null);
		var value = (annotation != null) ? annotation.getValue() : null;
		if (value instanceof OWLLiteral) {
			return value.asLiteral().get().getLiteral();
		} else if (value instanceof IRI) {
			return value.asIRI().get().getIRIString();
		}
		return null;
	}

	protected Import createImport(String type, String namespace, Ontology ontology) {
		if (ontology instanceof Vocabulary) {
			if (OmlConstants.Vocabulary.equals(type)) {
				return oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
			}
		} else if (ontology instanceof Description) {
			if (OmlConstants.Description.equals(type)) {
				return oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
			}
		} else if (ontology instanceof VocabularyBundle) {
			if (OmlConstants.VocabularyBundle.equals(type)) {
				return oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
			} else if (OmlConstants.Vocabulary.equals(type)) {
				return oml.addImport(ontology, ImportKind.INCLUSION, namespace, null);
			}
		} else {//if (ontology instanceof DescriptionBundle) {
			if (OmlConstants.DescriptionBundle.equals(type)) {
				return oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
			} else if (OmlConstants.Description.equals(type)) {
				return oml.addImport(ontology, ImportKind.INCLUSION, namespace, null);
			}
		}
		return oml.addImport(ontology, ImportKind.USAGE, namespace, null);
	}

	protected String getImportedIri(IRI iri, Ontology ontology) {
		String ontologyNamespace = getNamespace(iri);
		// import non-local IRI
		if (!ontologyNamespace.equals(ontology.getNamespace())) {
			var ontologyIri = IRI.create(ontologyNamespace.substring(0, ontologyNamespace.length()-1));
			OWLOntology importedOntology = manager.getOntology(ontologyIri);
			if (importedOntology != null || isStandardNamespace(ontologyNamespace)) {
				Import imp = ontology.getOwnedImports().stream().filter(i -> i.getNamespace().equals(ontologyNamespace)).findAny().orElse(null);
				if (imp == null) {
					String ontologyType = (importedOntology != null) ? getOmlType(importedOntology) : OmlConstants.Vocabulary;
					imp = createImport(ontologyType, ontologyNamespace, ontology);
				}
				if (imp.getPrefix() == null) {
					String ontologyPrefix = (importedOntology != null) ? getPrefix(importedOntology) : getStandardPrefix(ontologyNamespace);
					imp.setPrefix(ontologyPrefix);
				}
			} else {
				return null;//System.out.println("Could not import ontology: "+ontologyNamespace);
			}
		}
		return iri.getIRIString();
	}

	protected List<List<SWRLAtom>> removeRelationEntityAtoms(List<SWRLAtom> atoms) {
		List<List<SWRLAtom>> relEntAtoms = new ArrayList<>();
		for (int i=0; i<atoms.size(); i++) {
			if (i+1 < atoms.size()) {
				var iri = ((HasIRI)atoms.get(i+1).getPredicate()).getIRI().getIRIString();
				if (iri.startsWith(OmlConstants.OML_NS)) {
					List<SWRLAtom> relEntAtom = new ArrayList<>();
					relEntAtom.add(atoms.remove(i));
					relEntAtom.add(atoms.remove(i));
					relEntAtom.add(atoms.remove(i));
					relEntAtoms.add(relEntAtom);
					i--;
				}
			}
		}
		return relEntAtoms;
	}
	
}
