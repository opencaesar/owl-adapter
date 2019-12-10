package io.opencaesar.oml2owl

import io.opencaesar.oml.Aspect
import io.opencaesar.oml.Entity
import io.opencaesar.oml.Ontology
import io.opencaesar.oml.SpecializationAxiom
import io.opencaesar.oml2owl.utils.ClassExpression
import io.opencaesar.oml2owl.utils.Singleton
import io.opencaesar.oml2owl.utils.Taxonomy
import io.opencaesar.oml2owl.utils.Universal
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.resource.Resource
import org.semanticweb.owlapi.model.OWLOntology

import static extension io.opencaesar.oml.util.OmlRead.*
import static extension io.opencaesar.oml2owl.utils.OwlClassExpression.*

class CloseBundle {
	
	protected val Resource resource
		
  	new(Resource resource) {
		this.resource = resource
	}
	
	private def Taxonomy omlTaxonomy(List<Ontology> allOntologies) {

		val Taxonomy taxonomy = new Taxonomy
		val singletonMap = new HashMap<Entity, Singleton>
		
		allOntologies.forEach [ g |
			g.eAllContents.filter(Entity).filter[e | !(e instanceof Aspect)].forEach [ Entity entity |
				val s = new Singleton(entity)
				singletonMap.put(entity, s)
				taxonomy.addVertex(s)
			]
		]
		
		allOntologies.forEach [ g |
			g.eAllContents.filter(SpecializationAxiom).forEach [ axiom |
				val specializedSingleton = singletonMap.get(axiom.specializedTerm)
				val specializingSingleton = singletonMap.get(axiom.specializingTerm)

				if (specializedSingleton !== null && specializingSingleton !== null) {
					try {
						taxonomy.addEdge(specializedSingleton, specializingSingleton)
					} catch (IllegalArgumentException e) {
						val msg = e.getMessage + ": while adding edge " + specializedSingleton.toString + " -> " + specializingSingleton.toString
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
	protected val OwlApi owlApi

	new(Resource resource, OWLOntology ontology, OwlApi owlApi) {
		super(resource)
		this.ontology = ontology
		this.owlApi = owlApi
	}
	
	def void run() {
		getSiblingMap.values.forEach[ v |
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
