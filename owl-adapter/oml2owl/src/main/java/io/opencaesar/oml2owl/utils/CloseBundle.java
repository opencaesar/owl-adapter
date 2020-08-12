package io.opencaesar.oml2owl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.resource.Resource;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.Entity;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.SpecializationAxiom;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml2owl.utils.ClassExpression.Singleton;
import io.opencaesar.oml2owl.utils.ClassExpression.Universal;

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

	public Map<ClassExpression, Set<ClassExpression>> getSiblingMap() {
		final Ontology ontology = OmlRead.getOntology(resource);
		final Iterable<Ontology> allOntologies = OmlRead.reflexiveClosure(ontology, o -> OmlRead.getImportedOntologies(o));
				
		final Taxonomy taxonomy = omlTaxonomy(allOntologies);
		taxonomy.ensureConnected();
		
		final Taxonomy tree = taxonomy.treeify();
		tree.ensureTree();
				
		return tree.siblingMap();		
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
			getSiblingMap().forEach((ce, v) -> { 
   				final Stream<OWLClassExpression> subclasses = v.stream().map(e -> OwlClassExpression.toOwlClassExpression(e, owlApi));
	   			if (disjointUnions && (ce instanceof Singleton || ce instanceof Universal)) {
	   				final OWLClass parent = (OWLClass) OwlClassExpression.toOwlClassExpression(ce, owlApi);
	   				owlApi.addDisjointUnion(ontology, parent, subclasses.collect(Collectors.toSet()));
	     		} else {
	     			owlApi.addDisjointClasses(ontology, subclasses.collect(Collectors.toSet()));
				}
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
