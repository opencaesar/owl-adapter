package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.Arrays
import java.util.HashSet
import io.opencaesar.oml2owl.utils.Singleton
import io.opencaesar.oml2owl.utils.ClassExpression
import io.opencaesar.oml2owl.utils.Complement
import io.opencaesar.oml2owl.utils.Difference
import io.opencaesar.oml2owl.utils.Intersection
import io.opencaesar.oml2owl.utils.Union

class TestSingleton {
	Singleton sa1
	Singleton sa2
	Singleton sb
	Singleton sc
	Empty empty
	Universal universal

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		sa1 = new Singleton("a")
		sa2 = new Singleton("a")
		sb = new Singleton("b")
		sc = new Singleton("c")
		empty = new Empty()
		universal = new Universal()
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testHashCode() {
		assertEquals(sa1.hashCode(), sa2.hashCode())
		assertNotEquals(sa1.hashCode(), sb.hashCode())
		assertNotEquals(sa2.hashCode(), sb.hashCode())
	}

	@Test def void testToAtom() {
		assertEquals("a", sa1.toAtom())
		assertEquals("a", sa2.toAtom())
		assertEquals("b", sb.toAtom())
	}

	@Test def void testSingleton() {
		assertNotNull(sa1)
		assertNotNull(sa2)
		assertNotNull(sb)
	}

	@Test def void testEqualsObject() {
		assertEquals(sa1, sa2)
		assertNotEquals(sa1, sb)
		assertNotEquals(sa2, sb)
	}

	@Test def void testToString() {
		assertEquals("a", sa1.toString())
		assertEquals("a", sa2.toString())
		assertEquals("b", sb.toString())
	}

	@Test def void testComplement() {
		var Complement ca1 = new Complement(sa1)
		var Complement ca2 = new Complement(sa2)
		assertEquals(ca1, sa1.complement())
		assertEquals(ca2, sa2.complement())
		assertEquals(ca1, sa2.complement())
		assertEquals(ca2, sa1.complement())
		// Theorem 1
		assertEquals(sa1, sa1.complement().complement())
	}

	@Test def void testDifference() {
		var Difference amb = new Difference(sa1, sb)
		var Difference bma = new Difference(sb, sa1)
		assertEquals(amb, sa1.difference(sb))
		assertEquals(bma, sb.difference(sa1))
		// Theorem 8
		var Singleton[] sl = #[sb, sc]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Union u = new Union(s)
		assertEquals(sa1.difference(u), sa1.difference(sb).difference(sc))
		assertEquals(sa1.difference(u), sa1.difference(sc).difference(sb))
		// Theorem 11
		assertEquals(sa1, sa1.difference(empty))
		// Theorem 13
		assertEquals(empty, sa1.difference(sa1))
		// Theorem 16
		assertEquals(empty, sa1.difference(universal))
	}

	@Test def void testIntersection() {
		var Singleton[] sl = #[sa1, sb]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Intersection i = new Intersection(s)
		assertEquals(i, sa1.intersection(sb))
		assertEquals(i, sb.intersection(sa1))
		// Theorem 2
		assertEquals(sa1, sa1.intersection(sa1))
		// Theorem 3
		assertEquals(sa1.intersection(sb), sb.intersection(sa1))
		// Theorem 4
		assertEquals((sa1.intersection(sb)).intersection(sc), (sa1.intersection(sb)).intersection(sc))
	}

	@Test def void testUnion() {
		var Singleton[] sl = #[sa1, sb]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Union u = new Union(s)
		assertEquals(u, sa1.union(sb))
		assertEquals(u, sb.union(sa1))
		// Theorem 5
		assertEquals(sa1, sa1.union(sa1))
		// Theorem 6
		assertEquals(sa1.union(sb), sb.union(sa1))
		// Theorem 7
		assertEquals((sa1.union(sb)).union(sc), (sa1.union(sb)).union(sc)) // Theorem 15
	}
}
