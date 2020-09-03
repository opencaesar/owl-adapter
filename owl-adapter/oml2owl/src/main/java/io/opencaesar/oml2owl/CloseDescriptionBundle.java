package io.opencaesar.oml2owl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.closeworld.OwlApi;
import io.opencaesar.oml.Concept;
import io.opencaesar.oml.Entity;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Property;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.RelationCardinalityRestrictionAxiom;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.ScalarPropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.ScalarPropertyValueAssertion;
import io.opencaesar.oml.SpecializableTerm;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlSearch;

/**
 * @author sjenkins
 *
 */
public class CloseDescriptionBundle {

	protected final Resource resource;

	public CloseDescriptionBundle(final Resource resource) {
		this.resource = resource;
	}

	private static <T> Stream<T> toStream(Iterator<T> i) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(i, Spliterator.ORDERED), false);
	}
	
	private final static HashMap<Entity, HashSet<Property>> getEntitiesWithRestrictedProperties(final Iterable<Ontology> allOntologies) {
		final HashMap<Entity, HashSet<Property>> map = new HashMap<>();
		
		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof Entity).forEach(e -> {
				final Entity entity = (Entity) e;
				entity.getOwnedPropertyRestrictions().forEach(r -> {
					if (r instanceof ScalarPropertyCardinalityRestrictionAxiom) {
						final ScalarPropertyCardinalityRestrictionAxiom restriction = (ScalarPropertyCardinalityRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case MIN:
						case EXACTLY:
							final ScalarProperty property = restriction.getProperty();
							final HashSet<Property> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							set.add(property);
							break;
						default:
						}
					}
				});
			});
		});
		return map;
	}
	
	private final static HashMap<Entity, HashSet<Relation>> getEntitiesWithRestrictedRelations(final Iterable<Ontology> allOntologies) {
		final HashMap<Entity, HashSet<Relation>> map = new HashMap<>();
		
		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof Entity).forEach(e -> {
				final Entity entity = (Entity) e;
				entity.getOwnedRelationRestrictions().forEach(r -> {
					if (r instanceof RelationCardinalityRestrictionAxiom) {
						final RelationCardinalityRestrictionAxiom restriction = (RelationCardinalityRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case MIN:
						case EXACTLY:
							final Relation relation = restriction.getRelation();
							final HashSet<Relation> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							set.add(relation);
							break;
						default:
						}
					}
				});
			});
		});
		return map;
	}

	private final static NeighborCache<SpecializableTerm, DefaultEdge> getSpecializations(final Iterable<Ontology> allOntologies) {
		final DirectedAcyclicGraph<SpecializableTerm, DefaultEdge> taxonomy = new DirectedAcyclicGraph<SpecializableTerm, DefaultEdge>(DefaultEdge.class);
		
		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof Entity).map(e -> (Entity) e).forEach(entity -> {
				taxonomy.addVertex(entity);
				OmlRead.getSpecializedTerms(entity).forEach(specialized -> {
					taxonomy.addVertex(specialized);
					taxonomy.addEdge(specialized, entity);
				});
			});
		});

		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(taxonomy);
		return new NeighborCache<SpecializableTerm, DefaultEdge>(taxonomy);
	}

	private final static HashMap<Entity, HashSet<NamedInstance>> getEntityInstances(
			final Iterable<Ontology> allOntologies, final HashSet<Entity> entities,
			final NeighborCache<SpecializableTerm, DefaultEdge> neighborCache) {
		final HashMap<Entity, HashSet<NamedInstance>> map = new HashMap<>();

		entities.forEach(entity -> {
			final HashSet<SpecializableTerm> specializations = new HashSet<>(neighborCache.successorsOf(entity));
			specializations.add(entity);
			final HashSet<NamedInstance> instances = new HashSet<>();
			specializations.stream().filter(s -> s instanceof Concept).map(s -> (Concept) s).forEach(concept -> {
				OmlSearch.findConceptInstancesWithType(concept).forEach(instance -> {
					instances.add(instance);
				});
			});
			map.put(entity, instances);
		});
		return map;
	}
	
	/**
	 * 
	 * Returns a map from subject IRI to a map from predicate IRI to usage count
	 * for generating cardinality restrictions on data properties.
	 * 
	 * @param allOntologies
	 * @return map from subject IRI to map from predicate IRI to usage count
	 */
	private static HashMap<String, HashMap<String, Integer>> dataPropertyCounts(final HashMap<Entity, HashSet<Property>> entitiesWithRestrictedProperties, final HashMap<Entity, HashSet<NamedInstance>> entityInstances) {
		final HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedProperties.forEach((entity, properties) -> {
			final HashSet<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final String subj_iri = OmlRead.getIri(subj);
				final HashMap<String, Integer> subj_map = map.getOrDefault(subj_iri, new HashMap<String, Integer>());
				map.put(subj_iri, subj_map);
				properties.forEach(property -> {
					final String property_iri = OmlRead.getIri(property);
					if (!subj_map.containsKey(property_iri)) subj_map.put(property_iri, 0);
				});
				subj.getOwnedPropertyValues().forEach(pva -> {
					if (pva instanceof ScalarPropertyValueAssertion) {
						final ScalarPropertyValueAssertion spva = (ScalarPropertyValueAssertion) pva;
						final ScalarProperty prop = spva.getProperty();
						if (properties.contains(prop)) {
							final String prop_iri = OmlRead.getIri(prop);
							subj_map.put(prop_iri, subj_map.get(prop_iri) + 1);
						}
					}
				});
			});			
		});
		
		return map;
	}
	
	/**
	 * 
	 * Returns a map from subject IRI to a map from predicate IRI to usage count
	 * for generating cardinality restrictions on object properties.
	 * 
	 * @param allOntologies
	 * @return map from subject IRI to map from predicate IRI to usage count
	 */
	private static HashMap<String, HashMap<String, Integer>> objectPropertyCounts(final HashMap<Entity, HashSet<Relation>> entitiesWithRestrictedRelations, final HashMap<Entity, HashSet<NamedInstance>> entityInstances) {
		final HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedRelations.forEach((entity, relations) -> {
			final HashSet<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final String subj_iri = OmlRead.getIri(subj);
				final HashMap<String, Integer> subj_map = map.getOrDefault(subj_iri, new HashMap<String, Integer>());
				map.put(subj_iri, subj_map);
				relations.forEach(relation -> {
					final String relation_iri = OmlRead.getIri(relation);
					if (!subj_map.containsKey(relation_iri)) subj_map.put(relation_iri, 0);
				});
				subj.getOwnedLinks().forEach(link -> {
					final Relation rel = link.getRelation();
					if (relations.contains(rel)) {
						final String rel_iri = OmlRead.getIri(rel);
						subj_map.put(rel_iri, subj_map.get(rel_iri) + 1);
					}

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

		public void run() {
			final Ontology omlOntology = OmlRead.getOntology(this.resource);
			final Iterable<Ontology> allOntologies = OmlRead.reflexiveClosure(omlOntology, o -> OmlRead.getImportedOntologies(o));

			final HashMap<Entity, HashSet<Property>> entitiesWithRestrictedProperties = getEntitiesWithRestrictedProperties(allOntologies);
			final HashMap<Entity, HashSet<Relation>> entitiesWithRestrictedRelations = getEntitiesWithRestrictedRelations(allOntologies);
			final NeighborCache<SpecializableTerm, DefaultEdge> specializations = getSpecializations(allOntologies);

			final HashSet<Entity> allRestrictedEntities = new HashSet<Entity>(entitiesWithRestrictedProperties.keySet());
			allRestrictedEntities.addAll(entitiesWithRestrictedRelations.keySet());
			
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances = getEntityInstances(allOntologies, allRestrictedEntities, specializations);
			final HashMap<String, HashMap<String, Integer>> dataPropertyCounts = dataPropertyCounts(entitiesWithRestrictedProperties, entityInstances);
			final HashMap<String, HashMap<String, Integer>> objectPropertyCounts = objectPropertyCounts(entitiesWithRestrictedRelations, entityInstances);
			
			/*
			 * Generate data property cardinality restrictions.
			 */
			dataPropertyCounts.forEach((subj, map) -> {
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(IRI.create(subj));
				map.forEach((prop, c) -> {
					final OWLDataProperty dp = this.owlApi.getOWLDataProperty(IRI.create(prop));
					final OWLDataMaxCardinality mc = this.owlApi.getOWLDataMaxCardinality(c, dp);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});
			
			/*
			 * Generate object property cardinality restrictions.
			 */
			objectPropertyCounts.forEach((subj, map) -> {
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(IRI.create(subj));
				map.forEach((prop, c) -> {
					final OWLObjectProperty op = this.owlApi.getOWLObjectProperty(IRI.create(prop));
					final OWLObjectMaxCardinality mc = this.owlApi.getOWLObjectMaxCardinality(c, op);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});

		}
	}

}
