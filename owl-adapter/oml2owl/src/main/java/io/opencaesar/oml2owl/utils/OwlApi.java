package io.opencaesar.oml2owl.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

public class OwlApi {
	
	protected final OWLOntologyManager manager;
	protected final OWLDataFactory factory;
	
	public OwlApi(OWLOntologyManager manager) {
		this.manager = manager;
		this.factory = manager.getOWLDataFactory();
	}
	
	public OWLDisjointClassesAxiom addDisjointClasses(OWLOntology ontology, Collection<OWLClassExpression> classes, OWLAnnotation...annotations) {
		OWLDisjointClassesAxiom axiom = factory.getOWLDisjointClassesAxiom(classes, Arrays.asList(annotations));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}
	
	public OWLDisjointUnionAxiom addDisjointUnion(OWLOntology ontology, OWLClass owlClass, Collection<OWLClassExpression> subClasses, OWLAnnotation...annotations) {
		OWLDisjointUnionAxiom axiom = factory.getOWLDisjointUnionAxiom(owlClass, subClasses, Arrays.asList((annotations)));
		manager.addAxiom(ontology, axiom);
		return axiom;
	}

	public OWLClass getOWLThing() {
		return factory.getOWLThing();
	}

	public OWLClass getOWLNothing() {
		return factory.getOWLNothing();
	}
	
	public OWLClass getOWLClass(IRI iri) {
		return factory.getOWLClass(iri);
	}
	
	public OWLObjectComplementOf getOWLObjectComplementOf(OWLClassExpression e) {
		return factory.getOWLObjectComplementOf(e);
	}
	
	public OWLObjectIntersectionOf getOWLObjectIntersectionOf(Stream<OWLClassExpression> operands) {
		return factory.getOWLObjectIntersectionOf(operands);
	}
	
	public OWLObjectUnionOf getOWLObjectUnionOf(Stream<OWLClassExpression> operands) {
		return factory.getOWLObjectUnionOf(operands);
	}

	public OWLDisjointClassesAxiom getOWLDisjointClassesAxiom (Stream<OWLClassExpression> operands) {
		return factory.getOWLDisjointClassesAxiom(operands);
	}

	public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom (Stream<OWLClassExpression> operands) {
		return factory.getOWLEquivalentClassesAxiom(operands);
	}

	public OWLDisjointUnionAxiom getOWLDisjointUnionAxiom (OWLClass c, Stream<OWLClassExpression> operands) {
		return factory.getOWLDisjointUnionAxiom(c, operands);
	}

}