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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.vocab.OWLFacet;

import io.opencaesar.oml.Annotation;
import io.opencaesar.oml.AnnotationProperty;
import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.Assertion;
import io.opencaesar.oml.BooleanLiteral;
import io.opencaesar.oml.CardinalityRestrictionKind;
import io.opencaesar.oml.Concept;
import io.opencaesar.oml.ConceptInstance;
import io.opencaesar.oml.ConceptInstanceReference;
import io.opencaesar.oml.ConceptTypeAssertion;
import io.opencaesar.oml.DecimalLiteral;
import io.opencaesar.oml.Description;
import io.opencaesar.oml.DescriptionBundle;
import io.opencaesar.oml.DifferentFromPredicate;
import io.opencaesar.oml.DoubleLiteral;
import io.opencaesar.oml.Element;
import io.opencaesar.oml.EnumeratedScalar;
import io.opencaesar.oml.FacetedScalar;
import io.opencaesar.oml.FeaturePredicate;
import io.opencaesar.oml.Import;
import io.opencaesar.oml.IntegerLiteral;
import io.opencaesar.oml.KeyAxiom;
import io.opencaesar.oml.LinkAssertion;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.Member;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Predicate;
import io.opencaesar.oml.QuotedLiteral;
import io.opencaesar.oml.RangeRestrictionKind;
import io.opencaesar.oml.Reference;
import io.opencaesar.oml.RelationCardinalityRestrictionAxiom;
import io.opencaesar.oml.RelationEntity;
import io.opencaesar.oml.RelationEntityPredicate;
import io.opencaesar.oml.RelationInstance;
import io.opencaesar.oml.RelationInstanceReference;
import io.opencaesar.oml.RelationRangeRestrictionAxiom;
import io.opencaesar.oml.RelationTargetRestrictionAxiom;
import io.opencaesar.oml.RelationTypeAssertion;
import io.opencaesar.oml.ReverseRelation;
import io.opencaesar.oml.Rule;
import io.opencaesar.oml.SameAsPredicate;
import io.opencaesar.oml.Scalar;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.ScalarPropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.ScalarPropertyRangeRestrictionAxiom;
import io.opencaesar.oml.ScalarPropertyValueAssertion;
import io.opencaesar.oml.ScalarPropertyValueRestrictionAxiom;
import io.opencaesar.oml.SpecializationAxiom;
import io.opencaesar.oml.Structure;
import io.opencaesar.oml.StructureInstance;
import io.opencaesar.oml.StructuredProperty;
import io.opencaesar.oml.StructuredPropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.StructuredPropertyRangeRestrictionAxiom;
import io.opencaesar.oml.StructuredPropertyValueAssertion;
import io.opencaesar.oml.StructuredPropertyValueRestrictionAxiom;
import io.opencaesar.oml.Term;
import io.opencaesar.oml.TypePredicate;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlSwitch;

public class Oml2Owl extends OmlSwitch<Void> {

	public static final List<String> BUILT_IN_ONTOLOGIES = Arrays.asList(new String[] {
		"http://www.w3.org/2001/XMLSchema",
		"http://www.w3.org/1999/02/22-rdf-syntax-ns",
		"http://www.w3.org/2000/01/rdf-schema",
		"http://www.w3.org/2002/07/owl"});

	private final Resource inputResource;
	private final OwlApi owl;
	private OWLOntology ontology;

	public Oml2Owl(final Resource inputResource, final OwlApi owl2) {
		this.inputResource = inputResource;
		owl = owl2;
	}

	public OWLOntology run() {
		Iterable<EObject> iterable = () -> inputResource.getAllContents();
		StreamSupport.stream(iterable.spliterator(), false).forEach(i -> doSwitch(i));
		return ontology;
	}

	@Override
	public Void caseAnnotation(final Annotation annotation) {
		addAnnotation(OmlRead.getAnnotatedElement(annotation), annotation);
		return null;
	}

