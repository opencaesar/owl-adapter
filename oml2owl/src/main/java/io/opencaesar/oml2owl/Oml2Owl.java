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
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.vocab.OWLFacet;

import io.opencaesar.oml.Annotation;
import io.opencaesar.oml.AnnotationProperty;
import io.opencaesar.oml.Argument;
import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.BooleanLiteral;
import io.opencaesar.oml.BuiltInPredicate;
import io.opencaesar.oml.CardinalityRestrictionKind;
import io.opencaesar.oml.ClassifierEquivalenceAxiom;
import io.opencaesar.oml.Concept;
import io.opencaesar.oml.ConceptInstance;
import io.opencaesar.oml.DecimalLiteral;
import io.opencaesar.oml.Description;
import io.opencaesar.oml.DescriptionBundle;
import io.opencaesar.oml.DifferentFromPredicate;
import io.opencaesar.oml.DoubleLiteral;
import io.opencaesar.oml.Element;
import io.opencaesar.oml.Import;
import io.opencaesar.oml.InstanceEnumerationAxiom;
import io.opencaesar.oml.IntegerLiteral;
import io.opencaesar.oml.KeyAxiom;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.Member;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Predicate;
import io.opencaesar.oml.PropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.PropertyEquivalenceAxiom;
import io.opencaesar.oml.PropertyPredicate;
import io.opencaesar.oml.PropertyRangeRestrictionAxiom;
import io.opencaesar.oml.PropertyRestrictionAxiom;
import io.opencaesar.oml.PropertySelfRestrictionAxiom;
import io.opencaesar.oml.PropertyValueAssertion;
import io.opencaesar.oml.PropertyValueRestrictionAxiom;
import io.opencaesar.oml.QuotedLiteral;
import io.opencaesar.oml.RangeRestrictionKind;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.RelationBase;
import io.opencaesar.oml.RelationEntity;
import io.opencaesar.oml.RelationEntityPredicate;
import io.opencaesar.oml.RelationInstance;
import io.opencaesar.oml.ReverseRelation;
import io.opencaesar.oml.Rule;
import io.opencaesar.oml.SameAsPredicate;
import io.opencaesar.oml.Scalar;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.SpecializationAxiom;
import io.opencaesar.oml.Structure;
import io.opencaesar.oml.StructureInstance;
import io.opencaesar.oml.StructuredProperty;
import io.opencaesar.oml.Term;
import io.opencaesar.oml.TypeAssertion;
import io.opencaesar.oml.TypePredicate;
import io.opencaesar.oml.UnreifiedRelation;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.util.OmlSwitch;

