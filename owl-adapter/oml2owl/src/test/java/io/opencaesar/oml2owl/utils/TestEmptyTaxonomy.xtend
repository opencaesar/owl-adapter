package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestEmptyTaxonomy {
	package Singleton a
	package Taxonomy t
	package Taxonomy tA

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		a = new Singleton("a")
		t = new Taxonomy()
		tA = new Taxonomy()
		tA.addVertex(a)
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testMultiParentChild() {
		assertFalse(t.multiParentChild().isPresent())
	}

	@Test def void testExciseVerticesIf() {
		assertEquals(t, t.exciseVerticesIf([v|true]))
	}

	@Test def void testRootAt() {
		assertEquals(tA, t.rootAt(a))
	}

	@Test def void testTreeify() {
		assertEquals(t, t.treeify())
	}
}
