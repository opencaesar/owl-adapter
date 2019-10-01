package io.opencaesar.oml2owl

import io.opencaesar.oml.Aspect
import io.opencaesar.oml.Entity
import io.opencaesar.oml.Graph
import io.opencaesar.oml.TermSpecializationAxiom
import io.opencaesar.oml2owl.utils.Singleton
import io.opencaesar.oml2owl.utils.Taxonomy
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.jgrapht.GraphTests
import org.jgrapht.alg.connectivity.ConnectivityInspector
import org.jgrapht.graph.AsUndirectedGraph
import org.semanticweb.owlapi.model.OWLOntology

import static extension io.opencaesar.oml.Oml.*

class UnconnectedGraphException extends RuntimeException {	
	new(String s) {
		super(s)
	}
}

class InvalidTreeException extends RuntimeException {	
	new(String s) {
		super(s)
	}
}

class CloseBundle {
	
	protected val Resource resource
	protected val OWLOntology ontology
	protected val OwlApi owlApi
		
  	new(Resource resource, OWLOntology ontology, OwlApi owlApi) {
		this.resource = resource
		this.ontology = ontology
		this.owlApi = owlApi
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
	
	def void run() {
		
		val Singleton owlThing = new Singleton("owl:Thing")

		val graph = resource.contents.filter(Graph).findFirst[true]
		val allGraphs = graph.allImports.map[importedGraph].toList
		allGraphs.add(graph)
		
		val Taxonomy taxonomy = omlTaxonomy(allGraphs).rootAt(owlThing)
		if (!(new ConnectivityInspector(taxonomy).isConnected)) {
			throw (new UnconnectedGraphException("taxonomy is not connected"))
		}
		
		val Taxonomy tree = taxonomy.treeify
		val ug = new AsUndirectedGraph(tree)
		if (!(GraphTests.isTree(ug))) {
			throw (new InvalidTreeException("treeify method returned an invalid tree"))
		}
		
		val siblingMap = tree.siblingMap
		siblingMap.values.forEach[ v |
			owlApi.addDisjointClassesAxiom(ontology, v)
		]
		
	}
}