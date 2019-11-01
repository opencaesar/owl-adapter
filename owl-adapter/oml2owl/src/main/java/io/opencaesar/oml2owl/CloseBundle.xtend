package io.opencaesar.oml2owl

import io.opencaesar.oml.Aspect
import io.opencaesar.oml.Entity
import io.opencaesar.oml.Graph
import io.opencaesar.oml.TermSpecializationAxiom
import io.opencaesar.oml2owl.utils.Singleton
import io.opencaesar.oml2owl.utils.Taxonomy
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.semanticweb.owlapi.model.OWLOntology

import static extension io.opencaesar.oml.Oml.*
import java.util.Map
import java.util.Set
import io.opencaesar.oml2owl.utils.ClassExpression
import io.opencaesar.oml2owl.utils.Universal

class CloseBundle {
	
	protected val Resource resource
		
  	new(Resource resource) {
		this.resource = resource
	}
	
	private def Taxonomy omlTaxonomy(List<Graph> allGraphs) {

		val Taxonomy taxonomy = new Taxonomy

		allGraphs.forEach [ g |
			g.eAllContents.filter(Entity).forEach [ Entity entity |
				taxonomy.addVertex(new Singleton(entity))
			]
		]
		
		allGraphs.forEach [ g |
			g.eAllContents.filter(TermSpecializationAxiom).forEach [ axiom |
				val specialized = axiom.specializedTerm
				val specializing = axiom.specializingTerm

				if ((specialized instanceof Entity) &&
					!(specialized instanceof Aspect) &&
					(specializing instanceof Entity) &&
					!(specializing instanceof Aspect)) {
					val specializedSingleton = new Singleton(specialized)
					val specializingSingleton = new Singleton(specializing)
					try {
						taxonomy.addEdge(specializedSingleton, specializingSingleton)
					} catch (IllegalArgumentException e) {
						val msg = e.getMessage + ": while adding edge " + specialized.toString + " -> " + specializing.toString
						throw new IllegalArgumentException(msg)
					}
				}
			]
		]
		taxonomy
	}
	
	def Map<ClassExpression, Set<ClassExpression>> getSiblingMap() {
		
		val Universal universal = new Universal

		val graph = resource.contents.filter(Graph).findFirst[true]
		val allGraphs = graph.allImports.map[importedGraph].toList
		allGraphs.add(graph)
		
		val Taxonomy taxonomy = omlTaxonomy(allGraphs).rootAt(universal)
		taxonomy.ensureConnected
		
		val Taxonomy tree = taxonomy.treeify
		tree.ensureTree
				
		tree.siblingMap		
	}
}

class CloseBundleToOwl extends CloseBundle {
	
	protected val OWLOntology ontology
	protected val OwlApi owlApi

	new(Resource resource, OWLOntology ontology, OwlApi owlApi) {
		super(resource)
		this.ontology = ontology
		this.owlApi = owlApi
	}
	
	def void run() {
		getSiblingMap.values.forEach[ v |
			owlApi.addDisjointClassesAxiom(ontology, v)
		]
		
	}
}

class CloseBundleToOml extends CloseBundle {
	
	new(Resource resource) {
		super(resource)
	}
	
	def void run() throws RuntimeException {
		throw new RuntimeException("CloseBundleToOml not implemented")
	}
}
