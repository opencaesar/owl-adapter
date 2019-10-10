package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import java.util.Set
import java.util.stream.Collectors
import java.util.stream.Stream
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestSingleVertexTaxonomy {
	package Taxonomy t
	package Taxonomy e
	package Singleton a

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		t = new Taxonomy()
		e = new Taxonomy()
		a = new Singleton("a")
		t.addVertex(a)
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testChildrenOf() {
		if (!((t.childrenOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	def void testDirectChildrenOf() {
		if (!((t.directChildrenOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testDescendantsOf() {
		if (!((t.descendantsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testParentsOf() {
		if (!((t.parentsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testDirectParentsOf() {
		if (!((t.directParentsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testAncestorsOf() {
		if (!((t.ancestorsOf(a).isEmpty()))) {
			throw new AssertionError()
		}
	}

	@Test def void testExciseVertex() {
		assertEquals(e, t.exciseVertex(a))
	}

	@Test def void testExciseVertices() {
		val Set<ClassExpression> setA = Stream.of(a).map[e | e as ClassExpression].collect(Collectors::toSet())
		assertEquals(e, t.exciseVertices(setA))
	}

	@Test def void testExciseVerticesIf() {
		assertEquals(e, t.exciseVerticesIf([v|v === a]))
	}

	@Test def void testRootAt() {
		assertEquals(t, e.rootAt(a))
	}

	@Test def void testMultiParentChild() {
		assertFalse(t.multiParentChild().isPresent())
	}

	@Test def void testTreeify() {
		assertEquals(t, t.treeify())
	}
}
