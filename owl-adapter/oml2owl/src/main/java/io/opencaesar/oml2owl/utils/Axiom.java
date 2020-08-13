package io.opencaesar.oml2owl.utils;

import java.util.Set;

public abstract class Axiom {

    public enum AxiomType {DISJOINT_CLASSES, EQUIVALENT_CLASSES, DISJOINT_UNION}

    public abstract static class ClassExpressionSetAxiom extends Axiom {

        private final Set<ClassExpression> set;

        protected ClassExpressionSetAxiom(Set<ClassExpression> set) {
            super();
            this.set = set;
        }

        protected Set<ClassExpression> getSet() {
            return set;
        }

        public int hashCode() { return set.hashCode(); }

        public String toString(String type) {
            return type + "(" + set.toString() + ")";
        }

        protected static class DisjointClassesAxiom extends ClassExpressionSetAxiom {

            protected DisjointClassesAxiom(Set<ClassExpression> set) {
                super(set);
            }

            public boolean equals(Object o) {
                return (o instanceof DisjointClassesAxiom) && (((DisjointClassesAxiom) o).getSet().equals(getSet()));
            }

            public String toString() {
                return toString("DisjointClasses");
            }
        }

        protected class EquivalentClassesAxiom extends ClassExpressionSetAxiom {

            protected EquivalentClassesAxiom(Set<ClassExpression> set) {
                super(set);
            }
        }

        protected class DisjointUnionAxiom extends ClassExpressionSetAxiom {

            protected ClassExpression.Singleton c;

            protected DisjointUnionAxiom(ClassExpression.Singleton c, Set<ClassExpression> set) {
                super(set);
                this.c = c;
            }

            protected ClassExpression.Singleton getC() {
                return c;
            }
        }
    }

}
