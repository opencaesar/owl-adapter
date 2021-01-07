package io.opencaesar.oml2owl;

import static io.opencaesar.closeworld.Axiom.AxiomType.DISJOINT_CLASSES;
import static io.opencaesar.closeworld.Axiom.AxiomType.DISJOINT_UNION;
import static io.opencaesar.closeworld.OwlAxiom.toOwlAxiom;
import static io.opencaesar.oml.util.OmlRead.getImportedOntologies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.resource.Resource;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.closeworld.Axiom;
import io.opencaesar.closeworld.ClassExpression;
import io.opencaesar.closeworld.OwlApi;
import io.opencaesar.closeworld.Taxonomy;
import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.Entity;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.SpecializationAxiom;
import io.opencaesar.oml.util.OmlRead;

@SuppressWarnings("all")
public class CloseVocabularyBundle {

	protected final Resource resource;

	public CloseVocabularyBundle(final Resource resource) {
		this.resource = resource;
	}

	private <T> Stream<T> toStream(Iterator<T> i) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED), false);
	}
	
	/**
	 * Returns a transitively-reduced concept taxonomy, rooted at Universal, from a collection of OML ontologies.
	 * 
	 * @param allOntologies
	 * @return concept taxonomy
	 */
	private Taxonomy omlConceptTaxonomy(final Collection<Ontology> allOntologies) {
		final Map<Entity, ClassExpression.Singleton> singletonMap = new HashMap<Entity, ClassExpression.Singleton>();
		final List<ClassExpression>  vertexList = new ArrayList<ClassExpression>();
		final List<ClassExpression>  edgeList = new ArrayList<ClassExpression>();

		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof Entity && !(e instanceof Aspect)).map(e -> (Entity)e).forEach(entity -> {
				final ClassExpression.Singleton s = new ClassExpression.Singleton(OmlRead.getIri((Entity) entity));
				singletonMap.put(entity, s);
				vertexList.add(s);
			});
		});

		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof SpecializationAxiom).map(e -> (SpecializationAxiom)e).forEach(axiom -> {
				final ClassExpression.Singleton specializedSingleton = singletonMap.get(axiom.getSpecializedTerm());
				final ClassExpression.Singleton specializingSingleton = singletonMap.get(OmlRead.getSpecificTerm(axiom));

				if (specializedSingleton != null && specializingSingleton != null) {
					edgeList.add(specializedSingleton);
					edgeList.add(specializingSingleton);
				}
			});
		});
		
		return new Taxonomy(vertexList, edgeList).transitiveReduction().rootAt(new ClassExpression.Universal());
	}

	public static class CloseVocabularyBundleToOwl extends CloseVocabularyBundle {
		protected final OWLOntology ontology;
		protected final boolean disjointUnions;
		protected final OwlApi owlApi;

		public CloseVocabularyBundleToOwl(final Resource resource, final OWLOntology ontology, final boolean disjointUnions, final OwlApi owlApi) {
			super(resource);
			this.ontology = ontology;
			this.disjointUnions = disjointUnions;
			this.owlApi = owlApi;
		}

		public void run() {
			final Ontology omlOntology = OmlRead.getOntology(resource);
			final Collection<Ontology> allOntologies = OmlRead.reflexiveClosure(omlOntology, o -> getImportedOntologies(o));
			final Taxonomy conceptTaxonomy = super.omlConceptTaxonomy(allOntologies);
			final Axiom.AxiomType axiomType = disjointUnions ? DISJOINT_UNION : DISJOINT_CLASSES;

			conceptTaxonomy.generateClosureAxioms(axiomType).forEach(a -> {
				owlApi.addAxiom(ontology, toOwlAxiom(a, owlApi));
			});
		}
	}

	public static class CloseVocabularyBundleToOml extends CloseVocabularyBundle {
		public CloseVocabularyBundleToOml(final Resource resource) {
			super(resource);
		}

		public void run() throws RuntimeException {
			throw new RuntimeException("CloseBundleToOml not implemented");
		}
	}
}