class Oml2Owl extends OmlSwitch<Void> {

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
		addAnnotation(annotation.getAnnotatedElement(), annotation);
		return null;
	}

	@Override
	public Void caseVocabulary(final Vocabulary vocabulary) {
		ontology = owl.createOntology(vocabulary.getPrefix(), vocabulary.getNamespace());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Vocabulary)));
		return null;
	}

	@Override
	public Void caseVocabularyBundle(final VocabularyBundle bundle) {
		ontology = owl.createOntology(bundle.getPrefix(), bundle.getNamespace());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.VocabularyBundle)));
		return null;
	}

	@Override
	public Void caseDescription(final Description description) {
		ontology = owl.createOntology(description.getPrefix(), description.getNamespace());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Description)));
		return null;
	}

	@Override
	public Void caseDescriptionBundle(final DescriptionBundle bundle) {
		ontology = owl.createOntology(bundle.getPrefix(), bundle.getNamespace());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.DescriptionBundle)));
		return null;
	}

	@Override
	public Void caseImport(final Import import_) {
		final String iri = import_.getIri();
		if (!isBuiltInOntology(iri)) {
			owl.addImportsDeclaration(ontology, iri);
		}
		return null;
	}

	@Override
	public Void caseAspect(final Aspect aspect) {
		if (!aspect.isRef()) {
			owl.addClass(ontology, aspect.getIri());
			owl.addAnnotationAssertion(ontology, aspect.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Aspect)));
		}
		aspect.getOwnedPropertyRestrictions().stream().forEach(i -> owl.addSubClassOf(ontology, aspect.getIri(), handlePropertyRestrictionAxiom(i)));
		return null;
	}

	@Override
	public Void caseConcept(final Concept concept) {
		if (!concept.isRef()) {
			owl.addClass(ontology, concept.getIri());
			owl.addAnnotationAssertion(ontology, concept.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Concept)));
		}
		concept.getOwnedPropertyRestrictions().stream().forEach(i -> owl.addSubClassOf(ontology, concept.getIri(), handlePropertyRestrictionAxiom(i)));
		return null;
	}

	@Override
	public Void caseRelationEntity(final RelationEntity entity) {
		if (!entity.isRef()) {
			owl.addClass(ontology, entity.getIri());
			owl.addAnnotationAssertion(ontology, entity.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.RelationEntity)));
		}
		handleForwardRelation(entity);
		entity.getOwnedPropertyRestrictions().stream().forEach(i -> owl.addSubClassOf(ontology, entity.getIri(), handlePropertyRestrictionAxiom(i)));
		return null;
	}
	
	@Override
	public Void caseStructure(final Structure structure) {
		if (!structure.isRef()) {
			owl.addClass(ontology, structure.getIri());
			owl.addAnnotationAssertion(ontology, structure.getIri(), owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.Structure)));
		}
		structure.getOwnedPropertyRestrictions().stream().forEach(i -> owl.addSubClassOf(ontology, structure.getIri(), handlePropertyRestrictionAxiom(i)));
		return null;
	}

	@Override
	public Void caseScalar(final Scalar scalar) {
		if (!scalar.isRef()) {
			owl.addDatatype(ontology, scalar.getIri());
		}
		return null;
	}

	@Override
	public Void caseAnnotationProperty(final AnnotationProperty property) {
		if (!property.isRef()) {
			owl.addAnnotationProperty(ontology, property.getIri());
		}
		return null;
	}

	@Override
	public Void caseScalarProperty(final ScalarProperty property) {
		final String propertyIri = property.getIri();
		if (!property.isRef()) {
			owl.addDataProperty(ontology, propertyIri);
			owl.addAnnotationAssertion(ontology, propertyIri, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.scalarProperty)));
		}
		property.getDomains().forEach(t -> owl.addDataPropertyDomain(ontology, propertyIri, t.getIri()));
		property.getRanges().forEach(t -> owl.addDataPropertyRange(ontology, propertyIri, t.getIri()));
		if (property.isFunctional()) {
			owl.addFunctionalDataProperty(ontology, propertyIri);
		}
		return null;
	}

	@Override
	public Void caseStructuredProperty(final StructuredProperty property) {
		final String propertyIri = property.getIri();
		if (!property.isRef()) {
			owl.addObjectProperty(ontology, propertyIri);
			owl.addAnnotationAssertion(ontology, propertyIri, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.structuredProperty)));
		}
		property.getDomains().forEach(t -> owl.addObjectPropertyDomain(ontology, propertyIri, t.getIri()));
		property.getRanges().forEach(t -> owl.addObjectPropertyRange(ontology, propertyIri, t.getIri()));
		if (property.isFunctional()) {
			owl.addFunctionalObjectProperty(ontology, propertyIri);
		}
		return null;
	}

	@Override
	public Void caseUnreifiedRelation(final UnreifiedRelation relation) {
		handleForwardRelation(relation);
		return null;
	}

	protected void handleForwardRelation(final RelationBase base) {
		final String forwardIri = getForwardIri(base);
		final String forwardName = forwardIri.replace(base.getOntology().getNamespace(), "");
		final String relationKindIri = (base instanceof RelationEntity) ? OmlConstants.forwardRelation : OmlConstants.UnreifiedRelation;

		if (!base.isRef()) {
			owl.addObjectProperty(ontology, forwardIri);
			owl.addAnnotationAssertion(ontology, forwardIri, owl.getAnnotation(OmlConstants.type, owl.createIri(relationKindIri)));
			// derivation rule for forward relation
			if (base instanceof RelationEntity) {
				final ArrayList<SWRLAtom> antedecents = new ArrayList<SWRLAtom>();
				antedecents.add(owl.getClassAtom(base.getIri(), owl.getSWRLVariable("r")));
				antedecents.add(owl.getObjectPropertyAtom(OmlConstants.sourceRelation, owl.getSWRLVariable("r"), owl.getSWRLVariable("s")));
				antedecents.add(owl.getObjectPropertyAtom(OmlConstants.targetRelation, owl.getSWRLVariable("r"), owl.getSWRLVariable("t")));
				final SWRLObjectPropertyAtom consequent = owl.getObjectPropertyAtom(forwardIri, owl.getSWRLVariable("s"), owl.getSWRLVariable("t"));
				final OWLAnnotation annotation = owl.getAnnotation(RDFS.LABEL.toString(), owl.getLiteral(forwardName+" derivation"));
				owl.addRule(ontology, Collections.singletonList(consequent), antedecents, annotation);
			}
		}
		
		base.getSources().forEach(t -> owl.addObjectPropertyDomain(ontology, forwardIri, t.getIri()));
		base.getTargets().forEach(t -> owl.addObjectPropertyRange(ontology, forwardIri, t.getIri()));
		if (base.isFunctional()) {
			owl.addFunctionalObjectProperty(ontology, forwardIri);
		}
		if (base.isInverseFunctional()) {
			owl.addInverseFunctionalObjectProperty(ontology, forwardIri);
		}
		if (base.isSymmetric()) {
			owl.addSymmetricObjectProperty(ontology, forwardIri);
		}
		if (base.isAsymmetric()) {
			owl.addAsymmetricObjectProperty(ontology, forwardIri);
		}
		if (base.isReflexive()) {
			owl.addReflexiveObjectProperty(ontology, forwardIri);
		}
		if (base.isIrreflexive()) {
			owl.addIrreflexiveObjectProperty(ontology, forwardIri);
		}
		if (base.isTransitive()) {
			owl.addTransitiveObjectProperty(ontology, forwardIri);
		}
	}

	@Override
	public Void caseReverseRelation(final ReverseRelation relation) {
		RelationBase base = relation.getRelationBase();
		final String reverseIri = relation.getIri();
		owl.addObjectProperty(ontology, reverseIri);
		owl.addAnnotationAssertion(ontology, reverseIri, owl.getAnnotation(OmlConstants.type, owl.createIri(OmlConstants.reverseRelation)));
		owl.addInverseProperties(ontology, reverseIri, getForwardIri(base));

		base.getSources().forEach(t -> owl.addObjectPropertyRange(ontology, reverseIri, t.getIri()));
		base.getTargets().forEach(t -> owl.addObjectPropertyDomain(ontology, reverseIri, t.getIri()));
		if (base.isFunctional()) {
			owl.addInverseFunctionalObjectProperty(ontology, reverseIri);
		}
		if (base.isInverseFunctional()) {
			owl.addFunctionalObjectProperty(ontology, reverseIri);
		}
		if (base.isSymmetric()) {
			owl.addSymmetricObjectProperty(ontology, reverseIri);
		}
		if (base.isAsymmetric()) {
			owl.addAsymmetricObjectProperty(ontology, reverseIri);
		}
		if (base.isReflexive()) {
			owl.addReflexiveObjectProperty(ontology, reverseIri);
		}
		if (base.isIrreflexive()) {
			owl.addIrreflexiveObjectProperty(ontology, reverseIri);
		}
		if (base.isTransitive()) {
			owl.addTransitiveObjectProperty(ontology, reverseIri);
		}
		return null;
	}

	@Override
	public Void caseRule(final Rule rule) {
		if (!rule.isRef()) {
			List<OWLAnnotation> annotations = rule.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
			if (annotations.stream().filter(a -> a.getProperty().getIRI().toString().equals(RDFS.LABEL.toString())).count() == 0) {
				annotations = Collections.singletonList(owl.getAnnotation(RDFS.LABEL.toString(), owl.getLiteral(rule.getName())));
			}
			owl.addRule(ontology, 
				rule.getConsequent().stream().flatMap(p -> getAtom(p).stream()).collect(Collectors.toList()),
				rule.getAntecedent().stream().flatMap(p -> getAtom(p).stream()).collect(Collectors.toList()), 
				toArray(annotations));
		}
		return null;
	}

	@Override
	public Void caseConceptInstance(final ConceptInstance instance) {
		final String instanceIri = instance.getIri();
		final OWLNamedIndividual individual = instance.isRef() ?
				owl.getNamedIndividual(instanceIri) :
				owl.addNamedIndividual(ontology, instanceIri);
		instance.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseRelationInstance(final RelationInstance instance) {
		final String instanceIri = instance.getIri();
		final OWLNamedIndividual individual = instance.isRef() ?
				owl.getNamedIndividual(instanceIri) :
				owl.addNamedIndividual(ontology, instanceIri);
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
		instance.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseSpecializationAxiom(final SpecializationAxiom axiom) {
		specializes(axiom.getSubTerm(), axiom.getSuperTerm());
		return null;
	}

	@Override
	public Void caseClassifierEquivalenceAxiom(final ClassifierEquivalenceAxiom axiom) {
		if (axiom.getSuperClassifiers().size() == 1 && axiom.getOwnedPropertyRestrictions().size() == 0) {
			owl.addEquivalentClasses(ontology, 
					axiom.getSubClassifier().getIri(), 
					axiom.getSuperClassifiers().get(0).getIri());
		} else if (axiom.getOwnedPropertyRestrictions().size() == 1 && axiom.getSuperClassifiers().size() == 0) {
			owl.addEquivalentClasses(ontology, 
					axiom.getSubClassifier().getIri(), 
					handlePropertyRestrictionAxiom(axiom.getOwnedPropertyRestrictions().get(0)));
		} else {
			owl.addEquivalentClasses(ontology,
					axiom.getSubClassifier().getIri(), 
					owl.getObjectIntersectionOf(
							axiom.getSuperClassifiers().stream().map(c -> c.getIri()).collect(Collectors.toList()),
							axiom.getOwnedPropertyRestrictions().stream().map(r -> handlePropertyRestrictionAxiom(r)).collect(Collectors.toList())));
		}
		return null;
	}

	@Override
	public Void casePropertyEquivalenceAxiom(final PropertyEquivalenceAxiom axiom) {
		if (axiom.getSubProperty() instanceof ScalarProperty) {
			owl.addEquivalentDataProperties(ontology, 
					axiom.getSubProperty().getIri(), 
					axiom.getSuperProperty().getIri());
		} else {
			owl.addEquivalentObjectProperties(ontology, 
					axiom.getSubProperty().getIri(), 
					axiom.getSuperProperty().getIri());
		}
		return null;
	}

	public OWLClassExpression handlePropertyRestrictionAxiom(final PropertyRestrictionAxiom axiom) {
		if (axiom instanceof PropertyRangeRestrictionAxiom) {
			return handlePropertyRangeRestrictionAxiom((PropertyRangeRestrictionAxiom)axiom);
		} else if (axiom instanceof PropertyCardinalityRestrictionAxiom) {
			return handlePropertyCardinalityRestrictionAxiom((PropertyCardinalityRestrictionAxiom)axiom);
		} else if (axiom instanceof PropertyValueRestrictionAxiom) {
			return handlePropertyValueRestrictionAxiom((PropertyValueRestrictionAxiom)axiom);
		} else {// if (axiom instanceof PropertySelfRestrictionAxiom) {
			return handlePropertySelfRestrictionAxiom((PropertySelfRestrictionAxiom)axiom);
		}
	}
	
	public OWLClassExpression handlePropertyRangeRestrictionAxiom(final PropertyRangeRestrictionAxiom axiom) {
		if (axiom.getProperty() instanceof ScalarProperty) {
			if (axiom.getKind() == RangeRestrictionKind.ALL) {
				return owl.getDataAllValuesFrom( 
						axiom.getProperty().getIri(), 
						axiom.getRange().getIri());
			} else {
				return owl.getDataSomeValuesFrom( 
						axiom.getProperty().getIri(), 
						axiom.getRange().getIri());
			}
		} else {
			if (axiom.getKind() == RangeRestrictionKind.ALL) {
				return owl.getObjectAllValuesFrom( 
						axiom.getProperty().getIri(), 
						axiom.getRange().getIri());
			} else {
				return owl.getObjectSomeValuesFrom( 
						axiom.getProperty().getIri(), 
						axiom.getRange().getIri());
			}
		}
	}

	public OWLClassExpression handlePropertyCardinalityRestrictionAxiom(final PropertyCardinalityRestrictionAxiom axiom) {
		if (axiom.getProperty() instanceof ScalarProperty) {
			if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
				return owl.getDataMinCardinality( 
						axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
						(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
			} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
				return owl.getDataMaxCardinality( 
						axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
						(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
			} else {
				return owl.getDataExactCardinality( 
						axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
						(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
			}
		} else {
			if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
				return owl.getObjectMinCardinality( 
						axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
						(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
			} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
				return owl.getObjectMaxCardinality( 
						axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
						(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
			} else {
				return owl.getObjectExactCardinality( 
						axiom.getProperty().getIri(), (int) axiom.getCardinality(), 
						(axiom.getRange() != null) ? axiom.getRange().getIri() : null);
			}
		}
	}

	public OWLClassExpression handlePropertyValueRestrictionAxiom(final PropertyValueRestrictionAxiom axiom) {
		if (axiom.getProperty() instanceof ScalarProperty) {
			return owl.getDataHasValue( 
					axiom.getProperty().getIri(), 
					getLiteral(axiom.getLiteralValue()));
		} else if (axiom.getProperty() instanceof StructuredProperty) {
			return owl.getObjectHasValue( 
					axiom.getProperty().getIri(), 
					createIndividual(axiom.getStructureInstanceValue()));
		} else { //if (axiom.getProperty() instanceof Relation) {
			return owl.getObjectHasValue( 
					axiom.getProperty().getIri(), 
					axiom.getNamedInstanceValue().getIri());
		}
	}

	public OWLClassExpression handlePropertySelfRestrictionAxiom(final PropertySelfRestrictionAxiom axiom) {
		return owl.getObjectHasSelf(axiom.getProperty().getIri());
	}

	@Override
	public Void caseKeyAxiom(final KeyAxiom axiom) {
		owl.addHasKey(ontology, 
				axiom.getKeyedEntity().getIri(), 
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
	public Void caseInstanceEnumerationAxiom(final InstanceEnumerationAxiom axiom) {
		List<OWLNamedIndividual> individuals = axiom.getInstances().stream()
				.map(i -> owl.getNamedIndividual(i.getIri()))
				.collect(Collectors.toList());
		if (!individuals.isEmpty()) {
			owl.addObjectOneOf(ontology, axiom.getEnumeratedConcept().getIri(), individuals);
		}
		return null;
	}

	@Override
	public Void caseTypeAssertion(final TypeAssertion assertion) {
		final NamedInstance instance = assertion.getSubject();
		String subjectIri = instance.getIri();
		owl.addClassAssertion(ontology, 
				subjectIri,
				assertion.getType().getIri());
		return null;
	}

	protected OWLAnnotation createAnnotation(final Annotation annotation) {
		if (annotation.getReferenceValue() != null) {
			final IRI iri = owl.createIri(annotation.getReferenceValue().getIri());
			return owl.getAnnotation(annotation.getProperty().getIri(), iri);
		} if (annotation.getLiteralValue() != null) {
			final OWLLiteral literal = getLiteral(annotation.getLiteralValue());
			return owl.getAnnotation(annotation.getProperty().getIri(), literal);
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
		}
	}

	protected void specializes(final Term specific, final Term general, final OWLAnnotation...annotations) {
		if (specific instanceof Aspect && general instanceof Aspect) {
			specializes((Aspect) specific, (Aspect) general, annotations);
		} else if (specific instanceof Concept && general instanceof Aspect) {
			specializes((Concept) specific, (Aspect) general, annotations);
		} else if (specific instanceof Concept && general instanceof Concept) {
			specializes((Concept) specific, (Concept) general, annotations);
		} else if (specific instanceof RelationEntity && general instanceof Aspect) {
			specializes((RelationEntity) specific, (Aspect) general, annotations);
		} else if (specific instanceof RelationEntity && general instanceof RelationEntity) {
			specializes((RelationEntity) specific, (RelationEntity) general, annotations);
		} else if (specific instanceof Scalar && general instanceof Scalar) {
			specializes((Scalar) specific, (Scalar) general, annotations);
		} else if (specific instanceof ScalarProperty && general instanceof ScalarProperty) {
			specializes((ScalarProperty) specific, (ScalarProperty) general, annotations);
		} else if (specific instanceof Structure && general instanceof Structure) {
			specializes((Structure) specific, (Structure) general, annotations);
		} else if (specific instanceof StructuredProperty && general instanceof StructuredProperty) {
			specializes((StructuredProperty) specific, (StructuredProperty) general, annotations);
		} else if (specific instanceof Relation && general instanceof Relation) {
			specializes((Relation) specific, (Relation) general, annotations);
		}
	}

	protected void specializes(final Concept specific, final Concept general, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Concept specific, final Aspect general, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Aspect specific, final Aspect general, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final RelationEntity specific, final RelationEntity general, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
		owl.addSubObjectPropertyOf(ontology, getForwardIri(specific), getForwardIri(general), annotations);
	}

	protected void specializes(final RelationEntity specific, final Aspect general, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Structure specific, final Structure general, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Scalar specific, final Scalar general, final OWLAnnotation... annotations) {
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
		} else if (specific.getOwnedEnumeration() != null && !specific.getOwnedEnumeration().getLiterals().isEmpty()) {
			owl.addDataOneOf(ontology, specific.getIri(), specific.getOwnedEnumeration().getLiterals().stream().map(i -> getLiteral(i)).toArray(OWLLiteral[]::new));
		} else { // the alias case
			owl.addDatatypeDefinition(ontology, 
					specific.getIri(),
					general.getIri(),
					annotations);
		}
	}

	protected void specializes(final ScalarProperty specific, final ScalarProperty general, final OWLAnnotation... annotations) {
		owl.addSubDataPropertyOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final StructuredProperty specific, final StructuredProperty general, final OWLAnnotation... annotations) {
		owl.addSubObjectPropertyOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void specializes(final Relation specific, final Relation general, final OWLAnnotation... annotations) {
		owl.addSubObjectPropertyOf(ontology, specific.getIri(), general.getIri(), annotations);
	}

	protected void appliesTo(final PropertyValueAssertion assertion, final OWLIndividual individual) {
		if (assertion.getProperty() instanceof ScalarProperty && individual != null) {
			owl.addDataPropertyAssertion(ontology, 
					individual, 
					assertion.getProperty().getIri(),
					getLiteral(assertion.getLiteralValue()));
		} else if (assertion.getProperty() instanceof StructuredProperty && individual != null) {
			owl.addObjectPropertyAssertion(ontology, 
					individual, 
					assertion.getProperty().getIri(),
					createIndividual(assertion.getStructureInstanceValue()));
		} else if (assertion.getProperty() instanceof Relation && individual instanceof OWLNamedIndividual) {
			owl.addObjectPropertyAssertion(ontology, 
					((OWLNamedIndividual)individual).getIRI().getIRIString(),
					assertion.getProperty().getIri(), 
					assertion.getNamedInstanceValue().getIri());
		}
	}

	protected List<SWRLAtom> getAtom(final Predicate predicate) {
		if (predicate instanceof TypePredicate) {
			return getAtom((TypePredicate) predicate);
		} else if (predicate instanceof RelationEntityPredicate) {
			return getAtom((RelationEntityPredicate) predicate);
		} else if (predicate instanceof PropertyPredicate) {
			return getAtom((PropertyPredicate) predicate);
		} else if (predicate instanceof DifferentFromPredicate) {
			return getAtom((DifferentFromPredicate) predicate);
		} else if (predicate instanceof SameAsPredicate) {
			return getAtom((SameAsPredicate) predicate);
		} else if (predicate instanceof BuiltInPredicate) {
			return getAtom((BuiltInPredicate) predicate);
		}
		return Collections.emptyList();
	}
	
	protected List<SWRLAtom> getAtom(final TypePredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		if (predicate.getType() instanceof Scalar) {
			atoms.add(owl.getDataRangeAtom(predicate.getType().getIri(), getSWRLDArgument(predicate.getArgument())));
		} else {
			atoms.add(owl.getClassAtom(predicate.getType().getIri(), getSWRLIArgument(predicate.getArgument())));
		}
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final RelationEntityPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getClassAtom(predicate.getType().getIri(), getSWRLIArgument(predicate.getArgument())));
		atoms.add(owl.getObjectPropertyAtom(OmlConstants.sourceRelation, getSWRLIArgument(predicate.getArgument()), getSWRLIArgument(predicate.getArgument1())));
		atoms.add(owl.getObjectPropertyAtom(OmlConstants.targetRelation, getSWRLIArgument(predicate.getArgument()), getSWRLIArgument(predicate.getArgument2())));
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final PropertyPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		if (predicate.getProperty() instanceof AnnotationProperty) {
			if (predicate.getArgument2().getLiteral() != null) {
				atoms.add(owl.getDataPropertyAtom(predicate.getProperty().getIri(), getSWRLIArgument(predicate.getArgument1()), getSWRLDArgument(predicate.getArgument2())));
			} else {
				atoms.add(owl.getObjectPropertyAtom(predicate.getProperty().getIri(), getSWRLIArgument(predicate.getArgument1()), getSWRLIArgument(predicate.getArgument2())));
			}
		} else if (predicate.getProperty() instanceof ScalarProperty) {
			atoms.add(owl.getDataPropertyAtom(predicate.getProperty().getIri(), getSWRLIArgument(predicate.getArgument1()), getSWRLDArgument(predicate.getArgument2())));
		} else {
			atoms.add(owl.getObjectPropertyAtom(predicate.getProperty().getIri(), getSWRLIArgument(predicate.getArgument1()), getSWRLIArgument(predicate.getArgument2())));
		}
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final SameAsPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getSameIndividualAtom(getSWRLIArgument(predicate.getArgument1()), getSWRLIArgument(predicate.getArgument2())));
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final DifferentFromPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getDifferentIndividualsAtom(getSWRLIArgument(predicate.getArgument1()), getSWRLIArgument(predicate.getArgument2())));
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final BuiltInPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		String builtInIri = predicate.getBuiltIn().getIri();
		var args = predicate.getArguments().stream().map(a -> getSWRLDArgument(a)).collect(Collectors.toList());
		atoms.add(owl.getBuiltInAtom(builtInIri, args));
		return atoms;
	}

	protected SWRLDArgument getSWRLDArgument(Argument argument) {
		if (argument.getVariable() != null) {
			return owl.getSWRLVariable(argument.getVariable());
		} else if (argument.getLiteral() != null) {
			return owl.getSWRLLiteralArgument(getLiteral(argument.getLiteral()));
		}
		return null;
	}

	protected SWRLIArgument getSWRLIArgument(Argument argument) {
		if (argument.getVariable() != null) {
			return owl.getSWRLVariable(argument.getVariable());
		} else if (argument.getInstance() != null) {
			return owl.getSWRLIndividualArgument(owl.getNamedIndividual(argument.getInstance().getIri()));
		}
		return null;
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

	protected String getForwardIri(final RelationBase base) {
		if (base instanceof RelationEntity) {
			RelationEntity entity = (RelationEntity)base;
			if (entity.getForwardRelation() != null) {
				return entity.getOntology().getNamespace() + entity.getForwardRelation().getName();
			} else {
				var name = entity.isRef() ? entity.getRef().getName() : entity.getName();
				return entity.getOntology().getNamespace() + "has"+toFirstUpper(name)+"Forward";
			}
		}
		return base.getIri();
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
