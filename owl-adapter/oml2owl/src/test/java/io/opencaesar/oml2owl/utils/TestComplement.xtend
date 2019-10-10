package io.opencaesar.oml2owl.utils

import static org.junit.Assert.*
import java.util.Arrays
import java.util.HashSet
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import io.opencaesar.oml2owl.utils.Singleton
import io.opencaesar.oml2owl.utils.ClassExpression
import io.opencaesar.oml2owl.utils.Complement
import io.opencaesar.oml2owl.utils.Difference
import io.opencaesar.oml2owl.utils.Intersection
import io.opencaesar.oml2owl.utils.Union

class TestComplement {
	Singleton sa1
	Singleton sa2
	Singleton sb
	Singleton sc
	Complement ca1
	Complement ca2
	Complement cb
	Complement cc
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
		ca1 = new Complement(sa1)
		ca2 = new Complement(sa2)
		cb = new Complement(sb)
		cc = new Complement(sc)
		empty = new Empty()
		universal = new Universal()
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testHashCode() {
		assertEquals(ca1.hashCode(), ca2.hashCode())
		assertNotEquals(ca1.hashCode(), cb.hashCode())
		assertNotEquals(ca2.hashCode(), cb.hashCode())
	}

	@Test def void testComplement() {
		assertNotNull(ca1)
		assertNotNull(ca2)
		assertNotNull(cb)
	}

	@Test def void testToAtom() {
		var String caa = "a\u2032"
		var String cba = "b\u2032"
		assertEquals(caa, ca1.toAtom())
		assertEquals(caa, ca2.toAtom())
		assertEquals(cba, cb.toAtom())
	}

	@Test def void testComplement1() {
		// Theorem 1
		assertEquals(sa1, ca1.complement())
		assertEquals(sa2, ca2.complement())
		assertEquals(sb, cb.complement())
		assertEquals(sa1, sa1.complement().complement())
	}

	@Test def void testEqualsObject() {
		assertEquals(ca1, ca2)
		assertNotEquals(ca1, cb)
		assertNotEquals(ca2, cb)
	}

	@Test def void testToString() {
		var String caa = "a\u2032"
		var String cba = "b\u2032"
		assertEquals(caa, ca1.toString())
		assertEquals(caa, ca2.toString())
		assertEquals(cba, cb.toString())
	}

	@Test def void testDifference() {
		var Difference amb = new Difference(ca1, cb)
		var Difference bma = new Difference(cb, ca1)
		assertEquals(amb, ca1.difference(cb))
		assertEquals(bma, cb.difference(ca1))
		// Theorem 8
		var Complement[] sl = #[cb, cc]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Union u = new Union(s)
		assertEquals(ca1.difference(u), ca1.difference(cb).difference(cc))
		assertEquals(ca1.difference(u), ca1.difference(cc).difference(cb))
		// Theorem 11
		assertEquals(ca1, ca1.difference(empty))
		// Theorem 13
		assertEquals(empty, ca1.difference(ca1))
		// Theorem 16
		assertEquals(empty, ca1.difference(universal))
	}

	@Test def void testIntersection() {
		var Complement[] sl = #[ca1, cb]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Intersection i = new Intersection(s)
		assertEquals(i, ca1.intersection(cb))
		assertEquals(i, cb.intersection(ca1))
		// Theorem 2
		assertEquals(ca1, ca1.intersection(ca1))
		// Theorem 3
		assertEquals(ca1.intersection(cb), cb.intersection(ca1))
		// Theorem 4
		assertEquals((ca1.intersection(cb)).intersection(cc), (ca1.intersection(cb)).intersection(cc))
	}

	@Test def void testUnion() {
		var Complement[] sl = #[ca1, cb]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Union u = new Union(s)
		assertEquals(u, ca1.union(cb))
		assertEquals(u, cb.union(ca1))
		// Theorem 5
		assertEquals(ca1, ca1.union(ca1))
		// Theorem 6
		assertEquals(ca1.union(cb), cb.union(ca1))
		// Theorem 7
		assertEquals((ca1.union(cb)).union(cc), (ca1.union(cb)).union(cc)) // Theorem 15
	}
}
