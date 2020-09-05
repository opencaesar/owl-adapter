package io.opencaesar.oml2owl;

import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.Graph;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.alg.connectivity.BiconnectivityInspector;
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
import io.opencaesar.oml.ForwardRelation;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Property;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.RelationCardinalityRestrictionAxiom;
import io.opencaesar.oml.RelationEntity;
import io.opencaesar.oml.ReverseRelation;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.ScalarPropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.ScalarPropertyValueAssertion;
import io.opencaesar.oml.SpecializableTerm;
import io.opencaesar.oml.StructuredProperty;
import io.opencaesar.oml.StructuredPropertyCardinalityRestrictionAxiom;
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
					} else 
						if (r instanceof StructuredPropertyCardinalityRestrictionAxiom) {
						final StructuredPropertyCardinalityRestrictionAxiom restriction = (StructuredPropertyCardinalityRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case MIN:
						case EXACTLY:
							final StructuredProperty property = restriction.getProperty();
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

	private final static NeighborCache<SpecializableTerm, DefaultEdge> getTermSpecializations(
			final Iterable<Ontology> allOntologies) {
		final DirectedAcyclicGraph<SpecializableTerm, DefaultEdge> taxonomy = new DirectedAcyclicGraph<SpecializableTerm, DefaultEdge>(
				DefaultEdge.class);

		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof SpecializableTerm).map(e -> (SpecializableTerm) e)
					.forEach(term -> {
						taxonomy.addVertex(term);
						OmlRead.getSpecializedTerms(term).forEach(specialized -> {
							taxonomy.addVertex(specialized);
							taxonomy.addEdge(specialized, term);
						});
					});
		});

		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(taxonomy);
		return new NeighborCache<SpecializableTerm, DefaultEdge>(taxonomy);
	}

	private final static HashMap<Property, Graph<Property, DefaultEdge>> getPropertyTrees(
			final Iterable<Ontology> allOntologies) throws UnsupportedOperationException {
		final HashMap<Property, Graph<Property, DefaultEdge>> map = new HashMap<>();
		final DirectedAcyclicGraph<Property, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof Property).map(e -> (Property) e).forEach(p -> {
				graph.addVertex(p);
				OmlRead.getSpecializedTerms(p).forEach(s -> {
					final Property sp = (Property) s;
					graph.addVertex(sp);
					graph.addEdge(sp, p);
				});
			});
		});

		final Set<Graph<Property, DefaultEdge>> components = new BiconnectivityInspector<>(graph)
				.getConnectedComponents();
		components.forEach(component -> {
			final AsSubgraph<Property, DefaultEdge> subgraph = new AsSubgraph<>(graph, component.vertexSet());
			final List<Property> roots = subgraph.vertexSet().stream().filter(v -> subgraph.inDegreeOf(v) == 0)
					.collect(Collectors.toList());
			if (roots.size() > 1)
				throw new UnsupportedOperationException("multiply-rooted relation tree");
			map.put((Property) roots.get(0), subgraph);
		});

		return map;
	}
	
	private final static HashMap<Relation, Graph<Relation, DefaultEdge>> getRelationTrees(
			final Iterable<Ontology> allOntologies) throws UnsupportedOperationException {
		final HashMap<Relation, Graph<Relation, DefaultEdge>> map = new HashMap<>();
		final DirectedAcyclicGraph<Relation, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		toStream(allOntologies.iterator()).forEach(g -> {
			toStream(g.eAllContents()).filter(e -> e instanceof RelationEntity).map(e -> (RelationEntity) e)
					.forEach(re -> {
						final Relation f = re.getForward();
						final Relation r = re.getReverse();
						graph.addVertex(f);
						if (Objects.nonNull(r))
							graph.addVertex(r);
						OmlRead.getSpecializedTerms(re).forEach(s -> {
							if (s instanceof RelationEntity) {
								final RelationEntity sre = (RelationEntity) s;
								final Relation sf = sre.getForward();
								final Relation sr = sre.getReverse();
								graph.addVertex(sf);
								graph.addEdge(sf, f);
								if (Objects.nonNull(r) && Objects.nonNull(sr)) {
									graph.addVertex(sr);
									graph.addEdge(sr, r);
								}
							}
						});
					});
		});

		final Set<Graph<Relation, DefaultEdge>> components = new BiconnectivityInspector<>(graph)
				.getConnectedComponents();
		components.forEach(component -> {
			final AsSubgraph<Relation, DefaultEdge> subgraph = new AsSubgraph<>(graph, component.vertexSet());
			final List<Relation> roots = subgraph.vertexSet().stream().filter(v -> subgraph.inDegreeOf(v) == 0)
					.collect(Collectors.toList());
			if (roots.size() > 1)
				throw new UnsupportedOperationException("multiply-rooted relation tree");
			map.put((Relation) roots.get(0), subgraph);
		});

		return map;
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
	private static HashMap<NamedInstance, HashMap<Property, Integer>> dataPropertyCounts(
			final HashMap<Entity, HashSet<Property>> entitiesWithRestrictedProperties,
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances,
			final NeighborCache<SpecializableTerm, DefaultEdge> neighborCache,
			final HashMap<Property, Graph<Property, DefaultEdge>> propertyTrees) {
		final HashMap<NamedInstance, HashMap<Property, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedProperties.forEach((entity, properties) -> {
			final HashSet<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final HashMap<Property, Integer> subj_map = map.getOrDefault(subj, new HashMap<>());
				map.put(subj, subj_map);
				final HashSet<Property> all_properties = new HashSet<>();
				properties.forEach(property -> {
					all_properties.add(property);
					neighborCache.successorsOf(property).forEach(subproperty -> {
						all_properties.add((Property) subproperty);
					});
				});
				all_properties.forEach(property -> {
					if (!subj_map.containsKey(property)) subj_map.put(property, 0);
				});
				subj.getOwnedPropertyValues().forEach(pva -> {
					if (pva instanceof ScalarPropertyValueAssertion) {
						final ScalarPropertyValueAssertion spva = (ScalarPropertyValueAssertion) pva;
						final ScalarProperty prop = spva.getProperty();
						if (all_properties.contains(prop)) {
							subj_map.put(prop, subj_map.get(prop) + 1);
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
	private static HashMap<NamedInstance, HashMap<String, Integer>> objectPropertyCounts(
			final HashMap<Entity, HashSet<Relation>> entitiesWithRestrictedRelations,
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances,
			final NeighborCache<SpecializableTerm, DefaultEdge> neighborCache,
			final HashMap<Relation, Graph<Relation, DefaultEdge>> relationTrees) {
		final HashMap<NamedInstance, HashMap<String, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedRelations.forEach((entity, relations) -> {
			final HashSet<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final HashMap<String, Integer> subj_map = map.getOrDefault(subj, new HashMap<String, Integer>());
				map.put(subj, subj_map);
				final HashSet<Relation> all_relations = new HashSet<>();
				relations.forEach(relation -> {
					all_relations.add(relation);
					neighborCache.successorsOf(OmlRead.getEntity(relation)).forEach(st -> {
						final RelationEntity re = (RelationEntity) st;
						if (relation instanceof ForwardRelation)
							all_relations.add(re.getForward());
						else if (relation instanceof ReverseRelation)
							all_relations.add(re.getReverse());
					});
				});
				all_relations.forEach(relation -> {
					final String relation_iri = OmlRead.getIri(relation);
					if (!subj_map.containsKey(relation_iri)) subj_map.put(relation_iri, 0);
				});
				subj.getOwnedLinks().forEach(link -> {
					final Relation rel = link.getRelation();
					if (all_relations.contains(rel)) {
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
			final NeighborCache<SpecializableTerm, DefaultEdge> termSpecializations = getTermSpecializations(allOntologies);
			final HashMap<Property, Graph<Property, DefaultEdge>> propertyTrees = getPropertyTrees(allOntologies);
			final HashMap<Relation, Graph<Relation, DefaultEdge>> relationTrees = getRelationTrees(allOntologies);

			final HashSet<Entity> allRestrictedEntities = new HashSet<Entity>(entitiesWithRestrictedProperties.keySet());
			allRestrictedEntities.addAll(entitiesWithRestrictedRelations.keySet());
			
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances = getEntityInstances(allOntologies, allRestrictedEntities, termSpecializations);
			final HashMap<NamedInstance, HashMap<Property, Integer>> dataPropertyCounts = dataPropertyCounts(entitiesWithRestrictedProperties, entityInstances, termSpecializations, propertyTrees);
			final HashMap<NamedInstance, HashMap<String, Integer>> objectPropertyCounts = objectPropertyCounts(entitiesWithRestrictedRelations, entityInstances, termSpecializations, relationTrees);
			
			/*
			 * Generate data property cardinality restrictions.
			 */
			dataPropertyCounts.forEach((subj, map) -> {
				final IRI subj_iri = IRI.create(OmlRead.getIri(subj));
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(subj_iri);
				map.forEach((prop, c) -> {
					final IRI prop_iri = IRI.create(OmlRead.getIri(prop));
					final OWLDataProperty dp = this.owlApi.getOWLDataProperty(prop_iri);
					final OWLDataMaxCardinality mc = this.owlApi.getOWLDataMaxCardinality(c, dp);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});
			
			/*
			 * Generate object property cardinality restrictions.
			 */
			objectPropertyCounts.forEach((subj, map) -> {
				final IRI subj_iri = IRI.create(OmlRead.getIri(subj));
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(subj_iri);
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
