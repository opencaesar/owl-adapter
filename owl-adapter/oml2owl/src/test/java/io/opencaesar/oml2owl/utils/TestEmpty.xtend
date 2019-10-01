package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TestEmpty {
	public Singleton sa
	public Empty empty1
	public Empty empty2
	public Universal universal

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		sa = new Singleton("a")
		empty1 = new Empty()
		empty2 = new Empty()
		universal = new Universal()
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testHashCode() {
		assertEquals(empty1.hashCode(), empty2.hashCode())
		assertNotEquals(empty1.hashCode(), sa.hashCode())
		assertNotEquals(empty1.hashCode(), universal.hashCode())
	}

	@Test def void testComplement() {
		// Theorem 17
		assertEquals(universal, empty1.complement())
	}

	@Test def void testDifference() {
		// Theorem 12
		assertEquals(empty1, empty1.difference(sa))
	}

	@Test def void testIntersection() {
		// Theorem 9
		assertEquals(empty1, empty1.intersection(sa))
		assertEquals(empty1, empty1.intersection(universal))
	}

	@Test def void testUnion() {
		// Theorem 10
		assertEquals(sa, empty1.union(sa))
	}

	@Test def void testToAtom() {
		assertEquals("∅", empty1.toAtom())
	}

	@Test def void testEqualsObject() {
		assertEquals(empty1, empty2)
		assertNotEquals(empty1, sa)
		assertNotEquals(empty1, universal)
	}

	@Test def void testToString() {
		assertEquals("∅", empty1.toString())
	}
}
