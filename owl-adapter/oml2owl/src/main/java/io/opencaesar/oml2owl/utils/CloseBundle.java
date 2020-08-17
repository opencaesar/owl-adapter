package io.opencaesar.oml2owl.utils;

import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.Entity;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.SpecializationAxiom;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml2owl.utils.ClassExpression.Singleton;
import io.opencaesar.oml2owl.utils.ClassExpression.Universal;
import org.eclipse.emf.ecore.resource.Resource;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.opencaesar.oml.util.OmlRead.getImportedOntologies;
import static io.opencaesar.oml2owl.utils.Axiom.AxiomType.DISJOINT_CLASSES;
import static io.opencaesar.oml2owl.utils.Axiom.AxiomType.DISJOINT_UNION;
import static io.opencaesar.oml2owl.utils.OwlAxiom.toOwlAxiom;

@SuppressWarnings("all")
public class CloseBundle {

	protected final Resource resource;

	public CloseBundle(final Resource resource) {
		this.resource = resource;
	}

	private <T> Stream<T> toStream(Iterator<T> i) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED), false);
	}
	
	private Taxonomy omlTaxonomy(final Iterable<Ontology> allOntologies) {
		final Map<Entity, Singleton> singletonMap = new HashMap<Entity, Singleton>();
		final List<ClassExpression>  vertexList = new ArrayList<ClassExpression>();
		final List<ClassExpression>  edgeList = new ArrayList<ClassExpression>();

		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof Entity && !(e instanceof Aspect)).map(e -> (Entity)e).forEach(entity -> {
				final Singleton s = new Singleton(entity);
				singletonMap.put(entity, s);
				vertexList.add(s);
			});
		});

		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof SpecializationAxiom).map(e -> (SpecializationAxiom)e).forEach(axiom -> {
				final Singleton specializedSingleton = singletonMap.get(axiom.getSpecializedTerm());
				final Singleton specializingSingleton = singletonMap.get(OmlRead.getSpecializingTerm(axiom));

				if (specializedSingleton != null && specializingSingleton != null) {
					edgeList.add(specializedSingleton);
					edgeList.add(specializingSingleton);
				}
			});
		});
		
		return new Taxonomy(vertexList, edgeList).transitiveReduction().rootAt(new Universal());
	}

	public static class CloseBundleToOwl extends CloseBundle {
		protected final OWLOntology ontology;
		protected final boolean disjointUnions;
		protected final OwlApi owlApi;

		public CloseBundleToOwl(final Resource resource, final OWLOntology ontology, final boolean disjointUnions, final OwlApi owlApi) {
			super(resource);
			this.ontology = ontology;
			this.disjointUnions = disjointUnions;
			this.owlApi = owlApi;
		}

		public void run() {
			final Ontology omlOntology = OmlRead.getOntology(resource);
			final Iterable<Ontology> allOntologies = OmlRead.reflexiveClosure(omlOntology, o -> getImportedOntologies(o));
			final Taxonomy taxonomy = super.omlTaxonomy(allOntologies);
			final Axiom.AxiomType axiomType = disjointUnions ? DISJOINT_UNION : DISJOINT_CLASSES;

			taxonomy.generateClosureAxioms(axiomType).forEach(a -> {
				owlApi.addAxiom(ontology, toOwlAxiom(a, owlApi));
			});
		}
	}

	public static class CloseBundleToOml extends CloseBundle {
		public CloseBundleToOml(final Resource resource) {
			super(resource);
		}

		public void run() throws RuntimeException {
			throw new RuntimeException("CloseBundleToOml not implemented");
		}
	}
}
