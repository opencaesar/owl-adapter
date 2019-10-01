package io.opencaesar.oml2owl.utils

import java.util.Optional
import java.util.HashMap
import java.util.HashSet
import java.util.Set
import java.util.List
import java.util.function.Predicate
import java.util.stream.Collectors

import org.eclipse.xtext.util.Tuples

import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph
import org.jgrapht.alg.TransitiveReduction

class TaxonomyEdge extends DefaultEdge {
	
	override hashCode() {
		#[getSource, getTarget].hashCode
	}
	
	override equals(Object o) {
		(o instanceof TaxonomyEdge)
			&& (o as TaxonomyEdge).getSource == getSource
			&& (o as TaxonomyEdge).getTarget == getTarget
	}
}

class Taxonomy extends DirectedAcyclicGraph<ClassExpression, TaxonomyEdge> {
	
	new() {
		super(TaxonomyEdge)
	}
	
	new(List<ClassExpression> edgeList) {
		super(TaxonomyEdge)
		val HashSet<ClassExpression> vertexSet = new HashSet<ClassExpression>
		vertexSet.addAll(edgeList)
		vertexSet.forEach[ v | addVertex(v)]
		val i = edgeList.iterator
		while (i.hasNext) {
			val p = i.next
			val c = i.next
			addEdge(p, c)
		}
	}
	
	def Set<ClassExpression> childrenOf(ClassExpression v) {
		outgoingEdgesOf(v).stream.map[getEdgeTarget].collect(Collectors.toSet)
	}
	
	def Set<ClassExpression> descendantsOf(ClassExpression v) {
		getDescendants(v)
	}
	
	def Set<ClassExpression> directChildrenOf(ClassExpression v) {
		val Set<ClassExpression> c = childrenOf(v)
		val HashSet<ClassExpression> cd = new HashSet<ClassExpression>
		c.forEach[ e | cd.addAll(descendantsOf(e))]
		c.stream.filter[e | !cd.contains(e)].collect(Collectors.toSet)
	}
	
	def Set<ClassExpression> parentsOf(ClassExpression v) {
		incomingEdgesOf(v).stream.map[getEdgeSource].collect(Collectors.toSet)		
	}
	
	def Set<ClassExpression> ancestorsOf(ClassExpression v) {
		getAncestors(v)
	}
	
	def Set<ClassExpression> directParentsOf(ClassExpression v) {
		val Set<ClassExpression> p = parentsOf(v)
		val HashSet<ClassExpression> pa = new HashSet<ClassExpression>
		p.forEach[ e | pa.addAll(ancestorsOf(e))]
		p.stream.filter[e | !pa.contains(e)].collect(Collectors.toSet)
	}
	
	def Optional<ClassExpression> multiParentChild() {
		vertexSet.stream.filter[parentsOf.length > 1].findFirst
	}
	
	def Taxonomy exciseVertex(ClassExpression v) {

		val Taxonomy g = new Taxonomy
		 
		// Copy all vertices except the specified vertex.
		
		vertexSet.stream.filter[ClassExpression e | e != v].forEach[ClassExpression x | g.addVertex(x)]

		// Copy all edges no involving v. Remember parents and children of v.
		
		val Set<ClassExpression> parents = new HashSet<ClassExpression>
		val Set<ClassExpression> children = new HashSet<ClassExpression>
		
		edgeSet.forEach[e |
			val s = getEdgeSource(e)
			val t = getEdgeTarget(e)
			switch (e) {
				case s == v:
					children.add(t)
				case t == v:
					parents.add(s)
				default:
					g.addEdge(s, t)				
			}
		]
		
		// Add edges from parents to children.
		
		parents.forEach[p |
			children.forEach[c |
				g.addEdge(p, c)
			]
		]
		
		g
		
	}
	
	def Taxonomy exciseVertices(Set<ClassExpression> vs) {
		
		if (vs.isEmpty) {
			this
		} else {
			val vl = vs.toList
			val first = vl.get(0)
			val rest = vl.drop(1).toSet
			exciseVertex(first).exciseVertices(rest)
		}
	}
	
	def Taxonomy exciseVerticesIf(Predicate<ClassExpression> predicate) {
		exciseVertices(vertexSet.stream.filter[v | predicate.test(v)].collect(Collectors.toSet))
	}
	
	def Taxonomy rootAt(ClassExpression root) {

		val Taxonomy g = clone as Taxonomy

		g.addVertex(root)

		vertexSet.stream.filter[v |
			inDegreeOf(v) == 0
		].forEach[t |
			g.addEdge(root, t)
		]

		g
	}
	
	def Taxonomy transitiveReduction() {
		
		val Taxonomy tr = clone as Taxonomy
		
		TransitiveReduction.INSTANCE.reduce(tr)
		tr
	}
	
