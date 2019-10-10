package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestUniversal {
	public Singleton sa
	public Empty empty
	public Universal universal1
	public Universal universal2

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		sa = new Singleton("a")
		empty = new Empty()
		universal1 = new Universal()
		universal2 = new Universal()
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testHashCode() {
		assertEquals(universal1.hashCode(), universal2.hashCode())
		assertNotEquals(universal1.hashCode(), empty.hashCode())
	}

	@Test def void testComplement() {
		// Theorem 1
		// Theorem 17
		assertEquals(empty, universal1.complement())
	}

	@Test def void testIntersection() {
		// Theorem 14
		assertEquals(sa, universal1.intersection(sa))
		assertEquals(empty, universal1.intersection(empty))
	}

	@Test def void testUnion() {
		// Theorem 15
		assertEquals(universal1, universal1.union(sa))
		assertEquals(universal1, universal1.union(empty))
	}

	@Test def void testToAtom() {
		assertEquals("U", universal1.toAtom())
	}

	@Test def void testEqualsObject() {
		assertEquals(universal1, universal2)
		assertNotEquals(universal1, empty)
	}

	@Test def void testToString() {
		assertEquals("U", universal1.toString())
	}
}
