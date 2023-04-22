/**
 * 
 * Copyright 2019-2021 California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package io.opencaesar.oml2owl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.oml.Concept;
import io.opencaesar.oml.ConceptInstance;
import io.opencaesar.oml.Entity;
import io.opencaesar.oml.Feature;
import io.opencaesar.oml.FeaturePredicate;
import io.opencaesar.oml.ForwardRelation;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Property;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.RelationCardinalityRestrictionAxiom;
import io.opencaesar.oml.RelationEntity;
import io.opencaesar.oml.RelationEntityPredicate;
import io.opencaesar.oml.RelationRangeRestrictionAxiom;
import io.opencaesar.oml.RelationTargetRestrictionAxiom;
import io.opencaesar.oml.ReverseRelation;
import io.opencaesar.oml.Rule;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.ScalarPropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.ScalarPropertyRangeRestrictionAxiom;
import io.opencaesar.oml.ScalarPropertyValueAssertion;
import io.opencaesar.oml.ScalarPropertyValueRestrictionAxiom;
import io.opencaesar.oml.SpecializableTerm;
import io.opencaesar.oml.StructureInstance;
import io.opencaesar.oml.StructuredProperty;
import io.opencaesar.oml.StructuredPropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.StructuredPropertyRangeRestrictionAxiom;
import io.opencaesar.oml.StructuredPropertyValueAssertion;
import io.opencaesar.oml.StructuredPropertyValueRestrictionAxiom;
import io.opencaesar.oml.Type;
import io.opencaesar.oml.TypePredicate;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlSearch;

/**
 * An algorithm to close the world on a description bundle by adding cardinality restrictions
 * as extra classes to individuals
 */
public class CloseDescriptionBundle {

	/**
	 * The description bundle resource
	 */
	protected final Resource resource;

	/**
	 * Creates a new CloseDescriptionBundle object
	 * 
	 * @param resource The OML resource of the description bundle
	 */
	public CloseDescriptionBundle(final Resource resource) {
		this.resource = resource;
	}

	/**
	 * Gets all entities with restrictions on properties that require closure axioms. Such axioms are minimum and
	 * exact cardinality restrictions and some-values-from range restrictions.
	 * 
	 * @param allOntologies
	 * @return map from entity to set of restricted properties
	 */
	private final static HashMap<Entity, HashSet<ScalarProperty>> getEntitiesWithRestrictedScalarProperties(final Collection<Ontology> allOntologies) {
		final HashMap<Entity, HashSet<ScalarProperty>> map = new HashMap<>();
		
		allOntologies.stream()
			.filter(o -> o instanceof Vocabulary)
			.flatMap(o -> OmlRead.getStatements(o).stream())
			.filter(s -> s instanceof Entity)
			.map(s -> (Entity)s)
			.forEach(entity -> {
				OmlSearch.findPropertyRestrictions(entity).forEach(r -> {
					if (r instanceof ScalarPropertyCardinalityRestrictionAxiom) {
						final ScalarPropertyCardinalityRestrictionAxiom restriction = (ScalarPropertyCardinalityRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case MIN:
						case EXACTLY:
							final ScalarProperty property = restriction.getProperty();
							final HashSet<ScalarProperty> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							set.add(property);
							break;
						default:
						}
					} else if (r instanceof ScalarPropertyRangeRestrictionAxiom) {
						final ScalarPropertyRangeRestrictionAxiom restriction = (ScalarPropertyRangeRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case SOME:
							final ScalarProperty property = restriction.getProperty();
							final HashSet<ScalarProperty> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							set.add(property);
							break;
						default:
						}
					} else if (r instanceof ScalarPropertyValueRestrictionAxiom) {
						final ScalarPropertyValueRestrictionAxiom restriction = (ScalarPropertyValueRestrictionAxiom) r;
						final ScalarProperty property = restriction.getProperty();
						final HashSet<ScalarProperty> set = map.getOrDefault(entity, new HashSet<>());
						map.put(entity, set);
						set.add(property);
					}
				});
			});
		return map;
	}
	
