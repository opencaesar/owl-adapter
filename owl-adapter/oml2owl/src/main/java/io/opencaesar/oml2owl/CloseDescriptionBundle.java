package io.opencaesar.oml2owl;

import static io.opencaesar.oml.util.OmlRead.getImportedOntologies;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.resource.Resource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.closeworld.OwlApi;
import io.opencaesar.closeworld.Taxonomy;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.util.OmlRead;

public class CloseDescriptionBundle {

	protected final Resource resource;

	public CloseDescriptionBundle(final Resource resource) {
		this.resource = resource;
	}

	private <T> Stream<T> toStream(Iterator<T> i) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED), false);
	}
	
	private static HashMap<IRI, HashMap<IRI, Integer>> dataPropertyCounts(final Iterable<Ontology> allOntologies) {
		final HashMap<IRI, HashMap<IRI, Integer>> map = new HashMap<>();
		return map;
	}
	
	private static HashMap<IRI, HashMap<IRI, Integer>> objectPropertyCounts(final Iterable<Ontology> allOntologies) {
		final HashMap<IRI, HashMap<IRI, Integer>> map = new HashMap<>();
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

		public void run() {
			final Ontology omlOntology = OmlRead.getOntology(this.resource);
			final Iterable<Ontology> allOntologies = OmlRead.reflexiveClosure(omlOntology, o -> getImportedOntologies(o));
			final HashMap<IRI, HashMap<IRI, Integer>> dataPropertyCounts = dataPropertyCounts(allOntologies);
			final HashMap<IRI, HashMap<IRI, Integer>> objectPropertyCounts = objectPropertyCounts(allOntologies);
		}
	}

}
