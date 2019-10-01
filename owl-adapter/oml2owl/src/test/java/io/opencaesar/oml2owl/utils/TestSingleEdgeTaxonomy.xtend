package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import java.util.Arrays
import java.util.HashSet
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestSingleEdgeTaxonomy {
	package Taxonomy tAB
	package Taxonomy tB
	package Singleton a
	package Singleton b
	package HashSet<ClassExpression> setA
	package HashSet<ClassExpression> setB

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		tAB = new Taxonomy()
		tB = new Taxonomy()
		a = new Singleton("a")
		b = new Singleton("b")
		setA = new HashSet<ClassExpression>(Arrays.asList(a))
		setB = new HashSet<ClassExpression>(Arrays.asList(b))
		tAB.addVertex(a)
		tAB.addVertex(b)
		tAB.addEdge(a, b)
		tB.addVertex(b)
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testChildrenOf() {
		assertEquals(setB, tAB.childrenOf(a))
		if (!((tAB.childrenOf(b).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testDirectChildrenOf() {
		assertEquals(setB, tAB.directChildrenOf(a))
		if (!((tAB.directChildrenOf(b).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testDescendantsOf() {
		assertEquals(setB, tAB.descendantsOf(a))
		if (!((tAB.descendantsOf(b).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testParentsOf() {
		assertEquals(setA, tAB.parentsOf(b))
		if (!((tAB.parentsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testDirectParentsOf() {
		assertEquals(setA, tAB.directParentsOf(b))
		if (!((tAB.directParentsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testAncestorsOf() {
		assertEquals(setA, tAB.ancestorsOf(b))
		if (!((tAB.ancestorsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testExciseVertex() {
		assertEquals(tB, tAB.exciseVertex(a))
	}

	@Test def void testExciseVertices() {
		assertEquals(tB, tAB.exciseVertices(setA))
	}

	@Test def void testExciseVerticesIf() {
		assertEquals(tB, tAB.exciseVerticesIf([v|v === a]))
	}

	@Test def void testRootAt() {
		assertEquals(tAB, tB.rootAt(a))
	}

	@Test def void testMultiParentChild() {
		assertFalse(tAB.multiParentChild().isPresent())
	}

	@Test def void testTreeify() {
		assertEquals(tAB, tAB.treeify())
	}
}