	/**
	 * Gets all entities with restrictions on properties that require closure axioms. Such axioms are minimum and
	 * exact cardinality restrictions and some-values-from range restrictions.
	 * 
	 * @param allOntologies
	 * @return map from entity to set of restricted properties
	 */
	private final static HashMap<Entity, HashSet<StructuredProperty>> getEntitiesWithRestrictedStructuredProperties(final Collection<Ontology> allOntologies) {
		final HashMap<Entity, HashSet<StructuredProperty>> map = new HashMap<>();
		
		allOntologies.stream()
			.filter(o -> o instanceof Vocabulary)
			.flatMap(o -> OmlRead.getStatements(o).stream())
			.filter(s -> s instanceof Entity)
			.map(s -> (Entity)s)
			.forEach(entity -> {
				OmlSearch.findPropertyRestrictions(entity).forEach(r -> {
					if (r instanceof StructuredPropertyCardinalityRestrictionAxiom) {
						final StructuredPropertyCardinalityRestrictionAxiom restriction = (StructuredPropertyCardinalityRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case MIN:
						case EXACTLY:
							final StructuredProperty property = restriction.getProperty();
							final HashSet<StructuredProperty> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							set.add(property);
							break;
						default:
						}
					} else if (r instanceof StructuredPropertyRangeRestrictionAxiom) {
						final StructuredPropertyRangeRestrictionAxiom restriction = (StructuredPropertyRangeRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case SOME:
							final StructuredProperty property = restriction.getProperty();
							final HashSet<StructuredProperty> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							set.add(property);
							break;
						default:
						}
					} else if (r instanceof StructuredPropertyValueRestrictionAxiom) {
						final StructuredPropertyValueRestrictionAxiom restriction = (StructuredPropertyValueRestrictionAxiom) r;
						final StructuredProperty property = restriction.getProperty();
						final HashSet<StructuredProperty> set = map.getOrDefault(entity, new HashSet<>());
						map.put(entity, set);
						set.add(property);
					}
				});
			});
		return map;
	}

	private static void addDerivedRelationViaRule(final HashMap<Relation, HashSet<Rule>> derivedRelations, final Relation r, final Rule rule) {
		final HashSet<Rule> rs = derivedRelations.getOrDefault(r, new HashSet<>());
		rs.add(rule);
		derivedRelations.put(r, rs);
	}

