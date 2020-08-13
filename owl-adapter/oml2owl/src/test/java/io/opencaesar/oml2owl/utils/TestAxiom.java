package io.opencaesar.oml2owl.utils;

import org.junit.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TestAxiom {

    Set<ClassExpression> ces1a, ces1b, ces2a, ces2b;
    Axiom.ClassExpressionSetAxiom.DisjointClassesAxiom djca1a, djca1b, djca2a, djca2b;
    Axiom.ClassExpressionSetAxiom.EquivalentClassesAxiom eqca1a, eqca1b, eqca2a, eqca2b;
    Axiom.ClassExpressionSetAxiom.DisjointUnionAxiom djua1a, djua1b, djua2a, djua2b;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

        final ClassExpression.Singleton a = new ClassExpression.Singleton("a");
        final ClassExpression.Singleton b = new ClassExpression.Singleton("b");
        final ClassExpression.Singleton c = new ClassExpression.Singleton("c");
        final ClassExpression.Singleton d = new ClassExpression.Singleton("d");
        final ClassExpression.Singleton e = new ClassExpression.Singleton("e");

        final ClassExpression.Union aub = new ClassExpression.Union(new HashSet<>(Arrays.asList(a, b)));
        final ClassExpression.Union bua = new ClassExpression.Union(new HashSet<>(Arrays.asList(b, a)));
        final ClassExpression.Intersection ciaub = new ClassExpression.Intersection(new HashSet<>(Arrays.asList(c, aub)));
        final ClassExpression.Intersection buaic = new ClassExpression.Intersection(new HashSet<>(Arrays.asList(bua, c)));
        final ClassExpression.Difference ciaubmd = new ClassExpression.Difference(ciaub, d);
        final ClassExpression.Difference buaicmd = new ClassExpression.Difference(buaic, d);

        ces1a = Arrays.asList(aub, ciaub).stream().collect(Collectors.toSet());
        ces1b = Arrays.asList(bua, buaic).stream().collect(Collectors.toSet());
        ces2a = Arrays.asList(aub, ciaub, ciaubmd, e).stream().collect(Collectors.toSet());
        ces2b = Arrays.asList(bua, buaic, buaicmd, e).stream().collect(Collectors.toSet());

        djca1a = new Axiom.ClassExpressionSetAxiom.DisjointClassesAxiom(ces1a);
        djca1b = new Axiom.ClassExpressionSetAxiom.DisjointClassesAxiom(ces1b);
        djca2a = new Axiom.ClassExpressionSetAxiom.DisjointClassesAxiom(ces2a);
        djca2b = new Axiom.ClassExpressionSetAxiom.DisjointClassesAxiom(ces2b);

        eqca1a = new Axiom.ClassExpressionSetAxiom.EquivalentClassesAxiom(ces1a);
        eqca1b = new Axiom.ClassExpressionSetAxiom.EquivalentClassesAxiom(ces1b);
        eqca2a = new Axiom.ClassExpressionSetAxiom.EquivalentClassesAxiom(ces2a);
        eqca2b = new Axiom.ClassExpressionSetAxiom.EquivalentClassesAxiom(ces2b);

        djua1a = new Axiom.ClassExpressionSetAxiom.DisjointUnionAxiom(e, ces1a);
        djua1b = new Axiom.ClassExpressionSetAxiom.DisjointUnionAxiom(e, ces1b);
        djua2a = new Axiom.ClassExpressionSetAxiom.DisjointUnionAxiom(e, ces2a);
        djua2b = new Axiom.ClassExpressionSetAxiom.DisjointUnionAxiom(e, ces2b);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(djca1a.hashCode(), djca1b.hashCode());
        Assert.assertEquals(djca2a.hashCode(), djca2b.hashCode());

        Assert.assertEquals(eqca1a.hashCode(), eqca1b.hashCode());
        Assert.assertEquals(eqca2a.hashCode(), eqca2b.hashCode());

        Assert.assertEquals(djua1a.hashCode(), djua1b.hashCode());
        Assert.assertEquals(djua2a.hashCode(), djua2b.hashCode());
    }
}
