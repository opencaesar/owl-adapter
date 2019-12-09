package io.opencaesar.oml2owl

import io.opencaesar.oml.Aspect
import io.opencaesar.oml.Entity
import io.opencaesar.oml.Ontology
import io.opencaesar.oml.SpecializationAxiom
import io.opencaesar.oml2owl.utils.ClassExpression
import io.opencaesar.oml2owl.utils.Singleton
import io.opencaesar.oml2owl.utils.Taxonomy
import io.opencaesar.oml2owl.utils.Universal
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.resource.Resource
import org.semanticweb.owlapi.model.OWLOntology

import static extension io.opencaesar.oml.util.OmlRead.*
import static extension io.opencaesar.oml2owl.utils.OwlClassExpression.*
import org.semanticweb.owlapi.model.OWLClass

class CloseBundle {
	
	protected val Resource resource
		
  	new(Resource resource) {
		this.resource = resource
	}
	
	private def Taxonomy omlTaxonomy(List<Ontology> allOntologies) {

		val Taxonomy taxonomy = new Taxonomy

		allOntologies.forEach [ g |
			g.eAllContents.filter(Entity).forEach [ Entity entity |
				taxonomy.addVertex(new Singleton(entity))
			]
		]
		
		allOntologies.forEach [ g |
			g.eAllContents.filter(SpecializationAxiom).forEach [ axiom |
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
		taxonomy.transitiveReduction.rootAt(new Universal)
	}
	
	def Map<ClassExpression, Set<ClassExpression>> getSiblingMap() {
		
		val AllOntologies = resource.ontology.reflexiveClosure[importedOntologies].toList
		
		val Taxonomy taxonomy = omlTaxonomy(AllOntologies)
		taxonomy.ensureConnected
		
		val Taxonomy tree = taxonomy.treeify
		tree.ensureTree
				
		tree.siblingMap		
	}
}

class CloseBundleToOwl extends CloseBundle {
	
	protected val OWLOntology ontology
	protected val boolean disjointUnions
	protected val OwlApi owlApi

	new(Resource resource, OWLOntology ontology, boolean disjointUnions, OwlApi owlApi) {
		super(resource)
		this.ontology = ontology
		this.disjointUnions = disjointUnions
		this.owlApi = owlApi
	}
	
	def void run() {
		getSiblingMap.forEach[ ce, v |
			if (disjointUnions && (ce instanceof Singleton))
			  owlApi.addDisjointUnion(ontology, 
				(ce as Singleton).toOwlClassExpression(owlApi) as OWLClass,
			  	v.map[toOwlClassExpression(owlApi)].toSet
			  )
			else
			  owlApi.addDisjointClasses(ontology, v.map[toOwlClassExpression(owlApi)])
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