	/**
	 * Gets all entities with restrictions on non-derived relations that require closure axioms. Such axioms are minimum and
	 * exact cardinality restrictions and some-values-from range restrictions.
	 *
	 * Derived relations are those that appear in a consequent of a SWRL rule.
	 * 
	 * @param allOntologies
	 * @return map from entity to set of restricted relations
	 */
	private final static HashMap<Entity, HashSet<Relation>> getEntitiesWithRestrictedRelations(final Collection<Ontology> allOntologies) {

		/**
		 * This map indicates whether a relation is derived via SWRL rules.
		 */
		final HashMap<Relation, HashSet<Rule>> derivedRelations = new HashMap<>();
		
		allOntologies.stream()
			.filter(o -> o instanceof Vocabulary)
			.flatMap(o -> OmlRead.getStatements(o).stream())
			.filter(s -> s instanceof Rule)
			.map(s -> (Rule)s)
			.forEach(rule -> {
				rule.getConsequent().forEach(p -> {
					if (p instanceof TypePredicate) {
						final Type t = ((TypePredicate) p).getType();
						if (t instanceof RelationEntity) {
							final RelationEntity re = (RelationEntity) t;
							final ForwardRelation fr = re.getForwardRelation();
							if (null != fr)
								addDerivedRelationViaRule(derivedRelations, fr, rule);
							final ReverseRelation rr = re.getReverseRelation();
							if (null != rr)
								addDerivedRelationViaRule(derivedRelations, rr, rule);
						}
					} else if (p instanceof FeaturePredicate) {
						final Feature f = ((FeaturePredicate) p).getFeature();
						if (f instanceof Relation)
							addDerivedRelationViaRule(derivedRelations, (Relation)f, rule);
					} else if (p instanceof RelationEntityPredicate) {
						final RelationEntity re = ((RelationEntityPredicate) p).getEntity();
						final ForwardRelation fr = re.getForwardRelation();
						if (null != fr)
							addDerivedRelationViaRule(derivedRelations, fr, rule);
						final ReverseRelation rr = re.getReverseRelation();
						if (null != rr)
							addDerivedRelationViaRule(derivedRelations, rr, rule);
					}
				});
			});

		final HashMap<Entity, HashSet<Relation>> map = new HashMap<>();
		
		allOntologies.stream()
			.filter(o -> o instanceof Vocabulary)
			.flatMap(o -> OmlRead.getStatements(o).stream())
			.filter(s -> s instanceof Entity)
			.map(s -> (Entity)s)
			.forEach(entity -> {
				OmlSearch.findRelationRestrictions(entity).forEach(r -> {
					if (r instanceof RelationCardinalityRestrictionAxiom) {
						final RelationCardinalityRestrictionAxiom restriction = (RelationCardinalityRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case MIN:
						case EXACTLY:
							final Relation relation = restriction.getRelation();
							final HashSet<Relation> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							if (derivedRelations.getOrDefault(relation, new HashSet<>()).isEmpty()) {
								set.add(relation);
							}
							break;
						default:
						}
					} else if (r instanceof RelationRangeRestrictionAxiom) {
						final RelationRangeRestrictionAxiom restriction = (RelationRangeRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case SOME:
							final Relation relation = restriction.getRelation();
							final HashSet<Relation> set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							if (derivedRelations.getOrDefault(relation, new HashSet<>()).isEmpty()) {
								set.add(relation);
							}
							break;
						default:
						}
					} else if (r instanceof RelationTargetRestrictionAxiom) {
						final RelationTargetRestrictionAxiom restriction = (RelationTargetRestrictionAxiom) r;
						final Relation relation = restriction.getRelation();
						final HashSet<Relation> set = map.getOrDefault(entity, new HashSet<>());
						map.put(entity, set);
						if (derivedRelations.getOrDefault(relation, new HashSet<>()).isEmpty()) {
							set.add(relation);
						}
					}
				});
			});
		return map;
	}

	/**
	 * Creates a {@link org.jgrapht.alg.util.NeighborCache} of specialized terms and their specializations. For each specialized term, its
	 * successors in the cache are its specializations (direct and indirect).
	 * 
	 * @param allOntologies
	 * @return NeighborCache of terms and their specializations
	 */
	private final static NeighborCache<SpecializableTerm, DefaultEdge> getTermSpecializations(
			final Collection<Ontology> allOntologies) {
		final DirectedAcyclicGraph<SpecializableTerm, DefaultEdge> taxonomy = new DirectedAcyclicGraph<SpecializableTerm, DefaultEdge>(
				DefaultEdge.class);

		allOntologies.stream()
			.filter(o -> o instanceof Vocabulary)
			.flatMap(o -> OmlRead.getStatements(o).stream())
			.filter(s -> s instanceof SpecializableTerm)
			.map(s -> (SpecializableTerm)s)
			.forEach(term -> {
				taxonomy.addVertex(term);
				OmlSearch.findSuperTerms(term).forEach(specialized -> {
					taxonomy.addVertex(specialized);
					taxonomy.addEdge(specialized, term);
				});
			});

		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(taxonomy);
		return new NeighborCache<SpecializableTerm, DefaultEdge>(taxonomy);
	}

	/**
	 * Creates a map from each property to the tree of its specializations.
	 * 
	 * @param allOntologies
	 * @return map from property to property graph
	 * @throws UnsupportedOperationException
	 */
	private final static HashMap<Property, Set<Property>> getPropertyTrees(
			final Collection<Ontology> allOntologies) throws UnsupportedOperationException {
		final HashMap<Property, Set<Property>> map = new HashMap<>();
		final DirectedAcyclicGraph<Property, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		allOntologies.stream()
			.filter(o -> o instanceof Vocabulary)
			.flatMap(o -> OmlRead.getStatements(o).stream())
			.filter(s -> s instanceof Property)
			.map(s -> (Property)s)
			.forEach(p -> {
					graph.addVertex(p);
					if (p instanceof SpecializableTerm) {
						OmlSearch.findSuperTerms((SpecializableTerm)p).forEach(s -> {
							final Property sp = (Property) s;
							graph.addVertex(sp);
							graph.addEdge(sp, p);
						});
					}
				});

		graph.vertexSet().forEach(property -> {
			var subProperties = new HashSet<Property>();
			subProperties.add(property);
			var i = new DepthFirstIterator<>(graph, property);
			while (i.hasNext()) {
				subProperties.add(i.next());
			}
			map.put(property, subProperties);
		});

		return map;
	}
	
