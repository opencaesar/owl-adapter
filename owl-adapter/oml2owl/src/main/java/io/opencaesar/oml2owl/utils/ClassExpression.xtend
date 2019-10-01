package io.opencaesar.oml2owl.utils

import java.util.Set
import java.util.HashSet
import java.util.stream.Collectors

/**
 * ClassExpression implements methods for constructing class expressions,
 * including singleton expressions encapsulating a single class, complements, intersections,
 * and unions. While the library does not perform any mathematical reasoning, it employs
 * these theorems to simplify expressions:
 * <ul>
 * <li>Theorem 1: For any class A, (A&prime;)&prime; = A.</li>
 * 
 * <li>Theorem 2: For any class A, A &cap; A = A.</li>
 * 
 * <li>Theorem 3: For any classes A and B, A &cap; B = B &cap; A.</li>
 * 
 * <li>Theorem 4: For any classes A, B, and C,
 * 				(A &cap; B) &cap; C = A &cap; (B &cap; C).</li>
 * 
 * <li>Theorem 5: For any class A, A &cup; A = A.</li>
 * 
 * <li>Theorem 6: For any classes A and B, A &cup; B = B &cup; A.</li>
 * 
 * <li>Theorem 7: For any classes A, B, and C,
 * 				(A &cup; B) &cup; C = A &cup; (B &cup; C).</li>
 * 
 * <li>Theorem 8: For any classes A, B, and C, (A\B)\C = A\(B &cup; C).</li>
 * 
 * <li>Theorem 9: For any class A and empty set &empty;, &empty; &cap; A = &empty;.</li>
 * 
 * <li>Theorem 10: For any class A, &empty; &cup; A = A.</li>
 * 
 * <li>Theorem 11: For any class A, A\&empty; = A.</li>
 * 
 * <li>Theorem 12: For any class A, &empty;\A = &empty;.</li>
 * 
 * <li>Theorem 13: For any class A, A\A = &empty;.</li>
 * 
 * <li>Theorem 14: For any class A and universal set U, U &cap; A = A.</li>
 * 
 * <li>Theorem 15: For any class A, U &cup; A = U.</li>
 * 
 * <li>Theorem 16: For any class A, A\U = &empty;.</li>
 * 
 * <li>Theorem 17: &empty;&prime; = U.</li>  
 * </ul>
 * 
 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
 abstract class ClassExpression {

	/**
	 * @return		ClassExpression The complement of this ClassExpression
	 */
	def ClassExpression complement() { new Complement(this) }
	
	/**
	 * @param		e ClassExpression 
	 * @return		ClassExpression The difference of this ClassExpression and another
	 * 				specified ClassExpression
	 */
	def ClassExpression difference(ClassExpression e) {
		(e instanceof Empty) ?
			// Theorem 11
			this :
				(e instanceof Universal || this.equals(e)) ?
					//Theorem 13, Theorem 16
					new Empty :
						new Difference(this, e)
	}
	
	/**
	 * @param		e ClassExpression 
	 * @return		ClassExpression The intersection of this ClassExpression and another
	 * 				specified ClassExpression
	 */	
	def ClassExpression intersection(ClassExpression e) {
		this.equals(e) ?
			// Theorem 2
			this:
				(e instanceof Intersection || e instanceof Empty || e instanceof Universal) ?
					// Theorem 3
					e.intersection(this) :
						new Intersection(new HashSet<ClassExpression>(#[this, e]))
	}
	
	/**
	 * @param		e ClassExpression 
	 * @return		ClassExpression The union of this ClassExpression and another
	 * 				specified ClassExpression
	 */
	def ClassExpression union(ClassExpression e) {
		this.equals(e) ?
			// Theorem 5
			this:
				(e instanceof Union || e instanceof Empty || e instanceof Universal) ?
					// Theorem 6
					e.union(this) :
						new Union(new HashSet<ClassExpression>(#[this, e]))		
	}
	
	/**
	 * @return		String A string representation of this ClassExpression as an atom
	 */
	def String toAtom() { "(" + toString + ")"}
	
}

/**
 * Universal implements methods for ClassExpressions that denote the universal class.

 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
class Universal extends ClassExpression {
	
	/**
	 * @param		o Object
	 * @return		boolean true if and only if o is a Universal
	 */
	override boolean equals(Object o) {
		o instanceof Universal
	}
	
	/**
	 * @return		int hash code of Universal
	 */
	override int hashCode() {
		Universal.hashCode()
	}
	
	/**
	 * @return		String a string representation of the Universal
	 */
	override String toString() { "U" }
	
	/**
	 * @return		String a string representation of the Universal as an atom
	 */
	override String toAtom() {
		toString()
	}
	
	// Theorems 1, 17
	/**
	 * @return		Empty the complement of the Universal
	 */
	override ClassExpression complement() { new Empty }

	// Theorem 14
	/**
	 * @param		e ClassExpression 
	 * @return		ClassExpression e
	 */	
	override ClassExpression intersection(ClassExpression e) { e }

	// Theorem 15
	/**
	 * @param		e ClassExpression 
	 * @return		Universal
	 */	
	override ClassExpression union(ClassExpression e) { this }
	
}

/**
 * Empty implements methods for ClassExpressions that denote the empty class.

 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
class Empty extends ClassExpression {
	
	/**
	 * @param		o Object
	 * @return		boolean true if and only if o is an Empty
	 */
	override boolean equals(Object o) {
		o instanceof Empty
	}
	
	/**
	 * @return		int hash code of Empty
	 */
	override int hashCode() {
		Empty.hashCode()
	}
	
	/**
	 * @return		String a string representation of the Empty
	 */
	override String toString() {
		"∅"
	}
	
	/**
	 * @return		String a string representation of the Empty as an atom
	 */
	override String toAtom() {
		toString()
	}
	
	// Theorem 17
	/**
	 * @return		Universal the complement of the Empty
	 */
	override ClassExpression complement() { new Universal }
	
	// Theorem 12
	/**
	 * @param		e ClassExpression
	 * @return		Empty
	 */
	override ClassExpression difference(ClassExpression e) { this }

	// Theorem 9
	/**
	 * @param		e ClassExpression
	 * @return		Empty
	 */
	override ClassExpression intersection(ClassExpression e) { this }
	
	// Theorem 10
	/**
	 * @param		e ClassExpression
	 * @return		ClassExpression e
	 */
	override ClassExpression union(ClassExpression e) { e }
		
}

/**
 * Singleton implements methods for ClassExpressions that encapsulate an arbitrary
 * object representing a single class.

 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
class Singleton extends ClassExpression {
	
	protected val Object encapsulatedClass
	
	/**
	 * A Singleton encapsulating the specified class
	 *
	 * @param		encapsulatedClass An arbitrary object representing a class
	 */
	new(Object encapsulatedClass) {
		this.encapsulatedClass = encapsulatedClass
	}
	
	/**
	 * @param		o An arbitrary object
	 * @return		boolean true if and only if o denotes the same Singleton
	 */
	override boolean equals(Object o) {
		(o instanceof Singleton) &&
			(o as Singleton).encapsulatedClass.equals(encapsulatedClass)
	}
	
	/**
	 * @return		int hash code of the Singleton
	 */
	override int hashCode() {
		encapsulatedClass.hashCode
	}
	
	/**
	 * @return		String a string representation of the encapsulated class
	 */
	override String toString() {
		encapsulatedClass.toString
	}
	
	/**
	 * @return		String a string representation of the encapsulated class as an atom
	 */
	override String toAtom() {
		toString
	}
	
}

