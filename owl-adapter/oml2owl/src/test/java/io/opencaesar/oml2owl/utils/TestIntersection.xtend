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
import io.opencaesar.oml2owl.utils.Empty
import io.opencaesar.oml2owl.utils.Complement
import io.opencaesar.oml2owl.utils.Difference
import io.opencaesar.oml2owl.utils.Intersection
import io.opencaesar.oml2owl.utils.Union

class TestIntersection {
	Singleton sa1
	Singleton sa2
	Singleton sb
	Singleton sc
	Intersection a1
	Intersection a1ia2
	Intersection a1ia2ib
	Intersection a1ia2ibic
	Intersection a1ib
	Intersection a1ibic
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
		var Singleton[] sl1 = #[sa1, sa2, sb, sc]
		var HashSet<ClassExpression> a1l = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 1))
		a1 = new Intersection(a1l)
		var HashSet<ClassExpression> a1ia2l = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 2))
		a1ia2 = new Intersection(a1ia2l)
		var HashSet<ClassExpression> a1ia2ibl = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 3))
		a1ia2ib = new Intersection(a1ia2ibl)
		var HashSet<ClassExpression> a1ia2ibicl = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 4))
		a1ia2ibic = new Intersection(a1ia2ibicl)
		var Singleton[] sl2 = #[sa1, sb, sc]
		var HashSet<ClassExpression> a1ibl = new HashSet<ClassExpression>(Arrays.asList(sl2).subList(0, 2))
		a1ib = new Intersection(a1ibl)
		var HashSet<ClassExpression> a1ibicl = new HashSet<ClassExpression>(Arrays.asList(sl2).subList(0, 3))
		a1ibic = new Intersection(a1ibicl)
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testHashCode() {
		assertEquals(a1.hashCode(), a1ia2.hashCode())
		assertNotEquals(a1.hashCode(), a1ia2ib.hashCode())
		assertNotEquals(a1.hashCode(), a1ia2ibic.hashCode())
		assertNotEquals(a1.hashCode(), a1ib.hashCode())
		assertNotEquals(a1.hashCode(), a1ibic.hashCode())
		assertNotEquals(a1ia2.hashCode(), a1ia2ib.hashCode())
		assertNotEquals(a1ia2.hashCode(), a1ia2ibic.hashCode())
		assertNotEquals(a1ia2.hashCode(), a1ib.hashCode())
		assertNotEquals(a1ia2.hashCode(), a1ibic.hashCode())
		assertNotEquals(a1ia2ib.hashCode(), a1ia2ibic.hashCode())
		assertEquals(a1ia2ib.hashCode(), a1ib.hashCode())
		assertNotEquals(a1ia2ib.hashCode(), a1ibic.hashCode())
		assertNotEquals(a1ia2ibic.hashCode(), a1ib.hashCode())
		assertEquals(a1ia2ibic.hashCode(), a1ibic.hashCode())
		assertNotEquals(a1ib.hashCode(), a1ibic.hashCode())
	}

	@Test def void testIntersection() {
		assertNotNull(a1)
		assertNotNull(a1ia2)
		assertNotNull(a1ia2ib)
		assertNotNull(a1ia2ibic)
	}

	@Test def void testIntersection1() {
		assertEquals(a1ia2, a1.intersection(sa2))
		assertEquals(a1ia2ib, a1.intersection(sa2).intersection(sb))
		assertEquals(a1ia2ib, a1.intersection(sb).intersection(sa2))
		assertEquals(a1ia2ib, a1ia2.intersection(sb))
		assertEquals(a1ia2ibic, a1.intersection(sa2).intersection(sb).intersection(sc))
		assertEquals(a1ia2ibic, a1ia2.intersection(sb).intersection(sc))
		assertEquals(a1ia2ibic, a1ia2.intersection(sc).intersection(sb))
		assertEquals(a1ia2ibic, a1ia2ib.intersection(sc))
		// Theorem 2
		assertEquals(a1ia2, a1ia2.intersection(a1ia2))
		// Theorem 3
		assertEquals(a1ia2.intersection(a1ia2ib), a1ia2ib.intersection(a1ia2))
		// Theorem 4
		assertEquals((a1ia2.intersection(a1ia2ib)).intersection(a1ia2ibic),
			(a1ia2.intersection(a1ia2ib)).intersection(a1ia2ibic))
	}

	@Test def void testEqualsObject() {
		assertEquals(a1, a1ia2)
		assertNotEquals(a1, a1ia2ib)
		assertNotEquals(a1, a1ia2ibic)
		assertNotEquals(a1, a1ib)
		assertNotEquals(a1, a1ibic)
		assertNotEquals(a1ia2, a1ia2ib)
		assertNotEquals(a1ia2, a1ia2ibic)
		assertNotEquals(a1ia2, a1ib)
		assertNotEquals(a1ia2, a1ibic)
		assertNotEquals(a1ia2ib, a1ia2ibic)
		assertEquals(a1ia2ib, a1ib)
		assertNotEquals(a1ia2ib, a1ibic)
		assertNotEquals(a1ia2ibic, a1ib)
		assertEquals(a1ia2ibic, a1ibic)
		assertNotEquals(a1ib, a1ibic)
	}

	@Test def void testToString() {
		assertEquals("a", a1.toString())
		assertEquals("a", a1ia2.toString())
		assertEquals("a∩b", a1ia2ib.toString())
		assertEquals("a∩b", a1ib.toString())
		assertEquals("a∩b∩c", a1ia2ibic.toString())
		assertEquals("a∩b∩c", a1ibic.toString())
	}

	@Test def void testComplement() {
		var Complement a1c = new Complement(a1)
		var Complement a1ia2c = new Complement(a1ia2)
		var Complement a1ia2ibc = new Complement(a1ia2ib)
		var Complement a1ia2ibicc = new Complement(a1ia2ibic)
		assertEquals(a1c, a1.complement())
		assertEquals(a1ia2c, a1ia2.complement())
		assertEquals(a1ia2ibc, a1ia2ib.complement())
		assertEquals(a1ia2ibicc, a1ia2ibic.complement())
		// Theorem 1
		assertEquals(a1ia2, a1ia2.complement().complement())
	}

	@Test def void testDifference() {
		var Difference a2ia2ibma1ia2ibic = new Difference(a1ia2ib, a1ia2ibic)
		assertEquals(empty, a1.difference(a1ia2))
		assertEquals(a2ia2ibma1ia2ibic, a1ia2ib.difference(a1ia2ibic))
		// Theorem 8
		var Intersection[] il = #[a1ia2ib, a1ia2ibic]
		var HashSet<ClassExpression> us = new HashSet<ClassExpression>(Arrays.asList(il))
		var Difference d = new Difference(a1ia2, new Union(us))
		assertEquals(d, a1ia2.difference(a1ia2ib).difference(a1ia2ibic))
		assertEquals(d, a1ia2.difference(a1ia2ibic).difference(a1ia2ib))
		// Theorem 11
		assertEquals(a1ib, a1ib.difference(empty))
		// Theorem 13
		assertEquals(empty, a1ib.difference(a1ib))
		// Theorem 16
		assertEquals(empty, a1ib.difference(universal))
	}

	@Test def void testUnion() {
		var Intersection[] sl = #[a1, a1ia2, a1ia2ib]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Union u = new Union(s)
		assertEquals(u, a1.union(a1ia2).union(a1ia2ib))
		assertEquals(u, a1ia2.union(a1ia2ib).union(a1))
		// Theorem 2
		assertEquals(a1ia2, a1ia2.union(a1ia2))
		// Theorem 3
		assertEquals(a1ia2.union(a1ia2ib), a1ia2ib.union(a1ia2))
		// Theorem 4
		assertEquals((a1ia2.union(a1ia2ib)).union(a1ia2ibic), (a1ia2.union(a1ia2ib)).union(a1ia2ibic))
	}

	@Test def void testToAtom() {
		assertEquals("a", a1.toAtom())
		assertEquals("a", a1ia2.toAtom())
		assertEquals("(a∩b)", a1ia2ib.toAtom())
		assertEquals("(a∩b)", a1ib.toAtom())
		assertEquals("(a∩b∩c)", a1ia2ibic.toAtom())
		assertEquals("(a∩b∩c)", a1ibic.toAtom())
	}
}
