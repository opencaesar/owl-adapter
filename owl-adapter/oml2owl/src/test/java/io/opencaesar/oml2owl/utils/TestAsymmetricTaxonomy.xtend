package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import java.util.List
import java.util.Optional
import java.util.Set
import java.util.HashMap
import java.util.stream.Collectors
import java.util.stream.Stream
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestAsymmetricTaxonomy {
	
	package HashMap<String, ClassExpression> vertexMap = new HashMap<String, ClassExpression>
	package Taxonomy initialTaxonomy
	package Taxonomy redundantEdgeTaxonomy
	package Taxonomy afterExciseVertexTaxonomy
	package Taxonomy afterExciseVerticesTaxonomy
	package Taxonomy unrootedTaxonomy
	package Taxonomy afterBypassOneTaxonomy
	package Taxonomy afterBypassAllTaxonomy
	package Taxonomy afterReduceTaxonomy
	package Taxonomy afterIsolateOneTaxonomy
	package Taxonomy afterIsolateAllTaxonomy
	package Taxonomy afterTreeifyTaxonomy
	package HashMap<ClassExpression, Set<ClassExpression>> siblingMap = new HashMap<ClassExpression, Set<ClassExpression>>

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {

		// Initial Taxonomy

		var List<String> initialEdgeSpec = Stream::of(
			"a", "b",
			"a", "c",
			"b", "d",
			"b", "e",
			"c", "f",
			"c", "g",
			"c", "i",
			"e", "h",
			"e", "i",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)

		var Set<String> initialVertexNames = initialEdgeSpec.stream.collect(Collectors::toSet)

		initialVertexNames.forEach[ String vn |
				vertexMap.put(vn, new Singleton(vn))
		]
		
		var List<ClassExpression> initialEdgeList = initialEdgeSpec.stream.map[String e|vertexMap.get(e)].collect(
			Collectors::toList)
			
		initialTaxonomy = new Taxonomy(initialEdgeList)
		
		// Redundant edges.
		
		var List<String> redundantEdgeSpec = Stream::of(
			"a", "d",
			"a", "e",
			"a", "f",
			"a", "g",
			"a", "h",
			"a", "i",
			"a", "j",
			"a", "k",
			"b", "h",
			"b", "i",
			"b", "j",
			"c", "j",
			"b", "k",
			"c", "k",
			"e", "j",
			"e", "k",
			"i", "k").collect(Collectors::toList)
			
		var List<ClassExpression> redundantEdgeList = redundantEdgeSpec.stream
			.map[String e|vertexMap.get(e)].collect(Collectors::toList)
			
		redundantEdgeList.addAll(initialEdgeList)
		
		redundantEdgeTaxonomy = new Taxonomy(redundantEdgeList)
		
		// After exciseVertex(i)
		
		var List<String> afterExciseVertexEdgeSpec = Stream::of(
			"a", "b",
			"a", "c",
			"b", "d",
			"b", "e",
			"c", "f",
			"c", "g",
			"c", "j",
			"e", "h", 
			"e", "j",
			"f", "k",
			"j", "k").collect(Collectors::toList)
			
		var List<ClassExpression> afterExciseVertexEdgeList = afterExciseVertexEdgeSpec.stream
			.map[ String e | vertexMap.get(e)].collect(Collectors::toList)
		
		afterExciseVertexTaxonomy = new Taxonomy(afterExciseVertexEdgeList)
		
		// After exciseVertices({b, d, e, f, g})
		
		var List<String> afterExciseVerticesEdgeSpec = Stream::of(
			"c", "f",
			"c", "i",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)
			
		var List<ClassExpression> afterExciseVerticesEdgeList = afterExciseVerticesEdgeSpec.stream
			.map[ String e | vertexMap.get(e)].collect(Collectors::toList)
		
		afterExciseVerticesTaxonomy = new Taxonomy(afterExciseVerticesEdgeList)
		
		// After exciseVertices({b, d, e, f, g})
		
		var List<String> unrootedEdgeSpec = Stream::of(
			"b", "d",
			"b", "e",
			"c", "f",
			"c", "g",
			"c", "i",
			"e", "h",
			"e", "i",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)
			
		var List<ClassExpression> unrootedEdgeList = unrootedEdgeSpec.stream
			.map[String e|vertexMap.get(e)].collect(Collectors::toList)
			
		unrootedTaxonomy = new Taxonomy(unrootedEdgeList)
		
		// After bypass(i, c)
		
		var List<String> afterBypassOneEdgeSpec = Stream::of(
			"a", "b",
			"a", "c", 
			"b", "d",
			"b", "e", 
			"c", "f",
			"c", "g",
			"a", "i",
			"e", "h",
			"e", "i",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)
			
		var List<ClassExpression> afterBypassOneEdgeList = afterBypassOneEdgeSpec.stream
			.map[ String e | vertexMap.get(e)].collect(Collectors::toList)
		
		afterBypassOneTaxonomy = new Taxonomy(afterBypassOneEdgeList)
		
		// After bypass(i, {c, e})
		
		var List<String> afterBypassAllEdgeSpec = Stream::of(
			"a", "b",
			"a", "c", 
			"a", "i",
			"b", "d",
			"b", "e",
			"b", "i",
			"c", "f",
			"c", "g",
			"e", "h",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)
			
		var List<ClassExpression> afterBypassAllEdgeList = afterBypassAllEdgeSpec.stream
			.map[ String e | vertexMap.get(e)].collect(Collectors::toList)
		
		afterBypassAllTaxonomy = new Taxonomy(afterBypassAllEdgeList)
		
		// After bypass(i, {c, e})
		
		var List<String> afterReduceEdgeSpec = Stream::of(
			"a", "b",
			"a", "c",
			"b", "d",
			"b", "e",
			"b", "i",
			"c", "f",
			"c", "g",
			"e", "h",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)
			
		var List<ClassExpression> afterReduceEdgeList = afterReduceEdgeSpec.stream
			.map[String e|vertexMap.get(e)].collect(Collectors::toList)
			
		afterReduceTaxonomy = new Taxonomy(afterReduceEdgeList)
		
		var List<String> afterIsolateOneEdgeSpec = Stream::of(
			"a", "b",
			"a", "c\\i",
			"b", "d",
			"b", "e",
			"b", "i",
			"c\\i", "f",
			"c\\i", "g",
			"e", "h",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)
			
		vertexMap.put("c\\i", vertexMap.get("c").difference(vertexMap.get("i")))
		
		var List<ClassExpression> afterIsolateOneEdgeList = afterIsolateOneEdgeSpec.stream
			.map[ String e | vertexMap.get(e)].collect(Collectors::toList)
		
		afterIsolateOneTaxonomy = new Taxonomy(afterIsolateOneEdgeList)
		
		var List<String> afterIsolateAllEdgeSpec = Stream::of(
			"a", "b",
			"a", "c\\i",
			"b", "d",
			"b", "e\\i",
			"b", "i",
			"c\\i", "f",
			"c\\i", "g",
			"e\\i", "h",
			"f", "k",
			"i", "j",
			"j", "k").collect(Collectors::toList)
			
		vertexMap.put("e\\i", vertexMap.get("e").difference(vertexMap.get("i")))
		
		var List<ClassExpression> afterIsolateAllEdgeList = afterIsolateAllEdgeSpec.stream
			.map[ String e | vertexMap.get(e)].collect(Collectors::toList)
		
		afterIsolateAllTaxonomy = new Taxonomy(afterIsolateAllEdgeList)
		
		var List<String> afterTreeifyEdgeSpec = Stream::of(
			"a", "b",
			"a", "c\\(i∪k)",
			"b", "d",
			"b", "e\\i",
			"b", "i\\k",
			"b", "k",
			"c\\(i∪k)",
			"f\\k", "c\\(i∪k)",
			"g", "e\\i",
			"h", "i\\k",
			"j\\k").collect(Collectors::toList)
			
		vertexMap.put("c\\(i∪k)", vertexMap.get("c").difference((vertexMap.get("i")).union(vertexMap.get("k"))))
		vertexMap.put("f\\k", vertexMap.get("f").difference(vertexMap.get("k")))
		vertexMap.put("i\\k", vertexMap.get("i").difference(vertexMap.get("k")))
		vertexMap.put("j\\k", vertexMap.get("j").difference(vertexMap.get("k")))
		
		var List<ClassExpression> afterTreeifyEdgeList = afterTreeifyEdgeSpec.stream.map[String e|vertexMap.get(e)].
			collect(Collectors::toList)
			
		afterTreeifyTaxonomy = new Taxonomy(afterTreeifyEdgeList)
		
		siblingMap.put(vertexMap.get("a"),
			Stream::of("b", "c\\(i∪k)").map[s|vertexMap.get(s)].collect(Collectors::toSet))
		siblingMap.put(vertexMap.get("b"),
			Stream::of("d", "e\\i", "i\\k", "k").map[s|vertexMap.get(s)].collect(Collectors::toSet))
		siblingMap.put(vertexMap.get("c\\(i∪k)"),
			Stream::of("f\\k", "g").map[s|vertexMap.get(s)].collect(Collectors::toSet))
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testChildrenOf() {
		var Set<ClassExpression> bc = Stream::of("b", "c").map[vn|vertexMap.get(vn)].collect(Collectors::toSet)
		assertEquals(bc, initialTaxonomy.childrenOf(vertexMap.get("a")))
	}

	@Test def void testDescendantsOf() {
		var Set<ClassExpression> bcdefghijk = Stream::of("b", "c", "d", "e", "f", "g", "h", "i", "j", "k").map[ vn |
			vertexMap.get(vn)
		].collect(Collectors::toSet)
		assertEquals(bcdefghijk, initialTaxonomy.descendantsOf(vertexMap.get("a")))
	}

	@Test def void testDirectChildrenOf() {
		var Set<ClassExpression> bc = Stream::of("b", "c").map[vn|vertexMap.get(vn)].collect(Collectors::toSet)
		assertEquals(bc, initialTaxonomy.directChildrenOf(vertexMap.get("a")))
	}

	@Test def void testParentsOf() {
		var Set<ClassExpression> ce = Stream::of("c", "e").map[vn|vertexMap.get(vn)].collect(Collectors::toSet)
		assertEquals(ce, initialTaxonomy.parentsOf(vertexMap.get("i")))
	}

	@Test def void testAncestorsOf() {
		var Set<ClassExpression> abce = Stream::of("a", "b", "c", "e").map[vn|vertexMap.get(vn)].collect(
			Collectors::toSet)
		assertEquals(abce, initialTaxonomy.ancestorsOf(vertexMap.get("i")))
	}

	@Test def void testDirectParentsOf() {
		var Set<ClassExpression> ce = Stream::of("c", "e").map[vn|vertexMap.get(vn)].collect(Collectors::toSet)
		assertEquals(ce, initialTaxonomy.directParentsOf(vertexMap.get("i")))
	}

	@Test def void testMultiParentChild() {
		var Optional<ClassExpression> childOption = initialTaxonomy.multiParentChild()
		assertTrue(childOption.isPresent())
		assertEquals(vertexMap.get("i"), childOption.get())
	}

	@Test def void testExciseVertex() {
		assertEquals(afterExciseVertexTaxonomy, initialTaxonomy.exciseVertex(vertexMap.get("i")))
	}

	@Test def void testExciseVertices() {
		var Set<ClassExpression> exciseSet = Stream::of(
			"a", "b",
			"d", "e",
			"g", "h").map[s|vertexMap.get(s)].collect(Collectors::toSet)
		assertEquals(afterExciseVerticesTaxonomy, initialTaxonomy.exciseVertices(exciseSet))
	}

	@Test def void testExciseVerticesIf() {
		val Set<ClassExpression> exciseSet = Stream::of(
			"a", "b",
			"d", "e",
			"g", "h").map[s|vertexMap.get(s)].collect(Collectors::toSet)
		assertEquals(afterExciseVerticesTaxonomy, initialTaxonomy.exciseVerticesIf[v|exciseSet.contains(v)])
	}

	@Test def void testRootAt() {
		assertEquals(initialTaxonomy, unrootedTaxonomy.rootAt(vertexMap.get("a")))
	}

	@Test def void testTransitiveReduction() {
		assertEquals(initialTaxonomy, redundantEdgeTaxonomy.transitiveReduction())
	}

	@Test def void testBypassParent() {
		var ClassExpression c = vertexMap.get("c")
		var ClassExpression i = vertexMap.get("i")
		assertEquals(afterBypassOneTaxonomy, initialTaxonomy.bypassParent(i, c))
	}

	@Test def void testBypassParents() {
		var ClassExpression i = vertexMap.get("i")
		assertEquals(afterBypassAllTaxonomy, initialTaxonomy.bypassParents(i, initialTaxonomy.parentsOf(i)))
	}

	@Test def void testReduceChild() {
		var ClassExpression i = vertexMap.get("i")
		assertEquals(afterReduceTaxonomy, afterBypassAllTaxonomy.reduceChild(i))
	}

	@Test def void testIsolateChildFromOne() {
		var ClassExpression c = vertexMap.get("c")
		var ClassExpression i = vertexMap.get("i")
		assertEquals(afterIsolateOneTaxonomy, afterReduceTaxonomy.isolateChildFromOne(i, c))
	}

	@Test def void testIsolateChild() {
		var ClassExpression i = vertexMap.get("i")
		assertEquals(afterIsolateAllTaxonomy, afterReduceTaxonomy.isolateChild(i, initialTaxonomy.parentsOf(i)))
	}

	@Test def void testTreeify() {
		assertEquals(afterTreeifyTaxonomy, initialTaxonomy.treeify())
	}

	@Test def void testSiblingMap() {
		assertEquals(siblingMap, afterTreeifyTaxonomy.siblingMap())
	}
}
