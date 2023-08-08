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

import static io.opencaesar.closeworld.Axiom.AxiomType.DISJOINT_CLASSES;
import static io.opencaesar.closeworld.Axiom.AxiomType.DISJOINT_UNION;
import static io.opencaesar.closeworld.OwlAxiom.toOwlAxiom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;
import org.semanticweb.owlapi.model.OWLOntology;

import io.opencaesar.closeworld.Axiom;
import io.opencaesar.closeworld.ClassExpression;
import io.opencaesar.closeworld.OwlApi;
import io.opencaesar.closeworld.Taxonomy;
import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.Entity;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlSearch;

/**
 * An algorithm to close the world on a vocabulary bundle by adding disjointness axioms
 * between classes without common subclasses
 */
public class CloseVocabularyBundle {

	/**
	 * The vocabulary bundle resource
	 */
	protected final Resource resource;

	/**
	 * Creates a new CloseVocabularyBundle object
	 * 
	 * @param resource The vocabulary bundle resource
	 */
	public CloseVocabularyBundle(final Resource resource) {
		this.resource = resource;
	}

	/**
	 * Returns a transitively-reduced concept taxonomy, rooted at Universal, from a collection of OML ontologies.
	 * 
	 * @param allVocabularies
	 * @return concept taxonomy
	 */
	private Taxonomy omlConceptTaxonomy(final Collection<Vocabulary> allVocabularies) {
		final Map<Entity, ClassExpression.Unitary> singletonMap = new HashMap<Entity, ClassExpression.Unitary>();
		final List<ClassExpression> vertexList = new ArrayList<ClassExpression>();
		final List<ClassExpression> edgeList = new ArrayList<ClassExpression>();

		allVocabularies.stream()
			.flatMap(v -> OmlRead.getMembers(v).stream())
			.filter(e -> e instanceof Entity && !(e instanceof Aspect))
			.map(e -> (Entity)e)
			.filter(e -> !e.isRef())
			.forEach(entity -> {
					final ClassExpression.Unitary s = new ClassExpression.Unitary((entity.getIri()));
					singletonMap.put(entity, s);
					vertexList.add(s);
			});

		allVocabularies.stream()
			.flatMap(v -> OmlRead.getMembers(v).stream())
			.filter(e -> e instanceof Entity && !(e instanceof Aspect))
			.map(e -> (Entity)e)
			.forEach(term -> {
					final ClassExpression.Unitary subSingleton = singletonMap.get(term);
					OmlSearch.findSuperTerms(term).stream().forEach(superTerm -> {
						final ClassExpression.Unitary superSingleton = singletonMap.get(superTerm);
						if (superSingleton != null) {
							edgeList.add(superSingleton);
							edgeList.add(subSingleton);
						}
					});
			});
		
		return new Taxonomy(vertexList, edgeList).transitiveReduction().rootAt(new ClassExpression.Universal());
	}

	/**
	 * A subclass of CloseVocabularyBundle that works on an Owl ontology
	 */
	public static class CloseVocabularyBundleToOwl extends CloseVocabularyBundle {
		
		/**
		 * The Owl ontology
		 */
		protected final OWLOntology ontology;
		
		/**
		 * Whether to add djsjointUnion axioms
		 */
		protected final boolean disjointUnions;
		
		/**
		 * The Owl API
		 */
		protected final OwlApi owlApi;

		/**
		 * Creates a new CloseVocabularyBundleToOwl object
		 * 
		 * @param resource The vocabulary bundle resource
		 * @param ontology The Owl ontology
		 * @param disjointUnions Whether to add djsjointUnion axioms
		 * @param owlApi The Owl API
		 */
		public CloseVocabularyBundleToOwl(final Resource resource, final OWLOntology ontology, final boolean disjointUnions, final OwlApi owlApi) {
			super(resource);
			this.ontology = ontology;
			this.disjointUnions = disjointUnions;
			this.owlApi = owlApi;
		}

		/**
		 * Runs the algorithm
		 */
		public void run() {
			final Ontology omlOntology = OmlRead.getOntology(resource);
			final Collection<Ontology> allOntologies = OmlRead.getImportedOntologyClosure(omlOntology, true);
			final Collection<Vocabulary> allVocabularies = allOntologies.stream().filter(o -> o instanceof Vocabulary).map(o -> (Vocabulary)o).collect(Collectors.toList());
			final Taxonomy conceptTaxonomy = super.omlConceptTaxonomy(allVocabularies);
			final Axiom.AxiomType axiomType = disjointUnions ? DISJOINT_UNION : DISJOINT_CLASSES;

			conceptTaxonomy.generateClosureAxioms(axiomType).forEach(a -> {
				owlApi.addAxiom(ontology, toOwlAxiom(a, owlApi));
			});
		}
	}

}