	/**
	 * Creates a map from from each relation to the tree of its specializations.
	 * 
	 * @param allOntologies
	 * @return map from relation to relation graph
	 * @throws UnsupportedOperationException
	 */
	private final static HashMap<Relation, Set<Relation>> getRelationTrees(
			final Collection<Ontology> allOntologies) throws UnsupportedOperationException {
		final HashMap<Relation, Set<Relation>> map = new HashMap<>();
		final DirectedAcyclicGraph<Relation, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

		allOntologies.stream()
			.filter(o -> o instanceof Vocabulary)
			.flatMap(o -> OmlRead.getStatements(o).stream())
			.filter(s -> s instanceof RelationEntity)
			.map(s -> (RelationEntity)s)
			.forEach(re -> {
				final Relation f = re.getForwardRelation();
				final Relation r = re.getReverseRelation();
				if (Objects.nonNull(f))
					graph.addVertex(f);
				if (Objects.nonNull(r))
					graph.addVertex(r);
				OmlSearch.findSuperTerms(re).forEach(s -> {
					if (s instanceof RelationEntity) {
						final RelationEntity sre = (RelationEntity) s;
						final Relation sf = sre.getForwardRelation();
						final Relation sr = sre.getReverseRelation();
						if (Objects.nonNull(f) && Objects.nonNull(sf)) {
							graph.addVertex(sf);
							graph.addEdge(sf, f);
						}
						if (Objects.nonNull(r) && Objects.nonNull(sr)) {
							graph.addVertex(sr);
							graph.addEdge(sr, r);
						}
					}
				});
			});

		graph.vertexSet().forEach(relation -> {
			var subRelations = new HashSet<Relation>();
			subRelations.add(relation);
			var i = new DepthFirstIterator<>(graph, relation);
			while (i.hasNext()) {
				subRelations.add(i.next());
			}
			map.put(relation, subRelations);
		});

		return map;
	}
	
	/**
	 * Creates a map from entity to all instances of that entity or its specializations.
	 * 
	 * @param allOntologies
	 * @param entities
	 * @param neighborCache
	 * @return map from entity to instances
	 */
	private final static HashMap<Entity, HashSet<NamedInstance>> getEntityInstances(
			final Collection<Ontology> allOntologies, final HashSet<Entity> entities,
			final NeighborCache<SpecializableTerm, DefaultEdge> neighborCache) {
		final HashMap<Entity, HashSet<NamedInstance>> map = new HashMap<>();

		entities.forEach(entity -> {
			final HashSet<SpecializableTerm> specializations = new HashSet<>(neighborCache.successorsOf(entity));
			specializations.add(entity);
			final HashSet<NamedInstance> instances = new HashSet<>();
			specializations.stream().filter(s -> s instanceof Concept).map(s -> (Concept) s).forEach(concept -> {
				OmlSearch.findInstancesOfType(concept).stream()
					.filter(i -> i instanceof ConceptInstance)
					.map(i -> (ConceptInstance)i)
					.forEach(i -> instances.add(i));
			});
			map.put(entity, instances);
		});
		
		return map;
	}
	
