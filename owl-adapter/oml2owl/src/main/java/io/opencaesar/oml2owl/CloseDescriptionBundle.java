package io.opencaesar.oml2owl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.resource.Resource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.closeworld.OwlApi;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.ScalarPropertyValueAssertion;
import io.opencaesar.oml.StructuredProperty;
import io.opencaesar.oml.StructuredPropertyValueAssertion;
import io.opencaesar.oml.util.OmlRead;

public class CloseDescriptionBundle {

	protected final Resource resource;

	public CloseDescriptionBundle(final Resource resource) {
		this.resource = resource;
	}

	private static <T> Stream<T> toStream(Iterator<T> i) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED), false);
	}
	
	@SuppressWarnings("boxing")
	private static HashMap<String, HashMap<String, Integer>> dataPropertyCounts(final Iterable<Ontology> allOntologies) {
		final HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof NamedInstance).forEach(e -> {
				final NamedInstance subj = (NamedInstance) e;
				final String subj_iri = OmlRead.getIri(subj);
				final HashMap<String, Integer> subj_map = map.getOrDefault(subj_iri, new HashMap<String, Integer>());
				map.put(subj_iri, subj_map);
				subj.getOwnedPropertyValues().forEach(pva -> {
					if (pva instanceof ScalarPropertyValueAssertion) {
						final ScalarPropertyValueAssertion spva = (ScalarPropertyValueAssertion) pva;
						final ScalarProperty prop = spva.getProperty();
						final String prop_iri = OmlRead.getIri(prop);
						final Integer count = subj_map.getOrDefault(prop_iri, 0);
						subj_map.put(prop_iri, count + 1);
					} else if (pva instanceof StructuredPropertyValueAssertion) {
						final StructuredPropertyValueAssertion spva = (StructuredPropertyValueAssertion) pva;
						final StructuredProperty prop = spva.getProperty();
						final String prop_iri = OmlRead.getIri(prop);
						final Integer count = subj_map.getOrDefault(prop_iri, 0);
						subj_map.put(prop_iri, count + 1);
					}
				});
			});
		});
		
		return map;
	}
	
	@SuppressWarnings("boxing")
	private static HashMap<String, HashMap<String, Integer>> objectPropertyCounts(final Iterable<Ontology> allOntologies) {
		final HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof NamedInstance).forEach(e -> {
				final NamedInstance subj = (NamedInstance) e;
				final String subj_iri = OmlRead.getIri(subj);
				final HashMap<String, Integer> subj_map = map.getOrDefault(subj_iri, new HashMap<String, Integer>());
				map.put(subj_iri,  subj_map);
				subj.getOwnedLinks().forEach(la -> {
					final Relation rel = la.getRelation();
					final String rel_iri = OmlRead.getIri(rel);
					final Integer count = subj_map.getOrDefault(rel_iri, 0);
					subj_map.put(rel_iri, count + 1);
				});
			});
		});

		return map;
	}
	
	public static class CloseDescriptionBundleToOwl extends CloseDescriptionBundle {
		protected final OWLOntology ontology;
		protected final OwlApi owlApi;

		public CloseDescriptionBundleToOwl(final Resource resource, final OWLOntology ontology, final OwlApi owlApi) {
			super(resource);
			this.ontology = ontology;
			this.owlApi = owlApi;
		}

		@SuppressWarnings("boxing")
		public void run() {
			final Ontology omlOntology = OmlRead.getOntology(this.resource);
			final Iterable<Ontology> allOntologies = OmlRead.reflexiveClosure(omlOntology, o -> OmlRead.getImportedOntologies(o));
			final HashMap<String, HashMap<String, Integer>> dataPropertyCounts = dataPropertyCounts(allOntologies);
			final HashMap<String, HashMap<String, Integer>> objectPropertyCounts = objectPropertyCounts(allOntologies);
			
			dataPropertyCounts.forEach((subj, map) -> {
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(IRI.create(subj));
				map.forEach((prop, c) -> {
					final OWLDataProperty dp = this.owlApi.getOWLDataProperty(IRI.create(prop));
					final OWLDataMaxCardinality mc = this.owlApi.getOWLDataMaxCardinality(c, dp);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});
			objectPropertyCounts.forEach((subj, map) -> {
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(IRI.create(subj));
				map.forEach((prop, c) -> {
					final OWLObjectProperty dp = this.owlApi.getOWLObjectProperty(IRI.create(prop));
					final OWLObjectMaxCardinality mc = this.owlApi.getOWLObjectMaxCardinality(c, dp);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});
		}
	}

}