	/**
	 * Bypass a single parent of a child.
	 * 
	 * @param	child ClassExpression
	 * @param	parent ClassExpression
	 * @return Taxonomy
	 */
	def Taxonomy bypassParent(ClassExpression child, ClassExpression parent) {
		
		val Taxonomy g = new Taxonomy
		
		// Copy all vertices.
		
		vertexSet.forEach[ClassExpression v | g.addVertex(v)]
		
		// Copy all edges except that from parent to child.
		
		edgeSet.stream.map[TaxonomyEdge e | Tuples.pair(e.getEdgeSource, e.getEdgeTarget)]
			.filter[getFirst != parent || getSecond != child]
			.forEach[p | g.addEdge(p.getFirst, p.getSecond)]
		
		// Add edges from direct grandparents to child.
		
		directParentsOf(parent).forEach[ gp | g.addEdge(gp, child)]
		
		g
	}
	
	/**
	 * Recursively bypass parents of a child.
	 * 
	 * @param	child ClassExpression
	 * @param	parents Set<ClassExpression>
	 * @return Taxonomy
	 */
	def Taxonomy bypassParents(ClassExpression child, Set<ClassExpression> parents) {
		
		if (parents.isEmpty)
			this
		else {
			val pl = parents.toList
			val first = pl.get(0)
			val rest = pl.drop(1).toSet
			bypassParent(child, first).bypassParents(child, rest)
		}
		
	}
	
	/**
	 * Eliminate redundant edges above child.
	 * 
	 * @param child ClassExpression
	 * @return Taxonomy
	 */
	def Taxonomy reduceChild(ClassExpression child) {
		
		val Taxonomy g = new Taxonomy
		
		// Copy all vertices.
		
		vertexSet.forEach[ClassExpression v | g.addVertex(v)]
		
		// Copy all edges to child.
		
		edgeSet.stream.map[TaxonomyEdge e | Tuples.pair(e.getEdgeSource, e.getEdgeTarget)]
			.filter[getSecond != child]
			.forEach[p | g.addEdge(p.getFirst, p.getSecond)]
			
		// Eliminate redundant edges above child.
		
		directParentsOf(child).forEach[ClassExpression p | g.addEdge(p, child)]
		
		g
		
	}
	
	/**
	 * Isolate child from one parent.
	 * 
	 * @param child ClassExpression
	 * @param parent ClassExpression
	 * @return Taxonomy
	 * 
	 */
	def isolateChildFromOne(ClassExpression child, ClassExpression parent) {
		if (parentsOf(parent).isEmpty) {
			this
		} else {
			val Taxonomy g = new Taxonomy
			
			val ClassExpression diff = parent.difference(child)
			
			val HashSet<ClassExpression> newVertices = new HashSet<ClassExpression>
			newVertices.addAll(vertexSet)
			newVertices.remove(parent)
			newVertices.add(diff)
			newVertices.forEach[ClassExpression e | g.addVertex(e)]
			
			edgeSet.forEach[TaxonomyEdge e |
				val s = getEdgeSource(e)
				val t = getEdgeTarget(e)
				switch e {
					case s == parent:
						if (t != child) g.addEdge(diff, t)
					case t == parent:
						g.addEdge(s, diff)
					default:
						g.addEdge(s, t)
				}
			]
			
			g
		}
			
	}
	
	/**
	 * Recursively isolate child from parents.
	 * 
	 * @param	child ClassExpression
	 * @param	parents Set<ClassExpression>
	 * @return Taxonomy
	 */
	def Taxonomy isolateChild(ClassExpression child, Set<ClassExpression> parents) {
		
		if (parents.isEmpty)
			this
		else {
			val pl = parents.toList
			val first = pl.get(0)
			val rest = pl.drop(1).toSet
			isolateChildFromOne(child, first).isolateChild(child, rest)
		}
	}
	
	/**
	 * Recursively bypass, reduce, and isolate until the result is a tree.
	 * 
	 * @return Taxonomy
	 * 
	 */
	 def Taxonomy treeify() {
	 	
	 	val co = multiParentChild
	 	
	 	if (co.isPresent) {
	 		val child = co.get
	 		val parents = parentsOf(child)
	 		val bp = bypassParents(child, parents)
	 		val rd = bp.reduceChild(child)
	 		rd.isolateChild(child, parents).treeify
	 	} else {
	 		this
	 	}
	}
	
	/**
	 * Produce a map from each parent ClassExpression to its children
	 * (if more than one).
	 * 
	 * @return HashMap<ClassExpression, Set<ClassExpression>>
	 *
	 */
	def HashMap<ClassExpression, Set<ClassExpression>> siblingMap() {
		val HashMap<ClassExpression, Set<ClassExpression>> map = new HashMap
		vertexSet.forEach[ p |
			val Set<ClassExpression> cl = edgesOf(p).stream
				.filter[e1 | getEdgeSource(e1) == p]
				.map[e2 | getEdgeTarget(e2)].collect(Collectors.toSet)
			if (cl.size > 1) map.put(p, cl)
		]
		map
	}
}
