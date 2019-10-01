package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import java.util.Arrays
import java.util.HashSet
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestDoubleEdgeChainTaxonomy {
	package Taxonomy taxonomyABC
	package Taxonomy taxonomyBC
	package Taxonomy taxonomyAC
	package Taxonomy taxonomyAB
	package Taxonomy taxonomyA
	package Taxonomy taxonomyC
	package Singleton a
	package Singleton b
	package Singleton c
	package HashSet<ClassExpression> setA
	package HashSet<ClassExpression> setAB
	package HashSet<ClassExpression> setB
	package HashSet<ClassExpression> setBC
	package HashSet<ClassExpression> setC

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		taxonomyABC = new Taxonomy()
		taxonomyBC = new Taxonomy()
		taxonomyAC = new Taxonomy()
		taxonomyAB = new Taxonomy()
		taxonomyA = new Taxonomy()
		taxonomyC = new Taxonomy()
		a = new Singleton("a")
		b = new Singleton("b")
		c = new Singleton("c")
		setA = new HashSet<ClassExpression>(Arrays.asList(a))
		setAB = new HashSet<ClassExpression>(Arrays.asList(a, b))
		setB = new HashSet<ClassExpression>(Arrays.asList(b))
		setBC = new HashSet<ClassExpression>(Arrays.asList(b, c))
		setC = new HashSet<ClassExpression>(Arrays.asList(c))
		taxonomyABC.addVertex(a)
		taxonomyABC.addVertex(b)
		taxonomyABC.addVertex(c)
		taxonomyABC.addEdge(a, b)
		taxonomyABC.addEdge(b, c)
		taxonomyBC.addVertex(b)
		taxonomyBC.addVertex(c)
		taxonomyBC.addEdge(b, c)
		taxonomyAC.addVertex(a)
		taxonomyAC.addVertex(c)
		taxonomyAC.addEdge(a, c)
		taxonomyAB.addVertex(a)
		taxonomyAB.addVertex(b)
		taxonomyAB.addEdge(a, b)
		taxonomyA.addVertex(a)
		taxonomyC.addVertex(c)
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testChildrenOf() {
		assertEquals(setB, taxonomyABC.childrenOf(a))
		assertEquals(setC, taxonomyABC.childrenOf(b))
		assertTrue(taxonomyABC.childrenOf(c).isEmpty())
	}

	@Test def void testDirectChildrenOf() {
		assertEquals(setB, taxonomyABC.directChildrenOf(a))
		assertEquals(setC, taxonomyABC.directChildrenOf(b))
		assertTrue(taxonomyABC.directChildrenOf(c).isEmpty())
	}

	@Test def void testDescendantsOf() {
		assertEquals(setBC, taxonomyABC.descendantsOf(a))
		assertEquals(setC, taxonomyABC.descendantsOf(b))
		assertTrue(taxonomyABC.descendantsOf(c).isEmpty())
	}

	@Test def void testParentsOf() {
		if (!((taxonomyABC.parentsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
		assertEquals(setA, taxonomyABC.parentsOf(b))
		assertEquals(setB, taxonomyABC.parentsOf(c))
	}

	@Test def void testDirectParentsOf() {
		if (!((taxonomyABC.directParentsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
		assertEquals(setA, taxonomyABC.directParentsOf(b))
		assertEquals(setB, taxonomyABC.directParentsOf(c))
	}

	@Test def void testAncestorsOf() {
		if (!((taxonomyABC.ancestorsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
		assertEquals(setA, taxonomyABC.ancestorsOf(b))
		assertEquals(setAB, taxonomyABC.ancestorsOf(c))
	}

	@Test def void testMultiParentChild() {
		assertFalse(taxonomyABC.multiParentChild().isPresent())
	}

	@Test def void testExciseVertex() {
		assertEquals(taxonomyBC, taxonomyABC.exciseVertex(a))
		assertEquals(taxonomyAC, taxonomyABC.exciseVertex(b))
		assertEquals(taxonomyAB, taxonomyABC.exciseVertex(c))
	}

	@Test def void testExciseVertices() {
		assertEquals(taxonomyA, taxonomyABC.exciseVertices(setBC))
	}

	@Test def void testExciseVerticesIf() {
		assertEquals(taxonomyA, taxonomyABC.exciseVerticesIf([v|setBC.contains(v)]))
	}

	@Test def void testRootAt() {
		assertEquals(taxonomyABC, taxonomyBC.rootAt(a))
		assertEquals(taxonomyABC, taxonomyC.rootAt(b).rootAt(a))
	}

	@Test def void testTransitiveReduction() {
		var Taxonomy t1 = (taxonomyABC.clone() as Taxonomy)
		assertEquals(taxonomyABC, taxonomyABC.transitiveReduction())
		t1.addEdge(a, c)
		assertEquals(taxonomyABC, t1.transitiveReduction())
	}

	@Test def void testTreeify() {
		assertEquals(taxonomyABC, taxonomyABC.treeify())
	}
}