/**
 * Unary implements methods for ClassExpressions denoting an operation on
 * a single ClassExpression.

 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
abstract class Unary extends ClassExpression {
	
	public ClassExpression e
	
	/**
	 * A Unary involving e
	 * 
	 * @param		e a ClassExpression
	 */
	new(ClassExpression e) {
		this.e = e
	}
	
}

/**
 * Complement implements methods for ClassExpressions denoting complements.

 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
class Complement extends Unary {	
	
	/**
	 * The complement of e
	 * 
	 * @param		e a Class Expression
	 */
	new(ClassExpression e) {
		super(e)
	}
	
	/**
	 * @param		o An arbitrary object
	 * @return		boolean true if and only if o denotes the same Complement
	 */
	override boolean equals(Object o) {
		(o instanceof Complement) &&
			(o as Complement).e.equals(e)
	}
	
	/**
	 * @return		int hash code of the Complement
	 */
	override int hashCode() {
		#[Complement, e].hashCode
	}
	
	/**
	 * @return		String string denoting this Complement
	 */
	override String toString() {
		e.toAtom + "′"
	}
	
	/**
	 * @return		String string denoting this Complement as an atom
	 */
	override String toAtom() {
		toString
	}
	
	/**
	 * @return		ClassExpression the complement of this Complement (simplified)
	 */
	override ClassExpression complement() {
		// Theorem 1
		e
	}
	
}

/**
 * Binary implements methods for ClassExpressions denoting a operation on two
 * 		ClassExpressions.
 *
 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
abstract class Binary extends ClassExpression {
	
	public ClassExpression a
	public ClassExpression b
	
	/**
	 * A Binary involving a and b
	 * 
	 * @param		a a ClassExpression
	 * @param		b a ClassExpression
	 */
	new(ClassExpression a, ClassExpression b) {
		this.a = a
		this.b = b
	}
	
	/**
	 * @param		op String denoting binary operator
	 * @return		String denoting this Binary
	 */
	def String toString(String op) {
		a.toAtom + op + b.toAtom
	}
		
}

