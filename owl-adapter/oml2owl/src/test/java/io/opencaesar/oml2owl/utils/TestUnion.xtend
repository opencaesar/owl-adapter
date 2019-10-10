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
import io.opencaesar.oml2owl.utils.Union
import io.opencaesar.oml2owl.utils.Intersection
import io.opencaesar.oml2owl.utils.Empty

class TestUnion {
	Singleton sa1
	Singleton sa2
	Singleton sb
	Singleton sc
	Union a1
	Union a1ua2
	Union a1ua2ub
	Union a1ua2ubuc
	Union a1ub
	Union a1ubuc

	@BeforeClass def static void setUpBeforeClass() throws Exception {
	}

	@AfterClass def static void tearDownAfterClass() throws Exception {
	}

	@Before def void setUp() throws Exception {
		sa1 = new Singleton("a")
		sa2 = new Singleton("a")
		sb = new Singleton("b")
		sc = new Singleton("c")
		var Singleton[] sl1 = #[sa1, sa2, sb, sc]
		var HashSet<ClassExpression> a1l = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 1))
		a1 = new Union(a1l)
		var HashSet<ClassExpression> a1ua2l = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 2))
		a1ua2 = new Union(a1ua2l)
		var HashSet<ClassExpression> a1ua2ubl = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 3))
		a1ua2ub = new Union(a1ua2ubl)
		var HashSet<ClassExpression> a1ua2ubucl = new HashSet<ClassExpression>(Arrays.asList(sl1).subList(0, 4))
		a1ua2ubuc = new Union(a1ua2ubucl)
		var Singleton[] sl2 = #[sa1, sb, sc]
		var HashSet<ClassExpression> a1ibl = new HashSet<ClassExpression>(Arrays.asList(sl2).subList(0, 2))
		a1ub = new Union(a1ibl)
		var HashSet<ClassExpression> a1ibicl = new HashSet<ClassExpression>(Arrays.asList(sl2).subList(0, 3))
		a1ubuc = new Union(a1ibicl)
	}

	@After def void tearDown() throws Exception {
	}

	@Test def void testHashCode() {
		assertEquals(a1.hashCode(), a1ua2.hashCode())
		assertNotEquals(a1.hashCode(), a1ua2ub.hashCode())
		assertNotEquals(a1.hashCode(), a1ua2ubuc.hashCode())
		assertNotEquals(a1.hashCode(), a1ub.hashCode())
		assertNotEquals(a1.hashCode(), a1ubuc.hashCode())
		assertNotEquals(a1ua2.hashCode(), a1ua2ub.hashCode())
		assertNotEquals(a1ua2.hashCode(), a1ua2ubuc.hashCode())
		assertNotEquals(a1ua2.hashCode(), a1ub.hashCode())
		assertNotEquals(a1ua2.hashCode(), a1ubuc.hashCode())
		assertNotEquals(a1ua2ub.hashCode(), a1ua2ubuc.hashCode())
		assertEquals(a1ua2ub.hashCode(), a1ub.hashCode())
		assertNotEquals(a1ua2ub.hashCode(), a1ubuc.hashCode())
		assertNotEquals(a1ua2ubuc.hashCode(), a1ub.hashCode())
		assertEquals(a1ua2ubuc.hashCode(), a1ubuc.hashCode())
		assertNotEquals(a1ub.hashCode(), a1ubuc.hashCode())
	}

	@Test def void testUnion() {
		assertNotNull(a1)
		assertNotNull(a1ua2)
		assertNotNull(a1ua2ub)
		assertNotNull(a1ua2ubuc)
	}

	@Test def void testUnion1() {
		assertEquals(a1ua2, a1.union(sa2))
		assertEquals(a1ua2ub, a1.union(sa2).union(sb))
		assertEquals(a1ua2ub, a1.union(sb).union(sa2))
		assertEquals(a1ua2ub, a1ua2.union(sb))
		assertEquals(a1ua2ubuc, a1.union(sa2).union(sb).union(sc))
		assertEquals(a1ua2ubuc, a1ua2.union(sb).union(sc))
		assertEquals(a1ua2ubuc, a1ua2.union(sc).union(sb))
		assertEquals(a1ua2ubuc, a1ua2ub.union(sc))
	}

	@Test def void testEqualsObject() {
		assertEquals(a1, a1ua2)
		assertNotEquals(a1, a1ua2ub)
		assertNotEquals(a1, a1ua2ubuc)
		assertNotEquals(a1, a1ub)
		assertNotEquals(a1, a1ubuc)
		assertNotEquals(a1ua2, a1ua2ub)
		assertNotEquals(a1ua2, a1ua2ubuc)
		assertNotEquals(a1ua2, a1ub)
		assertNotEquals(a1ua2, a1ubuc)
		assertNotEquals(a1ua2ub, a1ua2ubuc)
		assertEquals(a1ua2ub, a1ub)
		assertNotEquals(a1ua2ub, a1ubuc)
		assertNotEquals(a1ua2ubuc, a1ub)
		assertEquals(a1ua2ubuc, a1ubuc)
		assertNotEquals(a1ub, a1ubuc)
	}

	@Test def void testToString() {
		assertEquals("a", a1.toString())
		assertEquals("a", a1ua2.toString())
		assertEquals("a∪b", a1ua2ub.toString())
		assertEquals("a∪b", a1ub.toString())
		assertEquals("a∪b∪c", a1ua2ubuc.toString())
		assertEquals("a∪b∪c", a1ubuc.toString())
	}

	@Test def void testComplement() {
		var Complement a1c = new Complement(a1)
		var Complement a1ia2c = new Complement(a1ua2)
		var Complement a1ia2ibc = new Complement(a1ua2ub)
		var Complement a1ia2ibicc = new Complement(a1ua2ubuc)
		assertEquals(a1c, a1.complement())
		assertEquals(a1ia2c, a1ua2.complement())
		assertEquals(a1ia2ibc, a1ua2ub.complement())
		assertEquals(a1ia2ibicc, a1ua2ubuc.complement())
	}

	@Test def void testDifference() {
		var Empty empty = new Empty()
		var Difference diff = new Difference(a1ua2ub, a1ua2ubuc)
		assertEquals(empty, a1.difference(a1ua2))
		assertEquals(diff, a1ua2ub.difference(a1ua2ubuc))
	}

	@Test def void testIntersection() {
		var Union[] sl = #[a1, a1ua2, a1ua2ub]
		var HashSet<ClassExpression> s = new HashSet<ClassExpression>(Arrays.asList(sl))
		var Intersection u = new Intersection(s)
		assertEquals(u, a1.intersection(a1ua2).intersection(a1ua2ub))
		assertEquals(u, a1ua2.intersection(a1ua2ub).intersection(a1))
	}

	@Test def void testToAtom() {
		assertEquals("a", a1.toAtom())
		assertEquals("a", a1ua2.toAtom())
		assertEquals("(a∪b)", a1ua2ub.toAtom())
		assertEquals("(a∪b)", a1ub.toAtom())
		assertEquals("(a∪b∪c)", a1ua2ubuc.toAtom())
		assertEquals("(a∪b∪c)", a1ubuc.toAtom())
	}
}
