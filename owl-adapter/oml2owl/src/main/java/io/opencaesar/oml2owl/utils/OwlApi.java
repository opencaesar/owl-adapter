package io.opencaesar.oml2owl.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
	
}