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
import io.opencaesar.oml.EntityPredicate;
import io.opencaesar.oml.EnumeratedScalar;
import io.opencaesar.oml.FacetedScalar;
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
import io.opencaesar.oml.RelationPredicate;
import io.opencaesar.oml.RelationRangeRestrictionAxiom;
import io.opencaesar.oml.RelationTargetRestrictionAxiom;
import io.opencaesar.oml.RelationTypeAssertion;
import io.opencaesar.oml.ReverseRelation;
import io.opencaesar.oml.Rule;
import io.opencaesar.oml.SameAsPredicate;
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
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlVisitor;

public class Oml2Owl extends OmlVisitor<Void> {

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
		addsAnnotation(OmlRead.getAnnotatedElement(annotation), annotation);
		return null;
	}

	@Override
	public Void caseVocabulary(final Vocabulary vocabulary) {
		ontology = owl.createOntology(vocabulary.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.ontologyType, owl.createIri(OmlConstants.Vocabulary)));
		return null;
	}

	@Override
	public Void caseVocabularyBundle(final VocabularyBundle bundle) {
		ontology = owl.createOntology(bundle.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.ontologyType, owl.createIri(OmlConstants.VocabularyBundle)));
		return null;
	}

	@Override
	public Void caseDescription(final Description description) {
		ontology = owl.createOntology(description.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.ontologyType, owl.createIri(OmlConstants.Description)));
		return null;
	}

	@Override
	public Void caseDescriptionBundle(final DescriptionBundle bundle) {
		ontology = owl.createOntology(bundle.getIri());
		owl.addOntologyAnnotation(ontology, owl.getAnnotation(OmlConstants.ontologyType, owl.createIri(OmlConstants.DescriptionBundle)));
		return null;
	}

	@Override
	public Void caseAspect(final Aspect aspect) {
		owl.addClass(ontology, OmlRead.getIri(aspect));
		owl.addSubClassOf(ontology, OmlRead.getIri(aspect), OmlConstants.Aspect);
		return null;
	}

	@Override
	public Void caseConcept(final Concept concept) {
		owl.addClass(ontology, OmlRead.getIri(concept));
		owl.addSubClassOf(ontology, OmlRead.getIri(concept), OmlConstants.Concept);
		return null;
	}

	@Override
	public Void caseRelationEntity(final RelationEntity entity) {
		owl.addClass(ontology, OmlRead.getIri(entity));
		owl.addSubClassOf(ontology, OmlRead.getIri(entity), OmlConstants.RelationEntity);
		
		// source relation
		final String sourceRelationIri = getSourceIri(entity);
		owl.addObjectProperty(ontology, sourceRelationIri);
		owl.addSubObjectPropertyOf(ontology, sourceRelationIri, OmlConstants.sourceRelation);
		owl.addObjectPropertyDomain(ontology, sourceRelationIri, OmlRead.getIri(entity));
		owl.addObjectPropertyRange(ontology, sourceRelationIri, OmlRead.getIri(entity.getSource()));
		owl.addFunctionalObjectProperty(ontology, sourceRelationIri);
		if (entity.isFunctional()) {
			owl.addInverseFunctionalObjectProperty(ontology, sourceRelationIri);
		}
		if (entity.isInverseFunctional()) {
			owl.addFunctionalObjectProperty(ontology, sourceRelationIri);
		}
		
		// inverse source relation
		final String inverseSourceRelationIri = getInverseSourceIri(entity);
		if (inverseSourceRelationIri != null) {
			owl.addObjectProperty(ontology, inverseSourceRelationIri);
			owl.addSubObjectPropertyOf(ontology, inverseSourceRelationIri, OmlConstants.inverseSourceRelation);
			owl.addInverseProperties(ontology, inverseSourceRelationIri, sourceRelationIri);
		}
		
		// target relation
		final String targetRelationIri = getTargetIri(entity);
		owl.addObjectProperty(ontology, targetRelationIri);
		owl.addSubObjectPropertyOf(ontology, targetRelationIri, OmlConstants.targetRelation);
		owl.addObjectPropertyDomain(ontology, targetRelationIri, OmlRead.getIri(entity));
		owl.addObjectPropertyRange(ontology, targetRelationIri, OmlRead.getIri(entity.getTarget()));
		owl.addFunctionalObjectProperty(ontology, targetRelationIri);
		if (entity.isFunctional()) {
			owl.addFunctionalObjectProperty(ontology, targetRelationIri);
		}
		if (entity.isInverseFunctional()) {
			owl.addInverseFunctionalObjectProperty(ontology, targetRelationIri);
		}
		
		// inverse target relation
		final String inverseTargetRelationIri = getInverseTargetIri(entity);
		if (inverseTargetRelationIri != null) {
			owl.addObjectProperty(ontology, inverseTargetRelationIri);
			owl.addSubObjectPropertyOf(ontology, inverseTargetRelationIri, OmlConstants.inverseTargetRelation);
			owl.addInverseProperties(ontology, inverseTargetRelationIri, targetRelationIri);
		}

		// forward relation
		handleForwardRelation(entity);

		// reverse relation
		handleReverseRelation(entity);
		
		return null;
	}

	@Override
	public Void caseStructure(final Structure structure) {
		owl.addClass(ontology, OmlRead.getIri(structure));
		owl.addSubClassOf(ontology, OmlRead.getIri(structure), OmlConstants.Structure);
		return null;
	}

	@Override
	public Void caseFacetedScalar(final FacetedScalar scalar) {
		owl.addDatatype(ontology, OmlRead.getIri(scalar));
		return null;
	}

	@Override
	public Void caseEnumeratedScalar(final EnumeratedScalar scalar) {
		owl.addDataOneOf(ontology, OmlRead.getIri(scalar), scalar.getLiterals().stream().map(i -> getLiteral(i)).toArray(OWLLiteral[]::new));
		return null;
	}

	@Override
	public Void caseAnnotationProperty(final AnnotationProperty property) {
		owl.addAnnotationProperty(ontology, OmlRead.getIri(property));
		return null;
	}

	@Override
	public Void caseScalarProperty(final ScalarProperty property) {
		final String propertyIri = OmlRead.getIri(property);
		owl.addDataProperty(ontology, propertyIri);
		owl.addSubDataPropertyOf(ontology, propertyIri, OmlConstants.scalarProperty);
		owl.addDataPropertyDomain(ontology, propertyIri, OmlRead.getIri(property.getDomain()));
		owl.addDataPropertyRange(ontology, propertyIri, OmlRead.getIri(property.getRange()));
		if (property.isFunctional()) {
			owl.addFunctionalDataProperty(ontology, propertyIri);
		}
		return null;
	}

	@Override
	public Void caseStructuredProperty(final StructuredProperty property) {
		final String propertyIri = OmlRead.getIri(property);
		owl.addObjectProperty(ontology, propertyIri);
		owl.addSubObjectPropertyOf(ontology, propertyIri, OmlConstants.structuredProperty);
		owl.addObjectPropertyDomain(ontology, propertyIri, OmlRead.getIri(property.getDomain()));
		owl.addObjectPropertyRange(ontology, propertyIri, OmlRead.getIri(property.getRange()));
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
		owl.addSubObjectPropertyOf(ontology, forwardIri, OmlConstants.forwardRelation);
		owl.addObjectPropertyDomain(ontology, forwardIri, OmlRead.getIri(entity.getSource()));
		owl.addObjectPropertyRange(ontology, forwardIri, OmlRead.getIri(entity.getTarget()));
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
			owl.addReflexiveObjectProperty(ontology, forwardIri);
		}
		if (entity.isIrreflexive()) {
			owl.addIrreflexiveObjectProperty(ontology, forwardIri);
		}
		if (entity.isTransitive()) {
			owl.addTransitiveObjectProperty(ontology, forwardIri);
		}
		
		// derivation rule for forward relation
		final ArrayList<SWRLAtom> antedecents = new ArrayList<SWRLAtom>();
		antedecents.add(owl.getObjectPropertyAtom(getSourceIri(entity), getSwrlIri("r"), getSwrlIri("s")));
		antedecents.add(owl.getObjectPropertyAtom(getTargetIri(entity), getSwrlIri("r"), getSwrlIri("t")));
		final SWRLObjectPropertyAtom consequent = owl.getObjectPropertyAtom(forwardIri, getSwrlIri("s"), getSwrlIri("t"));
		final OWLAnnotation annotation = owl.getAnnotation(RDFS.LABEL.toString(), owl.getLiteral(forwardName+" derivation"));
		owl.addRule(ontology, Collections.singletonList(consequent), antedecents, annotation);
	}

	protected void handleReverseRelation(final RelationEntity entity) {
		// reverse relation
		if (entity.getReverseRelation() != null) {
			ReverseRelation reverse = entity.getReverseRelation();
			final String reverseIri = OmlRead.getIri(reverse);
			owl.addObjectProperty(ontology, reverseIri);
			owl.addSubObjectPropertyOf(ontology, reverseIri, OmlConstants.reverseRelation);
			owl.addInverseProperties(ontology, reverseIri, OmlRead.getIri(reverse.getInverse()));
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
		final String instanceIri = OmlRead.getIri(instance);
		final OWLIndividual individual = owl.addNamedIndividual(ontology, instanceIri);
		instance.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		instance.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseConceptInstanceReference(final ConceptInstanceReference reference) {
		final String instanceIri = OmlRead.getIri(OmlRead.resolve(reference));
		final OWLNamedIndividual individual = owl.getNamedIndividual(instanceIri);
		reference.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		reference.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseRelationInstance(final RelationInstance instance) {
		final String instanceIri = OmlRead.getIri(instance);
		final OWLNamedIndividual individual = owl.addNamedIndividual(ontology, instanceIri);
		instance.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		instance.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseRelationInstanceReference(final RelationInstanceReference reference) {
		final String instanceIri = OmlRead.getIri(OmlRead.resolve(reference));
		final OWLNamedIndividual individual = owl.getNamedIndividual(instanceIri);
		reference.getOwnedPropertyValues().forEach(it -> appliesTo(it, individual));
		reference.getOwnedLinks().forEach(it -> appliesTo(it, individual));
		return null;
	}

	@Override
	public Void caseImport(final Import import_) {
		Ontology importedOntology = OmlRead.getImportedOntology(import_);
		if (importedOntology != null) {
			final String iri = importedOntology.getIri();
			if (Oml2Owl.isBuiltInOntology(iri)) {
				final List<Import> indirectImports = OmlRead.closure(import_, it -> OmlRead.getImportsWithSource(OmlRead.getImportedOntology(it)));
				indirectImports.forEach(i2 -> {
					final String iri2 = OmlRead.getImportedOntology(i2).getIri();
					if (!Oml2Owl.isBuiltInOntology(iri2)) {
						owl.addImportsDeclaration(ontology, iri2);
					}
				});
			} else {
				owl.addImportsDeclaration(ontology, iri);
			}
		} else {
			throw new RuntimeException("Could not resolve IRI '"+ import_.getUri()+ "'");
		}
		return null;
	}

	@Override
	public Void caseSpecializationAxiom(final SpecializationAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		specializes(OmlRead.getSpecificTerm(axiom), axiom.getSpecializedTerm(), axiom.getOwningReference(), toArray(annotations));
		return null;
	}

	@Override
	public Void caseScalarPropertyRangeRestrictionAxiom(final ScalarPropertyRangeRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		if (axiom.getKind() == RangeRestrictionKind.ALL) {
			owl.addDataAllValuesFrom(ontology, OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getProperty()), OmlRead.getIri(axiom.getRange()),
					toArray(annotations));
		} else {
			owl.addDataSomeValuesFrom(ontology, OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getProperty()), OmlRead.getIri(axiom.getRange()),
					toArray(annotations));
		}
		return null;
	}

	@Override
	public Void caseScalarPropertyValueRestrictionAxiom(final ScalarPropertyValueRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addDataHasValue(ontology, OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
				OmlRead.getIri(axiom.getProperty()), getLiteral(axiom.getValue()),
				toArray(annotations));
		return null;
	}

	@Override
	public Void caseScalarPropertyCardinalityRestrictionAxiom(final ScalarPropertyCardinalityRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
			owl.addDataMinCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)), 
					OmlRead.getIri(axiom.getProperty()), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
			owl.addDataMaxCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getProperty()), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		} else {
			owl.addDataExactCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getProperty()), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		}
		return null;
	}

	@Override
	public Void caseStructuredPropertyRangeRestrictionAxiom(final StructuredPropertyRangeRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		if (axiom.getKind() == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getProperty()), 
					OmlRead.getIri(axiom.getRange()),
					toArray(annotations));
		} else {
			owl.addObjectSomeValuesFrom(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getProperty()), 
					OmlRead.getIri(axiom.getRange()),
					toArray(annotations));
		}
		return null;
	}

	@Override
	public Void caseStructuredPropertyValueRestrictionAxiom(final StructuredPropertyValueRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addObjectHasValue(ontology, 
				OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
				OmlRead.getIri(axiom.getProperty()), 
				createIndividual(axiom.getValue()),
				toArray(annotations));
		return null;
	}

	@Override
	public Void caseStructuredPropertyCardinalityRestrictionAxiom(final StructuredPropertyCardinalityRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
			owl.addObjectMinCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)), 
					OmlRead.getIri(axiom.getProperty()), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
			owl.addObjectMaxCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)), 
					OmlRead.getIri(axiom.getProperty()), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		} else {
			owl.addObjectExactCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)), 
					OmlRead.getIri(axiom.getProperty()), (int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		}
		return null;
	}

	@Override
	public Void caseRelationRangeRestrictionAxiom(final RelationRangeRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		if (axiom.getKind() == RangeRestrictionKind.ALL) {
			owl.addObjectAllValuesFrom(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getRelation()), 
					OmlRead.getIri(axiom.getRange()),
					toArray(annotations));
		} else {
			owl.addObjectSomeValuesFrom(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
					OmlRead.getIri(axiom.getRelation()), 
					OmlRead.getIri(axiom.getRange()),
					toArray(annotations));
		}
		return null;
	}

	@Override
	public Void caseRelationTargetRestrictionAxiom(final RelationTargetRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addObjectHasValue(ontology, 
				OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)),
				OmlRead.getIri(axiom.getRelation()), 
				OmlRead.getIri(axiom.getTarget()),
				toArray(annotations));
		return null;
	}

	@Override
	public Void caseRelationCardinalityRestrictionAxiom(final RelationCardinalityRestrictionAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		if (axiom.getKind() == CardinalityRestrictionKind.MIN) {
			owl.addObjectMinCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)), 
					OmlRead.getIri(axiom.getRelation()), 
					(int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		} else if (axiom.getKind() == CardinalityRestrictionKind.MAX) {
			owl.addObjectMaxCardinality(ontology,
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)), 
					OmlRead.getIri(axiom.getRelation()), 
					(int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		} else {
			owl.addObjectExactCardinality(ontology, 
					OmlRead.getIri(OmlRead.getRestrictingClassifier(axiom)), 
					OmlRead.getIri(axiom.getRelation()), 
					(int) axiom.getCardinality(), 
					(axiom.getRange() != null) ? OmlRead.getIri(axiom.getRange()) : null,
					toArray(annotations));
		}
		return null;
	}

	@Override
	public Void caseKeyAxiom(final KeyAxiom axiom) {
		final List<OWLAnnotation> annotations = axiom.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addHasKey(ontology, 
				OmlRead.getIri(OmlRead.getKeyedEntity(axiom)), 
				axiom.getProperties().stream().map(i -> OmlRead.getIri(i)).collect(Collectors.toList()), 
				toArray(annotations));
		return null;
	}

	@Override
	public Void caseConceptTypeAssertion(final ConceptTypeAssertion assertion) {
		final List<OWLAnnotation> annotations = assertion.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addClassAssertion(ontology, 
				OmlRead.getIri(OmlRead.getConceptInstance(assertion)),
				OmlRead.getIri(assertion.getType()),
				toArray(annotations));
		return null;
	}

	@Override
	public Void caseRelationTypeAssertion(final RelationTypeAssertion assertion) {
		final List<OWLAnnotation> annotations = assertion.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		final RelationInstance instance = OmlRead.getRelationInstance(assertion);
		final String instanceIri = OmlRead.getIri(instance);
		owl.addClassAssertion(ontology, 
				instanceIri, 
				OmlRead.getIri(assertion.getType()),
				toArray(annotations));
		instance.getSources().forEach(s ->
			owl.addObjectPropertyAssertion(ontology, 
					instanceIri, 
					getSourceIri(assertion.getType()),
					OmlRead.getIri(s)));
		instance.getTargets().forEach(t ->
			owl.addObjectPropertyAssertion(ontology, 
					instanceIri, 
					getTargetIri(assertion.getType()),
					OmlRead.getIri(t)));
		return null;
	}

	protected OWLAnnotation createAnnotation(final Annotation annotation) {
		final OWLLiteral literal;
		if (annotation.getValue() != null) {
			literal = getLiteral(annotation.getValue());
		} else {
			literal = owl.getLiteral("true");
		}
		return owl.getAnnotation(OmlRead.getIri(annotation.getProperty()), literal);
	}

	protected void addsAnnotation(final Element element, final Annotation annotation) {
		if (element instanceof Ontology) {
			owl.addOntologyAnnotation(ontology, createAnnotation(annotation));
		} else if (element instanceof Member) {
			owl.addAnnotationAssertion(ontology, OmlRead.getIri((Member)element), createAnnotation(annotation));
		} else if (element instanceof Reference) {
			addsAnnotation(OmlRead.resolve((Reference)element), annotation);
		}
	}

	protected void specializes(final Term specific, final Term general, final Reference owningReference, final OWLAnnotation[] annotations) {
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
		owl.addSubClassOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
	}

	protected void specializes(final Concept specific, final Aspect general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
	}

	protected void specializes(final Aspect specific, final Aspect general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
	}

	protected void specializes(final RelationEntity specific, final RelationEntity general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
		owl.addSubObjectPropertyOf(ontology, getSourceIri(specific), getSourceIri(general), annotations);
		owl.addSubObjectPropertyOf(ontology, getTargetIri(specific), getTargetIri(general), annotations);
		owl.addSubObjectPropertyOf(ontology, getForwardIri(specific), getForwardIri(general), annotations);
	}

	protected void specializes(final RelationEntity specific, final Aspect general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
	}

	protected void specializes(final Structure specific, final Structure general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubClassOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
	}

	protected void specializes(final EnumeratedScalar specific, final EnumeratedScalar general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addDatatypeDefinition(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
	}

	protected void specializes(final FacetedScalar specific, final FacetedScalar general, final Reference owningReference, final OWLAnnotation... annotations) {
		if ((owningReference != null)) {
			owl.addDatatypeDefinition(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
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
						OmlRead.getIri(specific), 
						OmlRead.getIri(general),
						restrictions.toArray(new OWLFacetRestriction[0]),
						annotations);
			} else {
				owl.addDatatypeDefinition(ontology, 
						OmlRead.getIri(specific),
						OmlRead.getIri(general),
						annotations);
			}
		}
	}

	protected void specializes(final ScalarProperty specific, final ScalarProperty general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubDataPropertyOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
	}

	protected void specializes(final StructuredProperty specific, final StructuredProperty general, final Reference owningReference, final OWLAnnotation... annotations) {
		owl.addSubObjectPropertyOf(ontology, OmlRead.getIri(specific), OmlRead.getIri(general), annotations);
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
		final List<OWLAnnotation> annotations = assertion.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addDataPropertyAssertion(ontology, 
				individual, 
				OmlRead.getIri(assertion.getProperty()),
				getLiteral(assertion.getValue()),
				toArray(annotations));
	}

	protected void appliesTo(final StructuredPropertyValueAssertion assertion, final OWLIndividual individual) {
		final List<OWLAnnotation> annotations = assertion.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addObjectPropertyAssertion(ontology, 
				individual, 
				OmlRead.getIri(assertion.getProperty()),
				createIndividual(assertion.getValue()),
				toArray(annotations));
	}

	protected void appliesTo(final LinkAssertion assertion, final OWLNamedIndividual individual) {
		final List<OWLAnnotation> annotations = assertion.getOwnedAnnotations().stream().map(it -> createAnnotation(it)).collect(Collectors.toList());
		owl.addObjectPropertyAssertion(ontology, 
				individual.getIRI().getIRIString(),
				OmlRead.getIri(assertion.getRelation()), 
				OmlRead.getIri(assertion.getTarget()),
				toArray(annotations));
	}

	protected List<SWRLAtom> getAtom(final Predicate predicate) {
		if (predicate instanceof DifferentFromPredicate) {
			return getAtom((DifferentFromPredicate) predicate);
		} else if (predicate instanceof EntityPredicate) {
			return getAtom((EntityPredicate) predicate);
		} else if (predicate instanceof RelationEntityPredicate) {
			return getAtom((RelationEntityPredicate) predicate);
		} else if (predicate instanceof RelationPredicate) {
			return getAtom((RelationPredicate) predicate);
		} else if (predicate instanceof SameAsPredicate) {
			return getAtom((SameAsPredicate) predicate);
		}
		return Collections.emptyList();
	}

	protected List<SWRLAtom> getAtom(final EntityPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getClassAtom(OmlRead.getIri(predicate.getEntity()), getSwrlIri(predicate.getVariable())));
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final RelationEntityPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getObjectPropertyAtom(getSourceIri(predicate.getEntity()), getSwrlIri(predicate.getEntityVariable()), getSwrlIri(predicate.getVariable1())));
		atoms.add(owl.getObjectPropertyAtom(getTargetIri(predicate.getEntity()), getSwrlIri(predicate.getEntityVariable()), getSwrlIri(predicate.getVariable2())));
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final RelationPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getObjectPropertyAtom(OmlRead.getIri(predicate.getRelation()), getSwrlIri(predicate.getVariable1()), getSwrlIri(predicate.getVariable2())));
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final SameAsPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getSameIndividualAtom(getSwrlIri(predicate.getVariable1()), getSwrlIri(predicate.getVariable2())));
		return atoms;
	}

	protected List<SWRLAtom> getAtom(final DifferentFromPredicate predicate) {
		final List<SWRLAtom> atoms = new ArrayList<>();
		atoms.add(owl.getDifferentIndividualsAtom(getSwrlIri(predicate.getVariable1()), getSwrlIri(predicate.getVariable2())));
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
		if (literal.getType() != null) {
			return owl.getLiteralWithDatatype(literal.getValue().toString(), OmlRead.getIri(literal.getType()));
		} else if (literal.getLangTag() != null) {
			return owl.getLiteralWithLangTag(literal.getValue().toString(), literal.getLangTag());
		} else {		
			return owl.getLiteral(literal.getValue());
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
		final OWLAnonymousIndividual individual = owl.getAnonymousIndividual(OmlRead.getId(instance));
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
		String namespace = OmlRead.getNamespace(OmlRead.getOntology(entity));
		String name = getForwardName(entity);
		return namespace+name;
	}

	protected String getSourceIri(final RelationEntity entity) {
		if (entity.getSourceRelation() != null) {
			return OmlRead.getIri(entity.getSourceRelation());
		} else {
			String namespace = OmlRead.getNamespace(OmlRead.getOntology(entity));
			String name = toFirstUpper(entity.getName());
			return namespace+"has"+name+"Source";
		}
	}

	protected String getTargetIri(final RelationEntity entity) {
		if (entity.getTargetRelation() != null) {
			return OmlRead.getIri(entity.getTargetRelation());
		} else {
			String namespace = OmlRead.getNamespace(OmlRead.getOntology(entity));
			String name = toFirstUpper(entity.getName());
			return namespace+"has"+name+"Target";
		}
	}

	protected String getInverseSourceIri(final RelationEntity entity) {
		if (entity.getInverseSourceRelation() != null) {
			return OmlRead.getIri(entity.getInverseSourceRelation());
		}
		return null;
	}
		
	protected String getInverseTargetIri(final RelationEntity entity) {
		if (entity.getInverseTargetRelation() != null) {
			return OmlRead.getIri(entity.getInverseTargetRelation());
		}
		return null;
	}

	protected String getSwrlIri(final String variableName) {
		return "urn:swrl#" + variableName;
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