	@Override
	public Void caseVocabulary(final Vocabulary vocabulary) {
		ontology = owl.createOntology(vocabulary.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Vocabulary)));
		return null;
	}

	@Override
	public Void caseVocabularyBundle(final VocabularyBundle bundle) {
		ontology = owl.createOntology(bundle.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.VocabularyBundle)));
		return null;
	}

	@Override
	public Void caseDescription(final Description description) {
		ontology = owl.createOntology(description.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Description)));
		return null;
	}

	@Override
	public Void caseDescriptionBundle(final DescriptionBundle bundle) {
		ontology = owl.createOntology(bundle.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.DescriptionBundle)));
		return null;
	}

	@Override
	public Void caseAspect(final Aspect aspect) {
		owl.addClass(ontology, aspect.getIri());
		owl.addAnnotationAssertion(ontology, aspect.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Aspect)));
		return null;
	}

	@Override
	public Void caseConcept(final Concept concept) {
		owl.addClass(ontology, concept.getIri());
		owl.addAnnotationAssertion(ontology, concept.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Concept)));
		if (!concept.getEnumeratedInstances().isEmpty()) {
			List<OWLNamedIndividual> individuals = concept.getEnumeratedInstances().stream()
					.map(i -> owl.getNamedIndividual(i.getIri()))
					.collect(Collectors.toList());
			owl.addObjectOneOf(ontology, concept.getIri(), individuals);
		}
		return null;
	}

	@Override
	public Void caseRelationEntity(final RelationEntity entity) {
		owl.addClass(ontology, entity.getIri());
		owl.addAnnotationAssertion(ontology, entity.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.RelationEntity)));
		
		// forward relation
		handleForwardRelation(entity);

		// reverse relation
		handleReverseRelation(entity);
		
		return null;
	}

	@Override
	public Void caseStructure(final Structure structure) {
		owl.addClass(ontology, structure.getIri());
		owl.addAnnotationAssertion(ontology, structure.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Structure)));
		return null;
	}

	@Override
	public Void caseFacetedScalar(final FacetedScalar scalar) {
		owl.addDatatype(ontology, scalar.getIri());
		return null;
	}

	@Override
	public Void caseEnumeratedScalar(final EnumeratedScalar scalar) {
		if (scalar.getLiterals().isEmpty()) {
			owl.addDatatype(ontology, scalar.getIri());
		} else {
			owl.addDataOneOf(ontology, scalar.getIri(), scalar.getLiterals().stream().map(i -> getLiteral(i)).toArray(OWLLiteral[]::new));
		}
		return null;
	}

	@Override
	public Void caseAnnotationProperty(final AnnotationProperty property) {
		owl.addAnnotationProperty(ontology, property.getIri());
		return null;
	}

	@Override
	public Void caseScalarProperty(final ScalarProperty property) {
		final String propertyIri = property.getIri();
		owl.addDataProperty(ontology, propertyIri);
		owl.addAnnotationAssertion(ontology, propertyIri, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.scalarProperty)));
		owl.addDataPropertyDomain(ontology, propertyIri, property.getDomain().getIri());
		owl.addDataPropertyRange(ontology, propertyIri, property.getRange().getIri());
		if (property.isFunctional()) {
			owl.addFunctionalDataProperty(ontology, propertyIri);
		}
		return null;
	}

	@Override
	public Void caseStructuredProperty(final StructuredProperty property) {
		final String propertyIri = property.getIri();
		owl.addObjectProperty(ontology, propertyIri);
		owl.addAnnotationAssertion(ontology, propertyIri, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.structuredProperty)));
		owl.addObjectPropertyDomain(ontology, propertyIri, property.getDomain().getIri());
		owl.addObjectPropertyRange(ontology, propertyIri, property.getRange().getIri());
		if (property.isFunctional()) {
			owl.addFunctionalObjectProperty(ontology, propertyIri);
		}
		return null;
	}

	protected void handleForwardRelation(final RelationEntity entity) {
		// forward relation
		final String forwardName = getForwardName(entity);
		final String forwardIri = getForwardIri(entity);
		owl.addObjectProperty(ontology, forwardIri);
		owl.addAnnotationAssertion(ontology, forwardIri, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.forwardRelation)));
		owl.addObjectPropertyDomain(ontology, forwardIri, entity.getSource().getIri());
		owl.addObjectPropertyRange(ontology, forwardIri, entity.getTarget().getIri());
		if (entity.isFunctional()) {
			owl.addFunctionalObjectProperty(ontology, forwardIri);
		}
		if (entity.isInverseFunctional()) {
			owl.addInverseFunctionalObjectProperty(ontology, forwardIri);
		}
		if (entity.isSymmetric()) {
			owl.addSymmetricObjectProperty(ontology, forwardIri);
		}
		if (entity.isAsymmetric()) {
			owl.addAsymmetricObjectProperty(ontology, forwardIri);
		}
		if (entity.isReflexive()) {
			//see: https://github.com/opencaesar/oml/issues/64
			//owl.addReflexiveObjectProperty(ontology, forwardIri);
			owl.addSubClassOfObjectHasSelf(ontology, entity.getSource().getIri(), forwardIri);
		}
		if (entity.isIrreflexive()) {
			owl.addIrreflexiveObjectProperty(ontology, forwardIri);
		}
		if (entity.isTransitive()) {
			owl.addTransitiveObjectProperty(ontology, forwardIri);
		}
		
		// derivation rule for forward relation
		final ArrayList<SWRLAtom> antedecents = new ArrayList<SWRLAtom>();
		antedecents.add(owl.getObjectPropertyAtom(OmlConstants.sourceRelation, "r", "s"));
		antedecents.add(owl.getClassAtom(entity.getIri(), "r"));
		antedecents.add(owl.getObjectPropertyAtom(OmlConstants.targetRelation, "r", "t"));
		final SWRLObjectPropertyAtom consequent = owl.getObjectPropertyAtom(forwardIri, "s", "t");
		final OWLAnnotation annotation = owl.getAnnotation(RDFS.LABEL.toString(), owl.getLiteral(forwardName+" derivation"));
		owl.addRule(ontology, Collections.singletonList(consequent), antedecents, annotation);
	}

	protected void handleReverseRelation(final RelationEntity entity) {
		// reverse relation
		if (entity.getReverseRelation() != null) {
			ReverseRelation reverse = entity.getReverseRelation();
			final String reverseIri = reverse.getIri();
			owl.addObjectProperty(ontology, reverseIri);
			owl.addAnnotationAssertion(ontology, reverseIri, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.reverseRelation)));
			owl.addInverseProperties(ontology, reverseIri, getForwardIri(entity));
		}
	}

	@Override
	public Void caseRule(final Rule rule) {
		List<OWLAnnotation> annotations = rule.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		if (annotations.stream().filter(a -> a.getProperty().getIRI().toString().equals(RDFS.LABEL.toString())).count() == 0) {
			annotations = Collections.singletonList(owl.getAnnotation(RDFS.LABEL.toString(), owl.getLiteral(rule.getName())));
		}
		owl.addRule(ontology, 
			rule.getConsequent().stream().flatMap(p -> getAtom(p).stream()).collect(Collectors.toList()),
			rule.getAntecedent().stream().flatMap(p -> getAtom(p).stream()).collect(Collectors.toList()), 
			toArray(annotations));
		return null;
	}

	@Override
	public Void caseConceptInstance(final ConceptInstance instance) {
		final String instanceIri = instance.getIri();
		final OWLIndividual individual = owl.addNamedIndividual(ontology, instanceIri);
		instance.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		instance.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseConceptInstanceReference(final ConceptInstanceReference reference) {
		final String instanceIri = OmlRead.resolve(reference).getIri();
		final OWLNamedIndividual individual = owl.getNamedIndividual(instanceIri);
		reference.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		reference.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseRelationInstance(final RelationInstance instance) {
		final String instanceIri = instance.getIri();
		final OWLNamedIndividual individual = owl.addNamedIndividual(ontology, instanceIri);
		instance.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		instance.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseRelationInstanceReference(final RelationInstanceReference reference) {
		final String instanceIri = OmlRead.resolve(reference).getIri();
		final OWLNamedIndividual individual = owl.getNamedIndividual(instanceIri);
		reference.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		reference.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseImport(final Import import_) {
		final String iri = import_.getIri();
		if (isBuiltInOntology(iri)) {
			final List<Import> indirectImports = OmlRead.closure(import_, false, it -> OmlRead.getImports(OmlRead.getImportedOntology(it)));
			indirectImports.forEach(i2 -> {
				final String iri2 = OmlRead.getImportedOntology(i2).getIri();
				if (!isBuiltInOntology(iri2)) {
					owl.addImportsDeclaration(ontology, iri2);
				}
			});
		} else {
			owl.addImportsDeclaration(ontology, iri);
		}
		return null;
	}

	@Override
	public Void caseSpecializationAxiom(final SpecializationAxiom axiom) {
		specializes(OmlRead.getSubTerm(axiom), axiom.getSpecializedTerm(), axiom.getOwningReference());
		return null;
	}

	@Override
	public Void caseScalarPropertyRangeRestrictionAxiom(final ScalarPropertyRangeRestrictionAxiom axiom) {
		if (axiom.getKind() == RangeRestrictionKind.ALL) {
			owl.addDataAllValuesFrom(ontology, OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getProperty().getIri(), axiom.getRange().getIri());
		} else {
			owl.addDataSomeValuesFrom(ontology, OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getProperty().getIri(), axiom.getRange().getIri());
		}
		return null;
	}

	@Override
	public Void caseScalarPropertyValueRestrictionAxiom(final ScalarPropertyValueRestrictionAxiom axiom) {
		owl.addDataHasValue(ontology, OmlRead.getRestrictingClassifier(axiom).getIri(),
				axiom.getProperty().getIri(), getLiteral(axiom.getValue()));
		return null;
	}

	@Override
	public Void caseScalarPropertyCardinalityRestrictionAxiom(final ScalarPropertyCardinalityRestrictionAxiom axiom) {
		if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
			owl.addDataMinCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(), 
					axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
			owl.addDataMaxCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		} else {
			owl.addDataExactCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		}
		return null;
	}

	@Override
	public Void caseStructuredPropertyRangeRestrictionAxiom(final StructuredPropertyRangeRestrictionAxiom axiom) {
		if (axiom.getKind() == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getProperty().getIri(), 
					axiom.getRange().getIri());
		} else {
			owl.addObjectSomeValuesFrom(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getProperty().getIri(), 
					axiom.getRange().getIri());
		}
		return null;
	}

	@Override
	public Void caseStructuredPropertyValueRestrictionAxiom(final StructuredPropertyValueRestrictionAxiom axiom) {
		owl.addObjectHasValue(ontology, 
				OmlRead.getRestrictingClassifier(axiom).getIri(),
				axiom.getProperty().getIri(), 
				createIndividual(axiom.getValue()));
		return null;
	}

	@Override
	public Void caseStructuredPropertyCardinalityRestrictionAxiom(final StructuredPropertyCardinalityRestrictionAxiom axiom) {
		if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
			owl.addObjectMinCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(), 
					axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
			owl.addObjectMaxCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(), 
					axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		} else {
			owl.addObjectExactCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(), 
					axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		}
		return null;
	}

	@Override
	public Void caseRelationRangeRestrictionAxiom(final RelationRangeRestrictionAxiom axiom) {
		if (axiom.getKind() == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getRelation().getIri(), 
					axiom.getRange().getIri());
		} else {
			owl.addObjectSomeValuesFrom(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(),
					axiom.getRelation().getIri(), 
					axiom.getRange().getIri());
		}
		return null;
	}

	@Override
	public Void caseRelationTargetRestrictionAxiom(final RelationTargetRestrictionAxiom axiom) {
		owl.addObjectHasValue(ontology, 
				OmlRead.getRestrictingClassifier(axiom).getIri(),
				axiom.getRelation().getIri(), 
				axiom.getTarget().getIri());
		return null;
	}

	@Override
	public Void caseRelationCardinalityRestrictionAxiom(final RelationCardinalityRestrictionAxiom axiom) {
		if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
			owl.addObjectMinCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(), 
					axiom.getRelation().getIri(), 
					(int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
			owl.addObjectMaxCardinality(ontology,
					OmlRead.getRestrictingClassifier(axiom).getIri(), 
					axiom.getRelation().getIri(), 
					(int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		} else {
			owl.addObjectExactCardinality(ontology, 
					OmlRead.getRestrictingClassifier(axiom).getIri(), 
					axiom.getRelation().getIri(), 
					(int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
		}
		return null;
	}

	@Override
	public Void caseKeyAxiom(final KeyAxiom axiom) {
		owl.addHasKey(ontology, 
				OmlRead.getKeyedEntity(axiom).getIri(), 
				axiom.getProperties().stream()
					.map(i -> {
						if (i instanceof ScalarProperty) 
							return owl.getDataProperty(i.getIri());
						else
							return owl.getObjectProperty(i.getIri());
					}).collect(Collectors.toList()));
		return null;
	}

	@Override
	public Void caseConceptTypeAssertion(final ConceptTypeAssertion assertion) {
		owl.addClassAssertion(ontology, 
				OmlRead.getSubject(assertion).getIri(),
				assertion.getType().getIri());
		return null;
	}

	@Override
	public Void caseRelationTypeAssertion(final RelationTypeAssertion assertion) {
		final RelationInstance instance = OmlRead.getSubject(assertion);
		final String instanceIri = instance.getIri();
		owl.addClassAssertion(ontology, 
				instanceIri, 
				assertion.getType().getIri());
		instance.getSources().forEach(s ->
			owl.addObjectPropertyAssertion(ontology, 
					instanceIri, 
					OmlConstants.sourceRelation,
					s.getIri()));
		instance.getTargets().forEach(t ->
			owl.addObjectPropertyAssertion(ontology, 
					instanceIri, 
					OmlConstants.targetRelation,
					t.getIri()));
		return null;
	}

	protected OWLAnnotation createAnnotation(final Annotation annotation) {
		if (annotation.getValue() != null) {
			final OWLLiteral literal = getLiteral(annotation.getValue());
			return owl.getAnnotation(annotation.getProperty().getIri(), literal);
		} else if (annotation.getReferenceValue() != null) {
			final IRI iri = owl.createIri(annotation.getReferenceValue().getIri());
			return owl.getAnnotation(annotation.getProperty().getIri(), iri);
		} else {
			final OWLLiteral literal = owl.getLiteral("true");
			return owl.getAnnotation(annotation.getProperty().getIri(), literal);
		}
	}

	protected void addAnnotation(final Element element, final Annotation annotation) {
		if (element instanceof Ontology) {
			owl.addOntologyAnnotation(ontology, createAnnotation(annotation));
		} else if (element instanceof Member) {
			owl.addAnnotationAssertion(ontology, ((Member)element).getIri(), createAnnotation(annotation));
		} else if (element instanceof Reference) {
			addAnnotation(OmlRead.resolve((Reference)element), annotation);
		}
	}

	protected void specializes(final Term specific, final Term general, final Reference owningReference, final OWLAnnotation...annotations) {
		if (specific instanceof Aspect && general instanceof Aspect) {
			specializes((Aspect) specific, (Aspect) general, owningReference, annotations);
			return;
		} else if (specific instanceof Concept && general instanceof Aspect) {
			specializes((Concept) specific, (Aspect) general, owningReference, annotations);
			return;
		} else if (specific instanceof Concept && general instanceof Concept) {
			specializes((Concept) specific, (Concept) general, owningReference, annotations);
			return;
		} else if (specific instanceof RelationEntity && general instanceof Aspect) {
			specializes((RelationEntity) specific, (Aspect) general, owningReference, annotations);
			return;
		} else if (specific instanceof RelationEntity && general instanceof RelationEntity) {
			specializes((RelationEntity) specific, (RelationEntity) general, owningReference, annotations);
			return;
		} else if (specific instanceof EnumeratedScalar && general instanceof EnumeratedScalar) {
			specializes((EnumeratedScalar) specific, (EnumeratedScalar) general, owningReference, annotations);
			return;
		} else if (specific instanceof FacetedScalar && general instanceof FacetedScalar) {
			specializes((FacetedScalar) specific, (FacetedScalar) general, owningReference, annotations);
			return;
		} else if (specific instanceof ScalarProperty && general instanceof ScalarProperty) {
			specializes((ScalarProperty) specific, (ScalarProperty) general, owningReference, annotations);
			return;
		} else if (specific instanceof Structure && general instanceof Structure) {
			specializes((Structure) specific, (Structure) general, owningReference, annotations);
			return;
		} else if (specific instanceof StructuredProperty && general instanceof StructuredProperty) {
			specializes((StructuredProperty) specific, (StructuredProperty) general, owningReference, annotations);
			return;
		}
	}

	protected void specializes(final Concept specific, final Concept general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Concept specific, final Aspect general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Aspect specific, final Aspect general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final RelationEntity specific, final RelationEntity general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
		owl.addSubObjectPropertyOf(ontology, getForwardIri(specific), getForwardIri(general), annotations);
	}

	protected void specializes(final RelationEntity specific, final Aspect general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Structure specific, final Structure general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final EnumeratedScalar specific, final EnumeratedScalar general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addDatatypeDefinition(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final FacetedScalar specific, final FacetedScalar general, final Reference owningReference, final OWLAnnotation... annotations) {
		if ((owningReference != null)) {
			owl.addDatatypeDefinition(ontology, specific.getIri(), general.getIri(), annotations);
		} else {
			final ArrayList<OWLFacetRestriction> restrictions = new ArrayList<>();
			if (specific.getLength() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.LENGTH, owl.getLiteral((specific.getLength()).longValue())));
			}
			if (specific.getMaxLength() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.MAX_LENGTH, owl.getLiteral((specific.getLength()).longValue())));
			}
			if (specific.getMinLength() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.MIN_LENGTH, owl.getLiteral((specific.getLength()).longValue())));
			}
			if (specific.getPattern() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.PATTERN, owl.getLiteral(specific.getPattern())));
			}
			if (specific.getLanguage() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.LANG_RANGE, owl.getLiteral(specific.getLanguage())));
			}
			if (specific.getMinInclusive() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.MIN_INCLUSIVE, getLiteral(specific.getMinInclusive())));
			}
			if (specific.getMaxInclusive() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.MAX_INCLUSIVE, getLiteral(specific.getMaxInclusive())));
			}
			if (specific.getMinExclusive() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.MIN_EXCLUSIVE, getLiteral(specific.getMinExclusive())));
			}
			if (specific.getMaxExclusive() != null) {
				restrictions.add(owl.getFacetRestriction(OWLFacet.MAX_EXCLUSIVE, getLiteral(specific.getMaxExclusive())));
			}
			if (!restrictions.isEmpty()) {
				owl.addDatatypeDefinition(ontology, 
						specific.getIri(), 
						general.getIri(),
						restrictions.toArray(new OWLFacetRestriction[0]),
						annotations);
			} else {
				owl.addDatatypeDefinition(ontology, 
						specific.getIri(),
						general.getIri(),
						annotations);
			}
		}
	}

	protected void specializes(final ScalarProperty specific, final ScalarProperty general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubDataPropertyOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final StructuredProperty specific, final StructuredProperty general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubObjectPropertyOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void appliesTo(final Assertion assertion, final OWLIndividual individual) {
		if (assertion instanceof ScalarPropertyValueAssertion && individual != null) {
			appliesTo((ScalarPropertyValueAssertion) assertion, individual);
		} else if (assertion instanceof StructuredPropertyValueAssertion && individual != null) {
			appliesTo((StructuredPropertyValueAssertion) assertion, individual);
		} else if (assertion instanceof LinkAssertion && individual instanceof OWLNamedIndividual) {
			appliesTo((LinkAssertion) assertion, (OWLNamedIndividual) individual);
		}
	}

	protected void appliesTo(final ScalarPropertyValueAssertion assertion, final OWLIndividual individual) {
		owl.addDataPropertyAssertion(ontology, 
				individual, 
				assertion.getProperty().getIri(),
				getLiteral(assertion.getValue()));
	}

	protected void appliesTo(final StructuredPropertyValueAssertion assertion, final OWLIndividual individual) {
		owl.addObjectPropertyAssertion(ontology, 
				individual, 
				assertion.getProperty().getIri(),
				createIndividual(assertion.getValue()));
	}

	protected void appliesTo(final LinkAssertion assertion, final OWLNamedIndividual individual) {
		owl.addObjectPropertyAssertion(ontology, 
				individual.getIRI().getIRIString(),
				assertion.getRelation().getIri(), 
				assertion.getTarget().getIri());
	}

	protected List<SWRLAtom> getAtom(final Predicate predicate) {
		if (predicate instanceof TypePredicate) {
			return getAtom((TypePredicate) predicate);
		} else if (predicate instanceof RelationEntityPredicate) {
			return getAtom((RelationEntityPredicate) predicate);
		} else if (predicate instanceof FeaturePredicate) {
			return getAtom((FeaturePredicate) predicate);
		} else if (predicate instanceof DifferentFromPredicate) {
			return getAtom((DifferentFromPredicate) predicate);
		} else if (predicate instanceof SameAsPredicate) {
			return getAtom((SameAsPredicate) predicate);
		}
		return Collections.emptyList();
	}

	protected List<SWRLAtom> getAtom(final TypePredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		if (predicate.getType() instanceof Scalar) {
			atoms.add(owl.getDataRangeAtom(predicate.getType().getIri(), predicate.getVariable()));
		} else {
			atoms.add(owl.getClassAtom(predicate.getType().getIri(), predicate.getVariable()));
		}
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final RelationEntityPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getClassAtom(predicate.getEntity().getIri(), predicate.getEntityVariable()));
		atoms.add(owl.getObjectPropertyAtom(OmlConstants.sourceRelation, predicate.getEntityVariable(), predicate.getVariable1()));
		if (predicate.getVariable2() != null) {
			atoms.add(owl.getObjectPropertyAtom(OmlConstants.targetRelation, predicate.getEntityVariable(), predicate.getVariable2()));
		} else if (predicate.getInstance2() != null) {
			atoms.add(owl.getObjectPropertyAtom2(OmlConstants.targetRelation, predicate.getEntityVariable(), owl.getNamedIndividual(predicate.getInstance2().getIri())));
		}
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final FeaturePredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		var feature = predicate.getFeature();
		if (feature instanceof AnnotationProperty || feature instanceof ScalarProperty) {
			if (predicate.getVariable2() != null) {
				atoms.add(owl.getDataPropertyAtom(feature.getIri(), predicate.getVariable1(), predicate.getVariable2()));
			} else if (predicate.getLiteral2() != null) {
				atoms.add(owl.getDataPropertyAtom2(feature.getIri(), predicate.getVariable1(), getLiteral(predicate.getLiteral2())));
			}
		} else {
			if (predicate.getVariable2() != null) {
				atoms.add(owl.getObjectPropertyAtom(feature.getIri(), predicate.getVariable1(), predicate.getVariable2()));
			} else if (predicate.getInstance2() != null) {
				atoms.add(owl.getObjectPropertyAtom2(feature.getIri(), predicate.getVariable1(), owl.getNamedIndividual(predicate.getInstance2().getIri())));
			}
		}
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final SameAsPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		if (predicate.getVariable2() != null) {
			atoms.add(owl.getSameIndividualAtom(predicate.getVariable1(), predicate.getVariable2()));
		} else if (predicate.getInstance2() != null) {
			atoms.add(owl.getSameIndividualAtom2(predicate.getVariable1(), owl.getNamedIndividual(predicate.getInstance2().getIri())));
		}
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final DifferentFromPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		if (predicate.getVariable2() != null) {
			atoms.add(owl.getDifferentIndividualsAtom(predicate.getVariable1(), predicate.getVariable2()));
		} else if (predicate.getInstance2() != null) {
			atoms.add(owl.getDifferentIndividualsAtom2(predicate.getVariable1(), owl.getNamedIndividual(predicate.getInstance2().getIri())));
		}
		return atoms;
	}

	protected OWLLiteral getLiteral(final Literal literal) {
		if (literal instanceof BooleanLiteral) {
			return getLiteral((BooleanLiteral) literal);
		} else if (literal instanceof DecimalLiteral) {
			return getLiteral((DecimalLiteral) literal);
		} else if (literal instanceof DoubleLiteral) {
			return getLiteral((DoubleLiteral) literal);
		} else if (literal instanceof IntegerLiteral) {
			return getLiteral((IntegerLiteral) literal);
		} else if (literal instanceof QuotedLiteral) {
			return getLiteral((QuotedLiteral) literal);
		}
		return null;
	}

	protected OWLLiteral getLiteral(final QuotedLiteral literal) {
		// remove invisible characters from the value
		String value = literal.getValue().replaceAll("[^\\x20-\\x7e]", "");
		if (literal.getType() != null) {
			return owl.getLiteralWithDatatype(value, literal.getType().getIri());
		} else if (literal.getLangTag() != null) {
			return owl.getLiteralWithLangTag(value, literal.getLangTag());
		} else {		
			return owl.getLiteral(value);
		}
	}

	protected OWLLiteral getLiteral(final BooleanLiteral literal) {
		return owl.getLiteral(literal.isValue());
	}

	protected OWLLiteral getLiteral(final IntegerLiteral literal) {
		return owl.getLiteral(literal.getValue());
	}

	protected OWLLiteral getLiteral(final DecimalLiteral literal) {
		return owl.getLiteral(literal.getValue());
	}

	protected OWLLiteral getLiteral(final DoubleLiteral literal) {
		return owl.getLiteral(literal.getValue());
	}

	protected OWLAnonymousIndividual createIndividual(final StructureInstance instance) {
		final OWLAnonymousIndividual individual = owl.getAnonymousIndividual();
		instance.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		return individual;
	}

	protected String getForwardName(final RelationEntity entity) {
		if (entity.getForwardRelation() != null) {
			return entity.getForwardRelation().getName();
		} else {
			String name = toFirstUpper(entity.getName());
			return "has"+name+"Forward";
		}
	}

	protected String getForwardIri(final RelationEntity entity) {
		String namespace = entity.getOntology().getNamespace();
		String name = getForwardName(entity);
		return namespace+name;
	}

	static boolean isBuiltInOntology(final String iri) {
		return Oml2Owl.BUILT_IN_ONTOLOGIES.contains(iri);
	}

	private static String toFirstUpper(String s) {
		if (s == null || s.length() == 0)
			return s;
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		if (s.length() == 1)
			return s.toUpperCase();
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	private OWLAnnotation[] toArray(List<OWLAnnotation> annotations) {
		return annotations.toArray(new OWLAnnotation[0]);
	}

}
