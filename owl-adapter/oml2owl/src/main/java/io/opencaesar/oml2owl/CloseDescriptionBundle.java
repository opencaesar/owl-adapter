package io.opencaesar.oml2owl;

import org.eclipse.emf.ecore.resource.Resource;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.closeworld.OwlApi;

public class CloseDescriptionBundle {

	protected final Resource resource;

	public CloseDescriptionBundle(final Resource resource) {
		this.resource = resource;
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
		}
	}

}