	/**
	 * Returns a map from subject to a map from scalar property to usage count
	 * for generating cardinality restrictions on data properties.
	 * 
	 * @param entitiesWithRestrictedProperties
	 * @param entityInstances
	 * @param neighborCache
	 * @param propertyTrees
	 * @return map from subject to map from scalar property to usage count
	 */
	private static HashMap<NamedInstance, HashMap<ScalarProperty, Integer>> getScalarPropertyCounts(
			final HashMap<Entity, HashSet<ScalarProperty>> entitiesWithRestrictedProperties,
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances,
			final NeighborCache<SpecializableTerm, DefaultEdge> neighborCache,
			final HashMap<Property, Set<Property>> propertyTrees) {
		final HashMap<NamedInstance, HashMap<ScalarProperty, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedProperties.forEach((entity, properties) -> {
			final HashSet<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());
			
			final HashSet<Property> all_properties = new HashSet<>();
			properties.forEach(property -> {
				all_properties.addAll(propertyTrees.get(property));
			});
			
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final HashMap<Property, HashSet<Literal>> subj_vals_map = new HashMap<>();
				final HashMap<ScalarProperty, Integer> subj_count_map = map.getOrDefault(subj, new HashMap<>());
				map.put(subj, subj_count_map);
				
				all_properties.forEach(property -> {
					if (!subj_vals_map.containsKey(property)) subj_vals_map.put(property, new HashSet<Literal>());
				});
				
				OmlSearch.findPropertyValueAssertions(subj).forEach(pva -> {
					if (pva instanceof ScalarPropertyValueAssertion) {
						final ScalarPropertyValueAssertion spva = (ScalarPropertyValueAssertion) pva;
						final ScalarProperty prop = spva.getProperty();
						if (all_properties.contains(prop)) {
							subj_vals_map.get(prop).add(spva.getValue());
						}
					}
				});
				OmlSearch.findAllTypes(subj).stream()
					.filter(r -> r instanceof Entity)
					.flatMap(r -> ((Entity)r).getOwnedPropertyRestrictions().stream())
					.filter(r -> r instanceof ScalarPropertyValueRestrictionAxiom)
					.map(r -> (ScalarPropertyValueRestrictionAxiom)r)
					.filter(r -> all_properties.contains(r.getProperty()))
					.forEach(r -> subj_vals_map.get(r.getProperty()).add(r.getValue()));
			
				properties.forEach(property -> {
					final HashSet<Literal> vals = subj_vals_map.get(property);
					for (Property prop : propertyTrees.get(property)) {
						vals.addAll(subj_vals_map.get(prop));
					}					
					subj_count_map.put(property, vals.size());
				});
			});			
		});
		
		return map;
	}
	
	/**
	 * Returns a map from subject to a map from structured property to usage count
	 * for generating cardinality restrictions on data properties.
	 * 
	 * @param entitiesWithRestrictedProperties
	 * @param entityInstances
	 * @param neighborCache
	 * @param propertyTrees
	 * @return map from subject to structured property to usage count
	 */
	private static HashMap<NamedInstance, HashMap<StructuredProperty, Integer>> getStructuredPropertyCounts(
			final HashMap<Entity, HashSet<StructuredProperty>> entitiesWithRestrictedProperties,
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances,
			final NeighborCache<SpecializableTerm, DefaultEdge> neighborCache,
			final HashMap<Property, Set<Property>> propertyTrees) {
		final HashMap<NamedInstance, HashMap<StructuredProperty, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedProperties.forEach((entity, properties) -> {
			final HashSet<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());
			
			final HashSet<Property> all_properties = new HashSet<>();
			properties.forEach(property -> {
				all_properties.addAll(propertyTrees.get(property));
			});
			
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final HashMap<Property, HashSet<StructureInstance>> subj_vals_map = new HashMap<>();
				final HashMap<StructuredProperty, Integer> subj_count_map = map.getOrDefault(subj, new HashMap<>());
				map.put(subj, subj_count_map);
				
				all_properties.forEach(property -> {
					if (!subj_vals_map.containsKey(property)) subj_vals_map.put(property, new HashSet<StructureInstance>());
				});
				
				OmlSearch.findPropertyValueAssertions(subj).forEach(pva -> {
					if (pva instanceof StructuredPropertyValueAssertion) {
						final StructuredPropertyValueAssertion spva = (StructuredPropertyValueAssertion) pva;
						final StructuredProperty prop = spva.getProperty();
						if (all_properties.contains(prop)) {
							subj_vals_map.get(prop).add(spva.getValue());
						}
					}
				});
				OmlSearch.findAllTypes(subj).stream()
					.filter(r -> r instanceof Entity)
					.flatMap(r -> ((Entity)r).getOwnedPropertyRestrictions().stream())
					.filter(r -> r instanceof StructuredPropertyValueRestrictionAxiom)
					.map(r -> (StructuredPropertyValueRestrictionAxiom)r)
					.filter(r -> all_properties.contains(r.getProperty()))
					.forEach(r -> subj_vals_map.get(r.getProperty()).add(r.getValue()));
				
				properties.forEach(property -> {
					final HashSet<StructureInstance> vals = subj_vals_map.get(property);
					for (Property prop : propertyTrees.get(property)) {
						vals.addAll(subj_vals_map.get(prop));
					}					
					subj_count_map.put(property, vals.size());
				});
			});			
		});
		
		return map;
	}
	
	/**
	 * Returns a map from subject to a map from relation to usage count
	 * for generating cardinality restrictions on data properties.
	 * 
	 * @param entitiesWithRestrictedRelations
	 * @param entityInstances
	 * @param neighborCache
	 * @param relationTrees
	 * @return map from subject to relation to usage count
	 */
	private static HashMap<NamedInstance, HashMap<Relation, Integer>> getRelationCounts(
			final HashMap<Entity, HashSet<Relation>> entitiesWithRestrictedRelations,
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances,
			final NeighborCache<SpecializableTerm, DefaultEdge> neighborCache,
			final HashMap<Relation, Set<Relation>> relationTrees) {
		final HashMap<NamedInstance, HashMap<Relation, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedRelations.forEach((entity, relations) -> {
			final HashSet<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());

			final HashSet<Relation> all_relations = new HashSet<>();
			relations.forEach(relation -> {
				all_relations.addAll(relationTrees.get(relation));
			});
			
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final HashMap<Relation, HashSet<NamedInstance>> subj_vals_map = new HashMap<>();
				final HashMap<Relation, Integer> subj_count_map = map.getOrDefault(subj, new HashMap<>());
				map.put(subj, subj_count_map);

				all_relations.forEach(relation -> {
					if (!subj_vals_map.containsKey(relation))
						subj_vals_map.put(relation, new HashSet<NamedInstance>());
				});

				OmlSearch.findRelationInstancesWithSource(subj).forEach(ri -> {
					ri.getOwnedTypes().forEach(rta ->{
						final Relation rel = rta.getType().getForwardRelation();
						if (all_relations.contains(rel)) {
							subj_vals_map.get(rel).addAll(ri.getTargets());
						}
					});
				});

				OmlSearch.findLinkAssertionsWithTarget(subj).forEach(li -> {
					final Relation rel = li.getRelation().getInverse();
					if (all_relations.contains(rel)) {
						subj_vals_map.get(rel).add(OmlRead.getSource(li));
					}
				});

				OmlSearch.findLinkAssertions(subj).forEach(link -> {
					final Relation rel = link.getRelation();
					if (all_relations.contains(rel)) {
						subj_vals_map.get(rel).add(OmlRead.getTarget(link));
					}

				});
				OmlSearch.findAllTypes(subj).stream()
					.filter(r -> r instanceof Entity)
					.flatMap(r -> ((Entity)r).getOwnedRelationRestrictions().stream())
					.filter(r -> r instanceof RelationTargetRestrictionAxiom)
					.map(r -> (RelationTargetRestrictionAxiom)r)
					.filter(r -> all_relations.contains(r.getRelation()))
					.forEach(r -> subj_vals_map.get(r.getRelation()).add(r.getTarget()));

				relations.forEach(relation -> {
					final HashSet<NamedInstance> vals = subj_vals_map.get(relation);
					for (Relation rel : relationTrees.get(relation)) {
						vals.addAll(subj_vals_map.get(rel));
					}					
					subj_count_map.put(relation, vals.size());
				});
			});			
		});
		
		return map;
	}

    /**
     * A subclass of CloseDescriptionBundle that modifies a given Owl ontology
     */
	public static class CloseDescriptionBundleToOwl extends CloseDescriptionBundle {
		
		/**
		 * The Owl ontology
		 */
		protected final OWLOntology ontology;
		
		/**
		 * Th Owl API
		 */
		protected final OwlApi owlApi;

		/**
		 * Creates a new CloseDescriptionBundleToOwl object
		 * 
		 * @param resource the description bundle resource
		 * @param ontology the Owl ontology
		 * @param owlApi the Owl API
		 */
		public CloseDescriptionBundleToOwl(final Resource resource, final OWLOntology ontology, final OwlApi owlApi) {
			super(resource);
			this.ontology = ontology;
			this.owlApi = owlApi;
		}

		/**
		 * Runs the algorithm
		 */
		public void run() {
			final Ontology omlOntology = OmlRead.getOntology(this.resource);
			final Collection<Ontology> allOntologies = OmlRead.getAllImportedOntologies(omlOntology, true);

			final HashMap<Entity, HashSet<ScalarProperty>> entitiesWithRestrictedScalarProperties = getEntitiesWithRestrictedScalarProperties(allOntologies);
			final HashMap<Entity, HashSet<StructuredProperty>> entitiesWithRestrictedStructuredProperties = getEntitiesWithRestrictedStructuredProperties(allOntologies);
			final HashMap<Entity, HashSet<Relation>> entitiesWithRestrictedRelations = getEntitiesWithRestrictedRelations(allOntologies);
			
			final NeighborCache<SpecializableTerm, DefaultEdge> termSpecializations = getTermSpecializations(allOntologies);
			final HashMap<Property, Set<Property>> propertyTrees = getPropertyTrees(allOntologies);
			final HashMap<Relation, Set<Relation>> relationTrees = getRelationTrees(allOntologies);

			final HashSet<Entity> allRestrictedEntities = new HashSet<Entity>();
			allRestrictedEntities.addAll(entitiesWithRestrictedScalarProperties.keySet());
			allRestrictedEntities.addAll(entitiesWithRestrictedStructuredProperties.keySet());
			allRestrictedEntities.addAll(entitiesWithRestrictedRelations.keySet());
			
			final HashMap<Entity, HashSet<NamedInstance>> entityInstances = getEntityInstances(allOntologies, allRestrictedEntities, termSpecializations);
			final HashMap<NamedInstance, HashMap<ScalarProperty, Integer>> scalarPropertyCounts = getScalarPropertyCounts(entitiesWithRestrictedScalarProperties, entityInstances, termSpecializations, propertyTrees);
			final HashMap<NamedInstance, HashMap<StructuredProperty, Integer>> structuredPropertyCounts = getStructuredPropertyCounts(entitiesWithRestrictedStructuredProperties, entityInstances, termSpecializations, propertyTrees);
			final HashMap<NamedInstance, HashMap<Relation, Integer>> relationCounts = getRelationCounts(entitiesWithRestrictedRelations, entityInstances, termSpecializations, relationTrees);
			
			/*
			 * Generate data property cardinality restrictions.
			 */
			scalarPropertyCounts.forEach((subj, map) -> {
				final IRI subj_iri = IRI.create(subj.getIri());
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(subj_iri);
				map.forEach((prop, c) -> {
					final IRI prop_iri = IRI.create(prop.getIri());
					final OWLDataProperty dp = this.owlApi.getOWLDataProperty(prop_iri);
					final OWLDataMaxCardinality mc = this.owlApi.getOWLDataMaxCardinality(c, dp);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});
			
			/*
			 * Generate object property cardinality restrictions.
			 */
			structuredPropertyCounts.forEach((subj, map) -> {
				final IRI subj_iri = IRI.create(subj.getIri());
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(subj_iri);
				map.forEach((prop, c) -> {
					final IRI prop_iri = IRI.create(prop.getIri());
					final OWLObjectProperty dp = this.owlApi.getOWLObjectProperty(prop_iri);
					final OWLObjectMaxCardinality mc = this.owlApi.getOWLObjectMaxCardinality(c, dp);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});
			relationCounts.forEach((subj, map) -> {
				final IRI subj_iri = IRI.create(subj.getIri());
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(subj_iri);
				map.forEach((prop, c) -> {
					final IRI prop_iri = IRI.create(prop.getIri());
					final OWLObjectProperty op = this.owlApi.getOWLObjectProperty(prop_iri);
					final OWLObjectMaxCardinality mc = this.owlApi.getOWLObjectMaxCardinality(c, op);
					final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
					this.ontology.add(ca);
				});
			});

		}
	}

}
