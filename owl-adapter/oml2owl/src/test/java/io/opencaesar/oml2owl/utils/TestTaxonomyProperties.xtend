package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestTaxonomyProperties {
	
	package Singleton a
	package Singleton b
	package Singleton c
	
	package Taxonomy connectedTree
	package Taxonomy connectedNotTree
	package Taxonomy notConnectedNotTree
	
	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		a = new Singleton("a")
		b = new Singleton("b")
		c = new Singleton("c")
		
		notConnectedNotTree = new Taxonomy
		notConnectedNotTree.addVertex(a)
		notConnectedNotTree.addVertex(b)
		notConnectedNotTree.addVertex(c)
		
		connectedTree = notConnectedNotTree.clone as Taxonomy
		connectedTree.addEdge(a, b)
		connectedTree.addEdge(a, c)
		
		connectedNotTree = connectedTree.clone as Taxonomy
		connectedNotTree.addEdge(b, c)
		
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testIsConnected() {
		assertFalse(notConnectedNotTree.isConnected)
		assertTrue(connectedTree.isConnected)
		assertTrue(connectedNotTree.isConnected)
	}

	@Test def void testEnsureConnected() {
		try {
			notConnectedNotTree.ensureConnected
			fail("no UnconnectedTaxonomyException thrown")
		}
		catch (UnconnectedTaxonomyException e) {
			assertTrue(true)
		}
		
		try {
			connectedTree.ensureConnected
			assertTrue(true)
		}
		catch (UnconnectedTaxonomyException e) {
			fail("UnconnectedTaxonomyException thrown")
		}

		try {
			connectedNotTree.ensureConnected
			assertTrue(true)
		}
		catch (UnconnectedTaxonomyException e) {
			fail("UnconnectedTaxonomyException thrown")
		}
	}

	@Test def void testIsTree() {
		assertFalse(notConnectedNotTree.isTree)
		assertTrue(connectedTree.isTree)
		assertFalse(connectedNotTree.isTree)
	}

	@Test def void testEnsureTree() {
		try {
			notConnectedNotTree.ensureTree
			fail("no InvalidTreeException thrown")
		}
		catch (InvalidTreeException e) {
			assertTrue(true)
		}
	
		try {
			connectedTree.ensureTree
			assertTrue(true)
		}
		catch (InvalidTreeException e) {
			fail("InvalidTreeException thrown")
		}

		try {
			connectedNotTree.ensureTree
			fail("no InvalidTreeException thrown")
		}
		catch (InvalidTreeException e) {
			assertTrue(true)
		}
	}
}