/**
 * Difference implements methods for ClassExpressions denoting set differences.
 *
 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
class Difference extends Binary {
	
	/**
	 * Difference denoting minuend minus subtrahend
	 * 
	 * @param		minuend a ClassExpression
	 * @param		subtrahend a ClassExpression
	 */
	new(ClassExpression minuend, ClassExpression subtrahend) {
		super(minuend, subtrahend)
	}
	
	/**
	 * @param		o An arbitrary object
	 * @return		boolean true if and only if o denotes the same Difference
	 */
	override boolean equals(Object o) {
		(o instanceof Difference) &&
			(o as Difference).a.equals(a) &&
			(o as Difference).b.equals(b)
	}
	
	/**
	 * @return		int hash code of the Difference
	 */
	override int hashCode() {
		#[Difference, a, b].hashCode
	}
	
	/**
	 * @return		String denoting this Difference
	 */
	override String toString() {
		toString("\\")
	}
	
	/**
	 * @param		e ClassExpression 
	 * @return		ClassExpression The difference of this ClassExpression and another
	 * 				specified ClassExpression (simplified)
	 */
	override ClassExpression difference(ClassExpression e) {
		(e instanceof Empty) ?
			// Theorem 11
			this :
				(e instanceof Universal || this.equals(e)) ?
					//Theorem 13, Theorem 16
					new Empty :
						new Difference(a, b.union(e))
	}
	
}

/**
 * Nary implements methods for ClassExpressions that denote an operation on a set of
 * ClassExpressions.
 *
 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
abstract class Nary extends ClassExpression {
	
	public Set<ClassExpression> s
	
	/**
	 * Nary involving s
	 * 
	 * @param		s Set&lt;ClassExpression&gt;
	 */
	new(Set<ClassExpression> s) {
		this.s = s
	}
	
	/**
	 * @param		c String denoting the operation
	 * @return		String denoting this Nary
	 */
	def String toString(String c) {
		String.join(c, s.stream().map[toString].collect(Collectors.toList()))
	}
	
	/**
	 * @return	String string denoting this Nary as an atom
	 */
	 override String toAtom() {
	 	(s.size <= 1) ? toString() : super.toAtom()
	 }
}

/**
 * Intersection implements methods for ClassExpressions that denote the intersection of a set of
 * ClassExpressions.
 *
 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
class Intersection extends Nary {
	
	/**
	 * Intersection of s
	 * @param		s Set&lt;ClassExpression&gt;
	 */
	new(Set<ClassExpression> s) {
		super(s)
	}
	
	/**
	 * @param		o An arbitrary object
	 * @return		boolean true if and only if o denotes the same Intersection
	 */
	override boolean equals(Object o) {
		(o instanceof Intersection) &&
			(o as Intersection).s.equals(s)
	}
	
	/**
	 * @return		int hash code of the Intersection
	 */
	override int hashCode() {
		#[Intersection, s].hashCode
	}
	
	/**
	 * @return		String denoting this Intersection
	 */
	override String toString() {
		toString("∩")
	}
	
	/**
	 * @param		e ClassExpression
	 * @return		Intersection denoting intersection of this Intersection with e (simplified)
	 */
	override ClassExpression intersection(ClassExpression e) {		
		val newSet = new HashSet(s)		
		// Theorem 4
		if (e instanceof Intersection)
			newSet.addAll((e as Intersection).s)
		else
			newSet.add(e)						
		new Intersection(newSet)
	}
}

/**
 * Union implements methods for ClassExpressions that denote the union of a set of
 * ClassExpressions.
 *
 * @author		Steven Jenkins j.s.jenkins@jpl.nasa.gov
 * @version		0.0.1
 * @since		0.0.1
 */
class Union extends Nary {
	
	/**
	 * Union of s
	 * 
	 * @param		s Set&lt;ClassExpression&gt;
	 */
	new(Set<ClassExpression> s) {
		super(s)
	}
	
	/**
	 * @param		o An arbitrary object
	 * @return		boolean true if and only if o denotes the same Union
	 */
	override boolean equals(Object o) {
		(o instanceof Union) &&
			(o as Union).s.equals(s)
	}
	
	/**
	 * @return		int hash code of the Union
	 */
	override int hashCode() {
		#[Union, s].hashCode
	}
	
	/**
	 * @return		String denoting this Union
	 */
	override String toString() {
		toString("∪")
	}
	
	/**
	 * @param		e ClassExpression
	 * @return		Union denoting union of this Union with e (simplified)
	 */
	override ClassExpression union(ClassExpression e) {
		val newSet = new HashSet(s)
		// Theorem 7
		if (e instanceof Union)
			newSet.addAll((e as Union).s)
		else
			newSet.add(e)			
		new Union(newSet)
	}
	
}