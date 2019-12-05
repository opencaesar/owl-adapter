package io.opencaesar.oml2owl.utils

import io.opencaesar.oml.Entity
import io.opencaesar.oml.util.OmlRead
import io.opencaesar.oml2owl.OwlApi
import java.util.HashSet
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLObjectComplementOf
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf
import org.semanticweb.owlapi.model.OWLObjectUnionOf

class OwlClassExpression {
	
	static dispatch def OWLClass toOwlClassExpression(Universal u, OwlApi a) {
		a.getOWLThing
	}
	
	static dispatch def OWLClass toOwlClassExpression(Empty e, OwlApi a) {
		a.getOWLNothing
	}
	
	static dispatch def OWLClass toOwlClassExpression(Singleton s, OwlApi a) {
		a.getOWLClass(IRI.create(OmlRead.getIri(s.encapsulatedClass as Entity)))
	}
	
	static dispatch def OWLObjectComplementOf toOwlClassExpression(Complement c, OwlApi a) {
		a.getOWLObjectComplementOf(c.e.toOwlClassExpression(a))
	}
	
	static dispatch def OWLClassExpression toOwlClassExpression(Difference d, OwlApi a) {
		val operands = new HashSet<ClassExpression>
		operands.add(d.a)
		operands.add(d.b.complement)
		new Intersection(operands).toOwlClassExpression(a)
	}

	static dispatch def OWLObjectIntersectionOf toOwlClassExpression(Intersection i, OwlApi a) {
		val operands = i.s.map[toOwlClassExpression(a)].toSet
		a.getOWLObjectIntersectionOf(operands)
	}

	static dispatch def OWLObjectUnionOf toOwlClassExpression(Union u, OwlApi a) {
		val operands = u.s.map[toOwlClassExpression(a)].toSet
		a.getOWLObjectUnionOf(operands)
	}

}