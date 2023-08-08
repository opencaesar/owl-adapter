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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.oml.Element;
import io.opencaesar.oml.Entity;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Property;
import io.opencaesar.oml.PropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.PropertyPredicate;
import io.opencaesar.oml.PropertyRangeRestrictionAxiom;
import io.opencaesar.oml.PropertyValueRestrictionAxiom;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.RelationEntity;
import io.opencaesar.oml.RelationEntityPredicate;
import io.opencaesar.oml.ReverseRelation;
import io.opencaesar.oml.Rule;
import io.opencaesar.oml.ScalarProperty;
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
	 * Gets all entities with restrictions on non-derived properties that require closure axioms. Such axioms are minimum and
	 * exact cardinality restrictions and some-values-from range restrictions.
	 *
	 * Derived properties are those that appear in a consequent of a SWRL rule.
	 * 
	 * @param allVocabularies
	 * @return map from entity to set of restricted properties
	 */
	private final static Map<Entity, Set<Property>> getEntitiesWithRestrictedProperties(final Collection<Vocabulary> allVocabularies) {

		/**
		 * This map indicates whether a property is derived via SWRL rules.
		 */
		final Map<Property, Set<Rule>> derivedProperties = new HashMap<>();
		
		allVocabularies.stream()
			.flatMap(o -> OmlRead.getMembers(o).stream())
			.filter(s -> s instanceof Rule)
			.map(s -> (Rule)s)
			.forEach(rule -> {
				rule.getConsequent().forEach(p -> {
					if (p instanceof TypePredicate) {
						final var t = ((TypePredicate) p).getType();
						if (t instanceof RelationEntity) {
							final var re = (RelationEntity) t;
							final var fr = re.getForwardRelation();
							if (null != fr)
								addDerivedPropertyViaRule(derivedProperties, fr, rule);
							final ReverseRelation rr = re.getReverseRelation();
							if (null != rr)
								addDerivedPropertyViaRule(derivedProperties, rr, rule);
						}
					} else if (p instanceof PropertyPredicate) {
						final var property = ((PropertyPredicate) p).getProperty();
						addDerivedPropertyViaRule(derivedProperties, property, rule);
					} else if (p instanceof RelationEntityPredicate) {
						final var re = ((RelationEntityPredicate) p).getType();
						final var fr = re.getForwardRelation();
						if (null != fr)
							addDerivedPropertyViaRule(derivedProperties, fr, rule);
						final ReverseRelation rr = re.getReverseRelation();
						if (null != rr)
							addDerivedPropertyViaRule(derivedProperties, rr, rule);
					}
				});
			});

		final Map<Entity, Set<Property>> map = new HashMap<>();
		
		allVocabularies.stream()
			.flatMap(o -> OmlRead.getMembers(o).stream())
			.filter(s -> s instanceof Entity)
			.map(s -> (Entity)s)
			.forEach(entity -> {
				OmlSearch.findPropertyRestrictionAxioms(entity).forEach(r -> {
					if (r instanceof PropertyCardinalityRestrictionAxiom) {
						final var restriction = (PropertyCardinalityRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case MIN:
						case EXACTLY:
							final var property = restriction.getProperty();
							final var set = map.getOrDefault(entity, new HashSet<>());
							map.put(entity, set);
							if (derivedProperties.getOrDefault(property, new HashSet<>()).isEmpty()) {
								set.add(property);
							}
							break;
						default:
						}
					} else if (r instanceof PropertyRangeRestrictionAxiom) {
						final var restriction = (PropertyRangeRestrictionAxiom) r;
						switch (restriction.getKind()) {
						case SOME:
							final var property = restriction.getProperty();
							final var set = map.getOrDefault(property, new HashSet<>());
							map.put(entity, set);
							if (derivedProperties.getOrDefault(property, new HashSet<>()).isEmpty()) {
								set.add(property);
							}
							break;
						default:
						}
					} else if (r instanceof PropertyValueRestrictionAxiom) {
						final var restriction = (PropertyValueRestrictionAxiom) r;
						final var property = restriction.getProperty();
						final var set = map.getOrDefault(entity, new HashSet<>());
						map.put(entity, set);
						if (derivedProperties.getOrDefault(property, new HashSet<>()).isEmpty()) {
							set.add(property);
						}
					}
				});
			});
		return map;
	}

	private static void addDerivedPropertyViaRule(final Map<Property, Set<Rule>> derivedProperties, final Property p, final Rule rule) {
		final Set<Rule> rs = derivedProperties.getOrDefault(p, new HashSet<>());
		rs.add(rule);
		derivedProperties.put(p, rs);
	}

	/**
	 * Creates a map from each property to the tree of its specializations.
	 * 
	 * @param allVocabularies
	 * @return map from property to property graph
	 * @throws UnsupportedOperationException
	 */
	private final static Map<Property, Set<Property>> getPropertyTrees(
			final Collection<Vocabulary> allVocabularies) throws UnsupportedOperationException {
		final Map<Property, Set<Property>> map = new HashMap<>();
		final SimpleDirectedGraph<Property, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

		allVocabularies.stream()
			.flatMap(o -> OmlRead.getMembers(o).stream())
			.filter(s -> s instanceof Property)
			.map(s -> (Property)s)
			.forEach(p -> {
					graph.addVertex(p);
					OmlSearch.findSuperTerms(p).forEach(s -> {
						final Property sp = (Property) s;
						graph.addVertex(sp);
						graph.addEdge(sp, p);
					});
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
	 * Creates a map from entity to all instances of that entity or its specializations.
	 * 
	 * @param allOntologies
	 * @param entities
	 * @param neighborCache
	 * @return map from entity to instances
	 */
	private final static Map<Entity, Set<NamedInstance>> getEntityInstances(final Set<Entity> entities) {
		final Map<Entity, Set<NamedInstance>> map = new HashMap<>();

		entities.forEach(entity -> {
			final Set<NamedInstance> instances = OmlSearch.findInstancesOfKind(entity).stream()
				.filter(i -> i instanceof NamedInstance)
				.map(i -> (NamedInstance)i)
				.collect(Collectors.toSet());
			map.put(entity, instances);
		});
		
		return map;
	}
	
	/**
	 * Returns a map from subject to a map from property to usage count
	 * for generating cardinality restrictions on properties.
	 * 
	 * @param entitiesWithRestrictedProperties
	 * @param entityInstances
	 * @param neighborCache
	 * @param propertyTrees
	 * @return map from subject to map from property to usage count
	 */
	private static Map<NamedInstance, Map<Property, Integer>> getPropertyCounts(
			final Map<Entity, Set<Property>> entitiesWithRestrictedProperties,
			final Map<Entity, Set<NamedInstance>> entityInstances,
			final Map<Property, Set<Property>> propertyTrees) {
		final Map<NamedInstance, Map<Property, Integer>> map = new HashMap<>();
		
		entitiesWithRestrictedProperties.forEach((entity, properties) -> {
			final Set<NamedInstance> instances = entityInstances.getOrDefault(entity, new HashSet<>());
			
			final Set<Property> all_properties = new HashSet<>();
			properties.forEach(property -> {
				all_properties.addAll(propertyTrees.get(property));
			});
			
			instances.forEach(instance -> {
				final NamedInstance subj = (NamedInstance) instance;
				final Map<Property, Set<Element>> subj_vals_map = new HashMap<>();
				final Map<Property, Integer> subj_count_map = map.getOrDefault(subj, new HashMap<>());
				map.put(subj, subj_count_map);
				
				all_properties.forEach(property -> {
					if (!subj_vals_map.containsKey(property)) subj_vals_map.put(property, new HashSet<Element>());
				});
				
				OmlSearch.findPropertyValueAssertionsWithSubject(subj).forEach(pva -> {
					final var prop = pva.getProperty();
					if (all_properties.contains(prop)) {
						subj_vals_map.get(prop).add(pva.getValue());
					}
				});

				OmlSearch.findRelationInstancesWithSource(subj).forEach(ri -> {
					ri.getOwnedTypes().forEach(rta ->{
						if (rta.getType() instanceof RelationEntity) {
							final Relation rel = ((RelationEntity)rta.getType()).getForwardRelation();
							if (all_properties.contains(rel)) {
								subj_vals_map.get(rel).addAll(ri.getTargets());
							}
						}
					});
				});

				OmlSearch.findPropertyValueAssertionsWithObject(subj).forEach(li -> {
					if (li.getProperty() instanceof Relation) {
						final Relation rel = ((Relation)li.getProperty()).getInverse();
						if (all_properties.contains(rel)) {
							subj_vals_map.get(rel).add(li.getSubject());
						}
					}
				});

				OmlSearch.findRelationInstancesWithTarget(subj).forEach(ri -> {
					ri.getOwnedTypes().forEach(rta ->{
						if (rta.getType() instanceof RelationEntity) {
							final Relation rel = ((RelationEntity)rta.getType()).getReverseRelation();
							if (rel != null && all_properties.contains(rel)) {
								subj_vals_map.get(rel).addAll(ri.getSources());
							}
						}
					});
				});

				OmlSearch.findAllTypes(subj).stream()
					.filter(r -> r instanceof Entity)
					.flatMap(r -> ((Entity)r).getOwnedPropertyRestrictions().stream())
					.filter(r -> r instanceof PropertyValueRestrictionAxiom)
					.map(r -> (PropertyValueRestrictionAxiom)r)
					.filter(r -> all_properties.contains(r.getProperty()))
					.forEach(r -> subj_vals_map.get(r.getProperty()).add(r.getValue()));
				
				properties.forEach(property -> {
					final Set<Element> vals = subj_vals_map.get(property);
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
			final Collection<Ontology> allOntologies = OmlRead.getImportedOntologyClosure(omlOntology, true);
			final Collection<Vocabulary> allVocabularies = allOntologies.stream().filter(o -> o instanceof Vocabulary).map(o -> (Vocabulary)o).collect(Collectors.toList());

			final Map<Entity, Set<Property>> entitiesWithRestrictedProperties = getEntitiesWithRestrictedProperties(allVocabularies);
			
			final Map<Property, Set<Property>> propertyTrees = getPropertyTrees(allVocabularies);

			final Set<Entity> allRestrictedEntities = new HashSet<Entity>();
			allRestrictedEntities.addAll(entitiesWithRestrictedProperties.keySet());
			
			final Map<Entity, Set<NamedInstance>> entityInstances = getEntityInstances(allRestrictedEntities);
			final Map<NamedInstance, Map<Property, Integer>> propertyCounts = getPropertyCounts(entitiesWithRestrictedProperties, entityInstances, propertyTrees);
			
			/*
			 * Generate data property cardinality restrictions.
			 */
			propertyCounts.forEach((subj, map) -> {
				final IRI subj_iri = IRI.create(subj.getIri());
				final OWLNamedIndividual ni = this.owlApi.getOWLNamedIndividual(subj_iri);
				map.forEach((prop, c) -> {
					final IRI prop_iri = IRI.create(prop.getIri());
					if (prop instanceof ScalarProperty) {
						final OWLDataProperty dp = this.owlApi.getOWLDataProperty(prop_iri);
						final OWLDataMaxCardinality mc = this.owlApi.getOWLDataMaxCardinality(c, dp);
						final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
						this.ontology.add(ca);
					} else {
						final OWLObjectProperty dp = this.owlApi.getOWLObjectProperty(prop_iri);
						final OWLObjectMaxCardinality mc = this.owlApi.getOWLObjectMaxCardinality(c, dp);
						final OWLClassAssertionAxiom ca = this.owlApi.getOWLClassAssertionAxiom(mc, ni);
						this.ontology.add(ca);
					}
				});
			});
		}
	}

}